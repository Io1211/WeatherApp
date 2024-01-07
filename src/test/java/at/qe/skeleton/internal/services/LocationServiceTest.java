package at.qe.skeleton.internal.services;

import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import at.qe.skeleton.external.model.location.LocationAnswerDTO;
import at.qe.skeleton.external.services.GeocodingApiRequestService;
import at.qe.skeleton.external.services.WeatherApiRequestService;
import at.qe.skeleton.internal.model.CurrentAndForecastAnswer;
import at.qe.skeleton.internal.model.Location;
import at.qe.skeleton.internal.model.LocationId;
import at.qe.skeleton.internal.repositories.CurrentAndForecastAnswerRepository;
import at.qe.skeleton.internal.repositories.LocationRepository;
import at.qe.skeleton.internal.services.utils.FailedToSerializeDTOException;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.jupiter.api.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest
@WebAppConfiguration
public class LocationServiceTest {

  @Autowired LocationRepository locationRepository;

  @Autowired private LocationService locationService;
  @Autowired private CurrentAndForecastAnswerService currentAndForecastAnswerService;
  @Mock private static WeatherApiRequestService mockedWeatherApiRequestService;
  @Mock private static GeocodingApiRequestService mockedGeocodingRequestService;

  @Autowired private CurrentAndForecastAnswerRepository currentAndForecastAnswerRepository;

  private static LocationAnswerDTO mockLocationApiResponseInnsbruck;

  private static ObjectMapper mapper;
  private static CurrentAndForecastAnswerDTO weatherDtoMunich;
  private static CurrentAndForecastAnswerDTO weatherDtoInnsbruck;
  private static LocationAnswerDTO locationDtoMunich;

  @BeforeAll
  public static void setUp() throws IOException {
    String resources = "src/test/resources/";
    mapper = new ObjectMapper().findAndRegisterModules();

    // Innsbruck Location Mock
    List<LocationAnswerDTO> _api =
        mapper.readValue(
            new File(resources + "GeocodingResponseInnsbruck.json"), new TypeReference<>() {});
    mockLocationApiResponseInnsbruck = _api.get(0);

    // Innsbruck Weather Mock
    weatherDtoInnsbruck =
        mapper.readValue(
            new File(resources + "MockCurrentAndForecastAnswersInnsbruck.json"),
            CurrentAndForecastAnswerDTO.class);

    // München Location Mock DTO
    _api =
        mapper.readValue(
            new File(resources + "GeocodingResponseMunich.json"), new TypeReference<>() {});
    locationDtoMunich = _api.get(0);

    // Munich Weather Mock DTO
    weatherDtoMunich =
        mapper.readValue(
            new File(resources + "WeatherApiResponseMunich.json"),
            CurrentAndForecastAnswerDTO.class);
  }

  public Location getMockLocation() {
    Location location = new Location();
    location.setId(
        mockLocationApiResponseInnsbruck.latitude(), mockLocationApiResponseInnsbruck.longitude());
    location.setCity(mockLocationApiResponseInnsbruck.name());
    location.setState(mockLocationApiResponseInnsbruck.state());
    location.setCountry(mockLocationApiResponseInnsbruck.country());
    return location;
  }

  void locationAssertions(Location location, LocationAnswerDTO answerDTO) {
    Assertions.assertNotNull(location);
    Assertions.assertNotNull(location.getWeather());
    Assertions.assertEquals(
        new LocationId(answerDTO.latitude(), answerDTO.longitude()), location.getId());
    Assertions.assertTrue(
        location.getWeather().getTimestampLastCall().isAfter(ZonedDateTime.now().minusMinutes(1)));
    // this also works for case 3, since after overwriting the old with the new weather, it must be
    // up-to-date
  }

