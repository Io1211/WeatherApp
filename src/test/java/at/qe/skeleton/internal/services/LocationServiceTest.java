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
import org.apache.commons.lang3.SerializationUtils;
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

  private static LocationAnswerDTO locationDtoInnsbruck;
  private static List<LocationAnswerDTO> locationDtoListInnsbruck;
  private static List<LocationAnswerDTO> locationDtoListMunich;
  private static LocationAnswerDTO locationDtoMunich;

  private static CurrentAndForecastAnswerDTO weatherDtoMunich;
  private static CurrentAndForecastAnswerDTO weatherDtoInnsbruck;

  @BeforeAll
  public static void setUp() throws IOException {
    String resources = "src/test/resources/";
    ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

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
   * @param locationAnswerDTO the locationDTO with which to creat a MockLocation for Testing.
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

  private CurrentAndForecastAnswerDTO extractWeatherDtoFromLocation(Location location) {
    byte[] blob = location.getWeather().getWeatherData();
    return currentAndForecastAnswerService.deserializeDTO(blob);
  }

  @Test
  public void handleLocationSearchTest() throws Exception {
    // inject mocked api services
    ReflectionTestUtils.setField(
        locationService, "geocodingApiRequestService", mockedGeocodingRequestService);
    ReflectionTestUtils.setField(
        currentAndForecastAnswerService,
        "weatherApiRequestService",
        mockedWeatherApiRequestService);

    // setup search and mocking parameters
    String searchedLocationName = "Munich";
    LocationAnswerDTO searchedMockLocation = locationDtoMunich;
    double searchedLatitude = searchedMockLocation.latitude();
    double searchedLongitude = searchedMockLocation.longitude();
    CurrentAndForecastAnswerDTO searchedMockWeather = weatherDtoMunich;

    // mocking the answers for innsbruck (without Precipitation)
    when(mockedGeocodingRequestService.retrieveLocationsLonLat(searchedLocationName, 1))
        .thenReturn(locationDtoListMunich);
    when(mockedWeatherApiRequestService.retrieveCurrentAndForecastWeather(
            searchedLatitude, searchedLongitude))
        .thenReturn(searchedMockWeather);

    // Case 1: Location doesnt exist yet in db
    Location locationCase1 = locationService.handleLocationSearch(searchedLocationName);
    Assertions.assertEquals(searchedLocationName, locationCase1.getCity());

    CurrentAndForecastAnswerDTO actualWeatherDtoCase1 =
        extractWeatherDtoFromLocation(locationCase1);
    Assertions.assertEquals(searchedMockWeather, actualWeatherDtoCase1);

    // Case 2: Location exists in db
    // verify that there is indeed 1 Location object in db
    Assertions.assertEquals(
        1, locationRepository.findAll().size(), "there should be exactly 1 Location Object in db.");
    // perform second search for the same Location Name
    Location locationCase2 = locationService.handleLocationSearch(searchedLocationName);
    // verify that there is still only 1 Location in db
    Assertions.assertEquals(
        1, locationRepository.findAll().size(), "there should be exactly 1 Location Object in db.");
    // verify that the mocked WeatherApiService only got called once
    verify(mockedWeatherApiRequestService, times(1))
        .retrieveCurrentAndForecastWeather(
            searchedMockLocation.latitude(), searchedMockLocation.longitude());
    // verify that the id of the Location object did not change
    Assertions.assertEquals(
        locationCase1.getId(), locationCase2.getId(), "the id should not have changed");
    // verify that the WeatherDto that is being stored is still the correct one.
    CurrentAndForecastAnswerDTO actualWeatherDtoCase2 =
        extractWeatherDtoFromLocation(locationCase2);
    Assertions.assertEquals(
        searchedMockWeather,
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

    Location locationCase3 = locationService.handleLocationSearch(searchedLocationName);
    // verify that the api has been called another time, in total two times.
    verify(mockedWeatherApiRequestService, times(2))
        .retrieveCurrentAndForecastWeather(
            searchedMockLocation.latitude(), searchedMockLocation.longitude());
    // verify that there is still only 1 Location in db
    Assertions.assertEquals(
        1,
        locationRepository.findAll().size(),
        "the database should still only contain 1 location entity, which has now been updated");
    // verify that there is only 1 Weather entity in db (the old one should be deleted upon the
    // update)
    Assertions.assertEquals(
        1,
        currentAndForecastAnswerRepository.findAll().size(),
        "the database should only contain 1 weather entity. The old one should be deleted in the update");
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
        searchedMockWeather,
        actualWeatherDtoCase3,
        "the WeatherDTO that has been deserialized from the db is not the same that has been stored after the api call");
  }

  @Test
  void updateLocationWeatherTest() {
    CurrentAndForecastAnswerDTO searchedMockDTO = weatherDtoInnsbruck;
    LocationAnswerDTO searchedLocationDTO = locationDtoInnsbruck;
    byte[] serializedWeatherBlob = SerializationUtils.serialize(searchedMockDTO);
    CurrentAndForecastAnswer currentAndForecastAnswer = new CurrentAndForecastAnswer();
    currentAndForecastAnswer.setWeatherData(serializedWeatherBlob);
    Location location = getMockLocation(searchedLocationDTO);
    location.setWeather(currentAndForecastAnswer);

    // check that weather in Location Object has indeed been set to weatherBlob
    Assertions.assertEquals(serializedWeatherBlob, location.getWeather().getWeatherData());

    // update CurrentAndForecastAnswer
    byte[] updatedSerializedWeatherBlob = SerializationUtils.serialize("updated");
    currentAndForecastAnswer.setWeatherData(updatedSerializedWeatherBlob);
    currentAndForecastAnswer.setWeatherData(updatedSerializedWeatherBlob);
    Location updatedLocation =
        locationService.updateLocationWeather(location, currentAndForecastAnswer);

    locationAssertions(updatedLocation, searchedLocationDTO);
    Assertions.assertEquals(
        1, locationRepository.findAll().size()); // check that the updates are being persisted
    Assertions.assertEquals(1, currentAndForecastAnswerRepository.findAll().size());
    // make sure that the updated Location has changed together with currentAndForeCastAnswer and
    // holds indeed the updated weatherBlob
    Assertions.assertEquals(currentAndForecastAnswer, updatedLocation.getWeather());
    Assertions.assertEquals(
        updatedSerializedWeatherBlob, updatedLocation.getWeather().getWeatherData());
  }

  @Test
  void locationAlreadyPersistedTest() {
    LocationAnswerDTO searchedLocationDTO = locationDtoMunich;

    Location location = getMockLocation(searchedLocationDTO);
    CurrentAndForecastAnswer weather = new CurrentAndForecastAnswer();
    weather.setWeatherData(SerializationUtils.serialize(searchedLocationDTO));
    location.setWeather(currentAndForecastAnswerRepository.save(weather));
    locationRepository.save(location);

    Assertions.assertEquals(
        1,
        locationRepository.findAll().size(),
        "There should be exactly 1 location entity in the database at this point");
    Assertions.assertEquals(
        1,
        currentAndForecastAnswerRepository.findAll().size(),
        "There should be exactly 1 weather entity in the database at this point");

    Assertions.assertTrue(locationService.locationAlreadyPersisted(searchedLocationDTO));
    location =
        locationRepository.findLocationById(
            new LocationId(searchedLocationDTO.latitude(), searchedLocationDTO.longitude()));
    locationAssertions(location, searchedLocationDTO);
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

    // Weather requests with timestamps within the current full hour should return true.
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
    LocationAnswerDTO searchedLocationDTO = locationDtoMunich;
    CurrentAndForecastAnswerDTO searchedWeatherDTO = weatherDtoMunich;

    Location location = getMockLocation(searchedLocationDTO);
    CurrentAndForecastAnswer weather = new CurrentAndForecastAnswer();
    weather.setWeatherData(SerializationUtils.serialize(searchedWeatherDTO));
    location.setWeather(currentAndForecastAnswerRepository.save(weather));
    locationRepository.save(location);

    Assertions.assertEquals(
        1,
        locationRepository.findAll().size(),
        "There should be exactly one location in the database at this point");

    locationAssertions(location, searchedLocationDTO);
  }
}
