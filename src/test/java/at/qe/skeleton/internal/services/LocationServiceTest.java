package at.qe.skeleton.internal.services;

import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import at.qe.skeleton.external.model.location.LocationAnswerDTO;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
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
  }

  @Test
  void handleLocationSearchTest() throws IOException, FailedToSerializeDTOException {
    // 3 cases covered:
    // 1. the location doesn't exist yet
    // 2. the location exists and the weather data is up-to-date
    // 3. the location exists but the weather data is not up-to-date

    // Setup mock api responses
    String resources = "src/test/resources/";
    ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
    List<LocationAnswerDTO> _api =
        mapper.readValue(
            new File(resources + "GeocodingResponseInnsbruck.json"), new TypeReference<>() {});
    LocationAnswerDTO mockLocationApi = _api.get(0);
    CurrentAndForecastAnswerDTO mockWeatherApi =
        mapper.readValue(
            new File(resources + "MockCurrentAndForecastAnswers.json"),
            CurrentAndForecastAnswerDTO.class);

    // 1.
    Location location =
        locationService.handleLocationSearch("Innsbruck", mockLocationApi, mockWeatherApi);
    locationAssertions(location, mockLocationApi);
    Assertions.assertTrue(
        location.getWeather().getTimestampLastCall().isAfter(ZonedDateTime.now().minusMinutes(1)));

    // 2.
    location = locationService.handleLocationSearch("Innsbruck", mockLocationApi, mockWeatherApi);
    locationAssertions(location, mockLocationApi);
    Assertions.assertEquals(1, locationRepository.findAll().size());
    Assertions.assertTrue(
        location.getWeather().getTimestampLastCall().isAfter(ZonedDateTime.now().minusMinutes(1)));

    // 3.
    CurrentAndForecastAnswer oldWeather = location.getWeather();
    oldWeather.setTimestampLastCall(ZonedDateTime.now().minusHours(2));
    location = locationService.updateLocationWeather(location, oldWeather);
    locationAssertions(location, mockLocationApi);
    Assertions.assertEquals(1, locationRepository.findAll().size());
    Assertions.assertTrue(
        location.getWeather().getTimestampLastCall().isBefore(ZonedDateTime.now().minusMinutes(1)));
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
}
