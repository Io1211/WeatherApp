package at.qe.skeleton.tests;

import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import at.qe.skeleton.external.model.location.LocationAnswerDTO;
import at.qe.skeleton.internal.model.CurrentAndForecastAnswer;
import at.qe.skeleton.internal.model.Location;
import at.qe.skeleton.internal.services.CurrentAndForecastAnswerService;
import at.qe.skeleton.internal.services.LocationService;
import at.qe.skeleton.internal.services.LocationServiceTest;
import at.qe.skeleton.internal.ui.beans.WeatherApiDemoBean;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;
import org.junit.Before;

import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.*;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.time.ZoneId;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LocationSearchTest {
  // todo: add tests
  @Mock LocationService locationServiceMock;
  @Mock CurrentAndForecastAnswerService currentAndForecastAnswerService;

  @InjectMocks WeatherApiDemoBean weatherApiDemoBean;

  LocationServiceTest locationServiceTest = new LocationServiceTest();
  static LocationAnswerDTO locationDtoInnsbruck;
  static CurrentAndForecastAnswerDTO weatherDtoInnsbruck;
  static ObjectMapper mapper;
  static final String RESOURCES = "src/test/resources/";

  @Before
  public void setup() throws Exception {
    mapper = new ObjectMapper().findAndRegisterModules();
    List<LocationAnswerDTO> locationAnswerDTOList =
        mapper.readValue(
            new File(RESOURCES + "GeocodingResponseInnsbruck.json"), new TypeReference<>() {});
    locationDtoInnsbruck = locationAnswerDTOList.get(0);

    weatherDtoInnsbruck =
        mapper.readValue(
            new File(RESOURCES + "MockCurrentAndForecastAnswersInnsbruck.json"),
            new TypeReference<>() {});
  }

  private Location getMockLocation(
      LocationAnswerDTO locationAnswerDTO, CurrentAndForecastAnswerDTO weatherDTO)
      throws Exception {
    Location location = new Location();
    // first create CurrentAndForeCastAnswer
    byte[] weatherBlob = mapper.writeValueAsBytes(locationAnswerDTO);
    CurrentAndForecastAnswer currentAndForecastAnswerInnsbruck = new CurrentAndForecastAnswer();
    currentAndForecastAnswerInnsbruck.setWeatherData(weatherBlob);
    currentAndForecastAnswerInnsbruck.setTimestampLastCall(ZonedDateTime.now());
    currentAndForecastAnswerInnsbruck.setId(1L);
    // Now create Location
    location.setId(locationAnswerDTO.latitude(), locationAnswerDTO.longitude());
    location.setCity(locationAnswerDTO.name());
    location.setState(locationAnswerDTO.state());
    location.setCountry(locationAnswerDTO.country());
    location.setWeather(currentAndForecastAnswerInnsbruck);
    return location;
  }

  @Test
  public void testPerformLocationAndWeatherSearch() throws Exception {
    String searchString = "Innsbruck";
    Location locationMock = getMockLocation(locationDtoInnsbruck, weatherDtoInnsbruck);

    // setting up mocks
    when(locationServiceMock.handleLocationAndWeatherSearch(searchString)).thenReturn(locationMock);
    when(currentAndForecastAnswerService.deserializeDTO(locationMock.getWeather().getWeatherData()))
        .thenReturn(weatherDtoInnsbruck);

    // preparing weatherApiDemoBean
    this.weatherApiDemoBean.setLocationSearchInput(searchString);
    this.weatherApiDemoBean.performLocationAndWeatherSearch();

    // the actual test
    verify(locationServiceMock, times(1)).handleLocationAndWeatherSearch(searchString);
    Assertions.assertEquals(weatherDtoInnsbruck, weatherApiDemoBean.getWeatherDTO());
  }

  @Test
  public void testGetSunsetDateTime() {
    Instant sunsetInstant = weatherDtoInnsbruck.currentWeather().sunset();
    String apiResponseTimezone = weatherDtoInnsbruck.timezone();
    this.weatherApiDemoBean.setWeatherDTO(weatherDtoInnsbruck);

    ZonedDateTime expectedSunsetInDesiredZone =
        sunsetInstant.atZone(ZoneId.of(apiResponseTimezone));
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
    String expected = expectedSunsetInDesiredZone.format(formatter);

    String result = weatherApiDemoBean.getSunsetString();

    assertEquals(expected, result);
  }
}
