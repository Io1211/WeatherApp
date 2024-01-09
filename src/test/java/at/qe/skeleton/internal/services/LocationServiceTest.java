package at.qe.skeleton.internal.services;

import static org.mockito.Mockito.*;

import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import at.qe.skeleton.external.model.location.LocationAnswerDTO;
import at.qe.skeleton.external.services.GeocodingApiRequestService;
import at.qe.skeleton.external.services.WeatherApiRequestService;
import at.qe.skeleton.internal.model.CurrentAndForecastAnswer;
import at.qe.skeleton.internal.model.Location;
import at.qe.skeleton.internal.model.LocationId;
import at.qe.skeleton.internal.repositories.CurrentAndForecastAnswerRepository;
import at.qe.skeleton.internal.repositories.LocationRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

@SpringBootTest
@WebAppConfiguration
public class LocationServiceTest {

  @Autowired LocationRepository locationRepository;

  @Autowired private LocationService locationService;
  @Autowired private CurrentAndForecastAnswerService currentAndForecastAnswerService;
  @Mock private static WeatherApiRequestService mockedWeatherApiRequestService;
  @Mock private static GeocodingApiRequestService mockedGeocodingRequestService;

  @Autowired private CurrentAndForecastAnswerRepository currentAndForecastAnswerRepository;

  private static List<LocationAnswerDTO> locationDtoListInnsbruck;
  private static LocationAnswerDTO locationDtoInnsbruck;
  private static List<LocationAnswerDTO> locationDtoListMunich;
  private static LocationAnswerDTO locationDtoMunich;

  private static ObjectMapper mapper;
  private static CurrentAndForecastAnswerDTO weatherDtoMunich;
  private static CurrentAndForecastAnswerDTO weatherDtoInnsbruck;

  // There is a bug concerning the PrecipitationDeserializer. The rain and snow fields in the
  // CurrentWeatherDTO are always set to null when serializing/deserializing the
  // CurrentAndForecastAnswerDTO.
  // Therefore, the tests currently use the mock api response from Innsbruck, where these fields are
  // expected to be null and not the one from Munich, were they are expected to return a non-null
  // value until this problem is resolved.
  @BeforeAll
  public static void setUp() throws IOException {
    String resources = "src/test/resources/";
    mapper = new ObjectMapper().findAndRegisterModules();

    // Innsbruck Location Mock
    locationDtoListInnsbruck =
        mapper.readValue(
            new File(resources + "GeocodingResponseInnsbruck.json"), new TypeReference<>() {});
    locationDtoInnsbruck = locationDtoListInnsbruck.get(0);

    // Innsbruck Weather Mock
    weatherDtoInnsbruck =
        mapper.readValue(
            new File(resources + "MockCurrentAndForecastAnswersInnsbruck.json"),
            CurrentAndForecastAnswerDTO.class);

    // München Location Mock DTO
    locationDtoListMunich =
        mapper.readValue(
            new File(resources + "GeocodingResponseMunich.json"), new TypeReference<>() {});
    locationDtoMunich = locationDtoListMunich.get(0);

    // Munich Weather Mock DTO
    weatherDtoMunich =
        mapper.readValue(
            new File(resources + "WeatherApiResponseMunich.json"),
            CurrentAndForecastAnswerDTO.class);
  }

  /**
   * Takes a LocationAnswerDTO and builds a Mock Location out of it.
   *
   * @param locationAnswerDTO
   * @return mock Location
   */
  public Location getMockLocation(LocationAnswerDTO locationAnswerDTO) {
    Location location = new Location();
    location.setId(locationAnswerDTO.latitude(), locationAnswerDTO.longitude());
    location.setCity(locationAnswerDTO.name());
    location.setState(locationAnswerDTO.state());
    location.setCountry(locationAnswerDTO.country());
    return location;
  }