  @Test
  void handleLocationSearchTest() throws Exception {
    // inject mocked api services
    ReflectionTestUtils.setField(
        locationService, "geocodingApiRequestService", mockedGeocodingRequestService);
    ReflectionTestUtils.setField(
        currentAndForecastAnswerService,
        "weatherApiRequestService",
        mockedWeatherApiRequestService);
    // mocking the answers for munich (with Precipitation)
    when(mockedGeocodingRequestService.retrieveLocationLonLat("München"))
        .thenReturn(locationDtoMunich);
    when(mockedWeatherApiRequestService.retrieveCurrentAndForecastWeather(48.1371079, 11.5753822))
        .thenReturn(weatherDtoMunich);
    // Case: Location doesnt exist yet in db
    // actual call
    String searchString = "München";
    Location location = locationService.handleLocationSearch(searchString);
    // Assertions
    Assertions.assertEquals("Munich", location.getCity());
    CurrentAndForecastAnswerDTO expectedWeatherDto = weatherDtoMunich;
    byte[] blob = location.getWeather().getWeatherData();
    CurrentAndForecastAnswerDTO actualWeatherDtoStored =
        currentAndForecastAnswerService.deserializeDTO(blob);
    Assertions.assertEquals(expectedWeatherDto, actualWeatherDtoStored);
  }

  private CurrentAndForecastAnswerDTO extractWeatherDtoFromLocation(Location location)
      throws Exception {
    byte[] blob = location.getWeather().getWeatherData();
    return currentAndForecastAnswerService.deserializeDTO(blob);
  }

  @Test
  public void handleLocationSearchWithNoPrecipitation() throws Exception {
    // inject mocked api services
    ReflectionTestUtils.setField(
        locationService, "geocodingApiRequestService", mockedGeocodingRequestService);
    ReflectionTestUtils.setField(
        currentAndForecastAnswerService,
        "weatherApiRequestService",
        mockedWeatherApiRequestService);
    // mocking the answers for innsbruck (without Precipitation)
    when(mockedGeocodingRequestService.retrieveLocationLonLat("Innsbruck"))
        .thenReturn(mockLocationApiResponseInnsbruck);
    when(mockedWeatherApiRequestService.retrieveCurrentAndForecastWeather(47.2654296, 11.3927685))
        .thenReturn(weatherDtoInnsbruck);

    // Case 1: Location doesnt exist yet in db
    String searchString = "Innsbruck";
    Location locationCase1 = locationService.handleLocationSearch(searchString);
    Assertions.assertEquals("Innsbruck", locationCase1.getCity());

    CurrentAndForecastAnswerDTO expectedWeatherDto = weatherDtoInnsbruck;
    CurrentAndForecastAnswerDTO actualWeatherDtoCase1 =
        extractWeatherDtoFromLocation(locationCase1);
    Assertions.assertEquals(expectedWeatherDto, actualWeatherDtoCase1);

    // Case 2: Location exists in db
    // verify that there is indeed 1 Location object in db
    Assertions.assertEquals(1, locationRepository.findAll().size());
    // perform second search for the same Location Name
    Location locationCase2 = locationService.handleLocationSearch(searchString);
    // verify that there is still only 1 Location in db
    Assertions.assertEquals(1, locationRepository.findAll().size());
    // verify that the mocked WeatherApiService only got called once
    verify(mockedWeatherApiRequestService, times(1))
        .retrieveCurrentAndForecastWeather(47.2654296, 11.3927685);
    // verify that the id of the Location object did not change
    Assertions.assertEquals(locationCase1.getId(), locationCase2.getId());
    // verify that the WeatherDto that is being stored is still the correct one.
    CurrentAndForecastAnswerDTO actualWeatherDtoCase2 =
        extractWeatherDtoFromLocation(locationCase2);
    Assertions.assertEquals(expectedWeatherDto, actualWeatherDtoCase2, "");

    // Case 3: Location exists in db but is too old to reuse
    // setting an "old" timestamp for location.
    CurrentAndForecastAnswer oldWeather = locationCase2.getWeather();
    oldWeather.setTimestampLastCall(ZonedDateTime.now().minusHours(2));
    locationService.updateLocationWeather(locationCase2, oldWeather);
    // check whether timestamp has been updated:
    Assertions.assertTrue(
        locationCase2
            .getWeather()
            .getTimestampLastCall()
            .isBefore(ZonedDateTime.now().minusMinutes(1)));

    Location locationCase3 = locationService.handleLocationSearch(searchString);
    // verify that the api has been called another time, in total two times.
    verify(mockedWeatherApiRequestService, times(2))
        .retrieveCurrentAndForecastWeather(47.2654296, 11.3927685);
    // verify that there is still only 1 Location in db
    Assertions.assertEquals(1, locationRepository.findAll().size());
    // verify that the timestamp is now current:
    Assertions.assertTrue(
        locationCase3
            .getWeather()
            .getTimestampLastCall()
            .isAfter(ZonedDateTime.now().minusMinutes(1)));
    // verify that WeatherDto is still correct:
    CurrentAndForecastAnswerDTO actualWeatherDtoCase3 =
        extractWeatherDtoFromLocation(locationCase3);
    Assertions.assertEquals(expectedWeatherDto, actualWeatherDtoCase3);
  }

