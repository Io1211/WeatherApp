package at.qe.skeleton.internal.ui.beans;

import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import at.qe.skeleton.external.model.location.LocationAnswerDTO;
import at.qe.skeleton.external.services.GeocodingApiRequestService;
import at.qe.skeleton.external.services.WeatherApiRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import at.qe.skeleton.internal.model.Location;
import at.qe.skeleton.internal.services.*;
import at.qe.skeleton.internal.services.utils.FailedJsonToDtoMappingException;
import at.qe.skeleton.internal.services.utils.FailedToSerializeDTOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Demonstrates the working api and what the raw request data would look like <br>
 * <br>
 * This class is part of the skeleton project provided for students of the course "Software
 * Architecture" offered by Innsbruck University.
 */
@Component
@Scope("view")
public class WeatherApiDemoBean {

  @Autowired private CurrentAndForecastAnswerService currentAndForecastAnswerService;

  @Autowired private LocationService locationService;

  private String currentWeather;

  private String searchedWeather;

  private String locationSearchInput;

  private double latitude;

  private double longitude;
  @Autowired private GeocodingApiRequestService geocodingApiRequestService;
  private String locationSearchInput;
  private static final Logger LOGGER = LoggerFactory.getLogger(WeatherApiDemoBean.class);

  private LocationAnswerDTO locationAnswerDTO;

  // this definitely gets checked by jsf after it is set to true. still the component with the
  // weather ist not loading...
  private boolean isLocationAnswerDTOReady = false;

  public boolean getIsLocationAnswerDTOReady() {
    return isLocationAnswerDTOReady;
  }

  public void setLocationSearchInput(String locationSearchInput) {
    this.locationSearchInput = locationSearchInput;
  }

  public void performLocationSearch()
      throws FailedToSerializeDTOException, FailedJsonToDtoMappingException {
    Location location = locationService.handleLocationSearch(locationSearchInput);
    CurrentAndForecastAnswerDTO weather =
        currentAndForecastAnswerService.deserializeDTO(location.getWeather().getWeatherData());
    this.latitude = location.getId().getLatitude();
    this.longitude = location.getId().getLongitude();
    StringBuilder body = new StringBuilder();
    body.append("City: %s<br>".formatted(location.getCity()));
    body.append(
        "Location: Lon - %s\tLat - %s<br>"
            .formatted(location.getId().getLongitude(), location.getId().getLatitude()));
    body.append(
        "Weather: Lon - %s\tLat - %s<br>".formatted(weather.longitude(), weather.latitude()));
    body.append("Description: %s<br>".formatted(weather.currentWeather().weather().description()));
    body.append("Title      : %s<br><br>".formatted(weather.currentWeather().weather().title()));
    this.searchedWeather = body.toString();
    // todo: add logic for handling errors in ui
  }

  public String getSearchedWeather() {
    return searchedWeather;
  }

  public void setSearchedWeather(String searchedWeather) {
    this.searchedWeather = searchedWeather;
  }

    public String getSunsetDateTime() {
        Instant sunsetInstant = this.currentAndForecastAnswerDTO.currentWeather().sunset();
        String apiResponseTimezone = this.currentAndForecastAnswerDTO.timezone();
        ZoneId utcZoneId = ZoneId.of(apiResponseTimezone);
        ZonedDateTime sunsetInDesiredZone = sunsetInstant.atZone(utcZoneId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return sunsetInDesiredZone.format(formatter);

  public LocationAnswerDTO getLocationAnswerDTO() {
    return locationAnswerDTO;
  }

  public CurrentAndForecastAnswerDTO getCurrentAndForecastAnswerDTO() {
    return currentAndForecastAnswerDTO;
  }

  public String getLocationSearchInput() {
    return locationSearchInput;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }
}

// todo: introduce error handling
// todo: write tests