  @AfterEach
  public void tearDown() {
    locationRepository.findAll().forEach(locationRepository::delete);
    currentAndForecastAnswerRepository
        .findAll()
        .forEach(currentAndForecastAnswerRepository::delete);
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
    when(mockedGeocodingRequestService.retrieveLocationsLonLat("Innsbruck", 1))
        .thenReturn(locationDtoListInnsbruck);
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
    Assertions.assertEquals(
        1, locationRepository.findAll().size(), "there should be exactly 1 Location Object in db.");
    // perform second search for the same Location Name
    Location locationCase2 = locationService.handleLocationSearch(searchString);
    // verify that there is still only 1 Location in db
    Assertions.assertEquals(
        1, locationRepository.findAll().size(), "there should be exactly 1 Location Object in db.");
    // verify that the mocked WeatherApiService only got called once
    verify(mockedWeatherApiRequestService, times(1))
        .retrieveCurrentAndForecastWeather(47.2654296, 11.3927685);
    // verify that the id of the Location object did not change
    Assertions.assertEquals(
        locationCase1.getId(), locationCase2.getId(), "the id should not have changed");
    // verify that the WeatherDto that is being stored is still the correct one.
    CurrentAndForecastAnswerDTO actualWeatherDtoCase2 =
        extractWeatherDtoFromLocation(locationCase2);
    Assertions.assertEquals(
        expectedWeatherDto,
        actualWeatherDtoCase2,
        "the WeatherDTO that has been deserialized from the db is not the same that has been stored after the api call");

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
            .isBefore(ZonedDateTime.now().minusMinutes(1)),
        "the timestamp should be older than 1 minute");

    Location locationCase3 = locationService.handleLocationSearch(searchString);
    // verify that the api has been called another time, in total two times.
    verify(mockedWeatherApiRequestService, times(2))
        .retrieveCurrentAndForecastWeather(47.2654296, 11.3927685);
    // verify that there is still only 1 Location in db
    Assertions.assertEquals(
        1,
        locationRepository.findAll().size(),
        "the database should still only contain 1 item, which has now been updated");
    // verify that the timestamp is now current:
    Assertions.assertTrue(
        locationCase3
            .getWeather()
            .getTimestampLastCall()
            .isAfter(ZonedDateTime.now().minusMinutes(1)),
        "the timestamp should be current");
    // verify that WeatherDto is still correct:
    CurrentAndForecastAnswerDTO actualWeatherDtoCase3 =
        extractWeatherDtoFromLocation(locationCase3);
    Assertions.assertEquals(
        expectedWeatherDto,
        actualWeatherDtoCase3,
        "the WeatherDTO that has been deserialized from the db is not the same that has been stored after the api call");
  }

  @Test
  void updateLocationWeatherTest() {
    byte[] weatherBlob = weatherDtoInnsbruck.toString().getBytes();
    CurrentAndForecastAnswer currentAndForecastAnswer = new CurrentAndForecastAnswer();
    currentAndForecastAnswer.setWeatherData(weatherBlob);

    Location location = getMockLocation(locationDtoInnsbruck);
    location.setWeather(currentAndForecastAnswer);

    // check that weather in Location Object has indeed been set to weatherBlob
    Assertions.assertEquals(weatherBlob, location.getWeather().getWeatherData());

    byte[] updatedWeatherBlob = "updated".getBytes();
    currentAndForecastAnswer.setWeatherData(updatedWeatherBlob);
    Location updatedLocation =
        locationService.updateLocationWeather(location, currentAndForecastAnswer);

    locationAssertions(updatedLocation, locationDtoInnsbruck);
    Assertions.assertEquals(
        1, locationRepository.findAll().size()); // check that the updates are being persisted
    Assertions.assertEquals(1, currentAndForecastAnswerRepository.findAll().size());
    // make sure that the updated Location has changed together with currentAndForeCastAnswer and
    // holds indeed the updated weatherBlob
    Assertions.assertEquals(currentAndForecastAnswer, updatedLocation.getWeather());
    Assertions.assertEquals(updatedWeatherBlob, updatedLocation.getWeather().getWeatherData());
  }

  @Test
  void locationAlreadyPersistedTest() {
    Location location = getMockLocation(locationDtoInnsbruck);
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

    Assertions.assertTrue(locationService.locationAlreadyPersisted(locationDtoInnsbruck));
    location =
        locationRepository.findLocationById(
            new LocationId(locationDtoInnsbruck.latitude(), locationDtoInnsbruck.longitude()));
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
    Location location = getMockLocation(locationDtoInnsbruck);
    CurrentAndForecastAnswer weather = new CurrentAndForecastAnswer();
    weather.setWeatherData(locationDtoListInnsbruck.toString().getBytes());
    location.setWeather(currentAndForecastAnswerRepository.save(weather));
    locationRepository.save(location);

    Assertions.assertEquals(
        1, locationRepository.findAll().size(), "There was a problem in the test setup");

    Assertions.assertEquals(
        location.getId(), locationService.getLocation(locationDtoInnsbruck).getId());
  }
}
