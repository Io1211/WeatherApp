package at.qe.skeleton.internal.services;

import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import at.qe.skeleton.external.model.location.LocationAnswerDTO;
import at.qe.skeleton.internal.model.CurrentAndForecastAnswer;
import at.qe.skeleton.internal.model.Location;
import at.qe.skeleton.internal.model.LocationId;
import at.qe.skeleton.internal.repositories.CurrentAndForecastAnswerRepository;
import at.qe.skeleton.internal.repositories.LocationRepository;
import at.qe.skeleton.internal.services.utils.FailedToSerializeDTOException;
import at.qe.skeleton.internal.services.utils.LocationSearch;
import at.qe.skeleton.internal.services.utils.LocationSearchBuilder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

@SpringBootTest
@WebAppConfiguration
public class LocationServiceTest {

  @Autowired LocationRepository locationRepository;

  @Autowired LocationService locationService;

  @Autowired CurrentAndForecastAnswerRepository currentAndForecastAnswerRepository;

  private LocationAnswerDTO mockLocationApi;
  private CurrentAndForecastAnswerDTO mockWeatherApi;

  @BeforeEach
  public void setUp() throws IOException {
    // Setup mock api responses and mock location search
    String resources = "src/test/resources/";
    ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
    List<LocationAnswerDTO> _api =
        mapper.readValue(
            new File(resources + "GeocodingResponseInnsbruck.json"), new TypeReference<>() {});
    this.mockLocationApi = _api.get(0);
    this.mockWeatherApi =
        mapper.readValue(
            new File(resources + "MockCurrentAndForecastAnswers.json"),
            CurrentAndForecastAnswerDTO.class);
  }

  @AfterEach
  public void tearDown() {
    locationRepository.findAll().forEach(locationRepository::delete);
    currentAndForecastAnswerRepository
        .findAll()
        .forEach(currentAndForecastAnswerRepository::delete);
  }

  public Location getMockLocation() {
    Location location = new Location();
    location.setId(this.mockLocationApi.latitude(), this.mockLocationApi.longitude());
    location.setCity(this.mockLocationApi.name());
    location.setState(this.mockLocationApi.state());
    location.setCountry(this.mockLocationApi.country());
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
  void handleLocationSearchTest() throws FailedToSerializeDTOException {
    // 3 cases covered:
    // 1. the location doesn't exist yet
    // 2. the location exists and the weather data is up-to-date
    // 3. the location exists but the weather data is not up-to-date

    // Setup mock location search
    LocationSearch mockLocationSearch =
        new LocationSearchBuilder()
            .setLocationName("Innsbruck")
            .setLocationAnswerDTO(this.mockLocationApi)
            .setCurrentAndForecastAnswerDTO(mockWeatherApi)
            .build();

    // 1.
    Location location = locationService.handleLocationSearch(mockLocationSearch);
    locationAssertions(location, mockLocationApi);

    // 2.
    location = locationService.handleLocationSearch(mockLocationSearch);
    locationAssertions(location, mockLocationApi);
    Assertions.assertEquals(1, locationRepository.findAll().size());

    // 3.
    CurrentAndForecastAnswer oldWeather = location.getWeather();
    oldWeather.setTimestampLastCall(ZonedDateTime.now().minusHours(2));
    locationService.updateLocationWeather(location, oldWeather);
    Assertions.assertTrue( // check that the weather has been back-set successfully
        location.getWeather().getTimestampLastCall().isBefore(ZonedDateTime.now().minusMinutes(1)));
    location = locationService.handleLocationSearch(mockLocationSearch);
    locationAssertions(location, mockLocationApi);
    Assertions.assertEquals(1, locationRepository.findAll().size());
  }

  @Test
  void updateLocationWeatherTest() {
    CurrentAndForecastAnswer currentAndForecastAnswer = new CurrentAndForecastAnswer();
    currentAndForecastAnswer.setWeatherData(this.mockWeatherApi.toString().getBytes());

    Location location = getMockLocation();
    location.setWeather(currentAndForecastAnswer);

    currentAndForecastAnswer.setWeatherData("updated".getBytes());
    Location updatedLocation =
        locationService.updateLocationWeather(location, currentAndForecastAnswer);

    locationAssertions(updatedLocation, this.mockLocationApi);
    Assertions.assertEquals(
        1, locationRepository.findAll().size()); // check that the updates are being persisted
    Assertions.assertEquals(1, currentAndForecastAnswerRepository.findAll().size());
    Assertions.assertEquals(currentAndForecastAnswer, updatedLocation.getWeather());
  }

  @Test
  void locationAlreadyPersistedTest() {
    Location location = getMockLocation();
    CurrentAndForecastAnswer weather = new CurrentAndForecastAnswer();
    weather.setWeatherData(this.mockLocationApi.toString().getBytes());
    location.setWeather(currentAndForecastAnswerRepository.save(weather));
    locationRepository.save(location);

    Assertions.assertEquals(
        1, locationRepository.findAll().size(), "There was a problem in test setup");
    Assertions.assertEquals(
        1,
        currentAndForecastAnswerRepository.findAll().size(),
        "There was a problem in test setup");

    Assertions.assertTrue(locationService.locationAlreadyPersisted(this.mockLocationApi));
    location =
        locationRepository.findLocationById(
            new LocationId(this.mockLocationApi.latitude(), this.mockLocationApi.longitude()));
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
    weather.setWeatherData(this.mockLocationApi.toString().getBytes());
    location.setWeather(currentAndForecastAnswerRepository.save(weather));
    locationRepository.save(location);

    Assertions.assertEquals(
        1, locationRepository.findAll().size(), "There was a problem in the test setup");

    Assertions.assertEquals(
        location.getId(), locationService.getLocation(this.mockLocationApi).getId());
  }
}