  //    location = locationService.handleLocationSearch("münchen");
  //    locationAssertions(location, mockLocationApiResponseInnsbruck);
  //    Assertions.assertEquals(1, locationRepository.findAll().size());

  @Test
  void updateLocationWeatherTest() {
    CurrentAndForecastAnswer currentAndForecastAnswer = new CurrentAndForecastAnswer();
    currentAndForecastAnswer.setWeatherData(weatherDtoInnsbruck.toString().getBytes());

    Location location = getMockLocation();
    location.setWeather(currentAndForecastAnswer);

    currentAndForecastAnswer.setWeatherData("updated".getBytes());
    Location updatedLocation =
        locationService.updateLocationWeather(location, currentAndForecastAnswer);

    locationAssertions(updatedLocation, mockLocationApiResponseInnsbruck);
    Assertions.assertEquals(
        1, locationRepository.findAll().size()); // check that the updates are being persisted
    Assertions.assertEquals(1, currentAndForecastAnswerRepository.findAll().size());
    Assertions.assertEquals(currentAndForecastAnswer, updatedLocation.getWeather());
  }

  @Test
  void locationAlreadyPersistedTest() {
    Location location = getMockLocation();
    CurrentAndForecastAnswer weather = new CurrentAndForecastAnswer();
    weather.setWeatherData(weatherDtoInnsbruck.toString().getBytes());
    location.setWeather(currentAndForecastAnswerRepository.save(weather));
    locationRepository.save(location);

    Assertions.assertEquals(
        1, locationRepository.findAll().size(), "There was a problem in test setup");
    Assertions.assertEquals(
        1,
        currentAndForecastAnswerRepository.findAll().size(),
        "There was a problem in test setup");

    Assertions.assertTrue(
        locationService.locationAlreadyPersisted(mockLocationApiResponseInnsbruck));
    location =
        locationRepository.findLocationById(
            new LocationId(
                mockLocationApiResponseInnsbruck.latitude(),
                mockLocationApiResponseInnsbruck.longitude()));
    Assertions.assertEquals(weather.getId(), location.getWeather().getId());
  }

  @Test
  void locationHasUpToDateWeatherDataTest() {
    // Weather requests with timestamps older than the current full hour should return false.
    Location oldWeatherLocation = new Location();
    CurrentAndForecastAnswer oldWeather =
        currentAndForecastAnswerRepository.save(new CurrentAndForecastAnswer());
    ZonedDateTime now = ZonedDateTime.now();
    oldWeather.setTimestampLastCall(
        now.minusMinutes(now.getMinute())
            .minusSeconds(now.getSecond())
            .minusNanos(now.getNano())
            .minusMinutes(
                1)); // set the old weather timestamp one minute before the current full hour
    oldWeather = currentAndForecastAnswerRepository.save(oldWeather);
    oldWeatherLocation.setWeather(oldWeather);
    Assertions.assertFalse(locationService.locationHasUpToDateWeatherData(oldWeatherLocation));

    // Weather requests with timestamps within the current full hour should return true..
    Location newWeatherLocation = new Location();
    CurrentAndForecastAnswer newWeather =
        currentAndForecastAnswerRepository.save(
            new CurrentAndForecastAnswer()); // timestamp for new weather entities is automatically
    // set to ZonedDateTime.now()
    newWeatherLocation.setWeather(newWeather);
    Assertions.assertTrue(locationService.locationHasUpToDateWeatherData(newWeatherLocation));
  }

  @Test
  void getLocationTest() {
    Location location = getMockLocation();
    CurrentAndForecastAnswer weather = new CurrentAndForecastAnswer();
    weather.setWeatherData(mockLocationApiResponseInnsbruck.toString().getBytes());
    location.setWeather(currentAndForecastAnswerRepository.save(weather));
    locationRepository.save(location);

    Assertions.assertEquals(
        1, locationRepository.findAll().size(), "There was a problem in the test setup");

    Assertions.assertEquals(
        location.getId(), locationService.getLocation(mockLocationApiResponseInnsbruck).getId());
  }
}
