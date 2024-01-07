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

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@WebAppConfiguration
public class LocationServiceTest {

  @Autowired LocationRepository locationRepository;

  @Autowired LocationService locationService;
  @Autowired CurrentAndForecastAnswerService currentAndForecastAnswerService;
  @Mock private static WeatherApiRequestService mockedWeatherApiRequestService;
  @Mock private static GeocodingApiRequestService mockedGeocodingRequestService;

  @Autowired CurrentAndForecastAnswerRepository currentAndForecastAnswerRepository;

  private static LocationAnswerDTO mockLocationApiResponseInnsbruck;

  private static CurrentAndForecastAnswerDTO mockWeatherApiResponseInnsbruck;
  private static ObjectMapper mapper;
  private static CurrentAndForecastAnswerDTO weatherDtoMunich;
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
    mockWeatherApiResponseInnsbruck =
        mapper.readValue(
            new File(resources + "MockCurrentAndForecastAnswersInnsbruck.json"),
            CurrentAndForecastAnswerDTO.class);

    // München Location Mock DTO & String
    _api =
        mapper.readValue(
            new File(resources + "GeocodingResponseMunich.json"), new TypeReference<>() {});
    locationDtoMunich = _api.get(0);

    // Munich Weather Mock DTO & String
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
    String searchString = "München";
    String encodedSearchString = URLEncoder.encode(searchString, StandardCharsets.UTF_8);

    // 1. Location doesnt exist yet in db
    // inject mocked api services
    ReflectionTestUtils.setField(
        locationService, "geocodingApiRequestService", mockedGeocodingRequestService);
    ReflectionTestUtils.setField(
        currentAndForecastAnswerService,
        "weatherApiRequestService",
        mockedWeatherApiRequestService);

    // prepare mock-answers
    when(mockedGeocodingRequestService.retrieveLocationLonLat("München"))
        .thenReturn(locationDtoMunich);
    when(mockedWeatherApiRequestService.retrieveCurrentAndForecastWeather(48.1371079, 11.5753822))
        .thenReturn(weatherDtoMunich);
    // actual call
    Location location = locationService.handleLocationSearch("München");
    // Assertions
    Assertions.assertEquals("Munich", location.getCity());
    CurrentAndForecastAnswerDTO expectedWeatherDto = weatherDtoMunich;
    byte[] blob = location.getWeather().getWeatherData();
    CurrentAndForecastAnswerDTO actualWeatherDtoStored =
        currentAndForecastAnswerService.deserializeDTO(blob);
    Assertions.assertEquals(expectedWeatherDto, actualWeatherDtoStored);

    //    // 2. Location should now exist in db
    //    // todo: test if location exists in db

    //    locationAssertions(location, mockLocationApiResponseInnsbruck);
    //    Assertions.assertEquals(1, locationRepository.findAll().size());
    //
    //    // 3. the location exists but the weather data is not up-to-date (set up manually):
    //    CurrentAndForecastAnswer oldWeather = location.getWeather();
    //    oldWeather.setTimestampLastCall(ZonedDateTime.now().minusHours(2));
    //    locationService.updateLocationWeather(location, oldWeather);
    //    Assertions.assertTrue( // check that the weather has been back-set successfully
    //
    // location.getWeather().getTimestampLastCall().isBefore(ZonedDateTime.now().minusMinutes(1)));
    //
    //    location = locationService.handleLocationSearch("münchen");
    //    locationAssertions(location, mockLocationApiResponseInnsbruck);
    //    Assertions.assertEquals(1, locationRepository.findAll().size());
  }

  @Test
  void updateLocationWeatherTest() {
    CurrentAndForecastAnswer currentAndForecastAnswer = new CurrentAndForecastAnswer();
    currentAndForecastAnswer.setWeatherData(mockWeatherApiResponseInnsbruck.toString().getBytes());

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
    weather.setWeatherData(mockLocationApiResponseInnsbruck.toString().getBytes());
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
