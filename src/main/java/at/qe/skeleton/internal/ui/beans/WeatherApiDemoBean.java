package at.qe.skeleton.internal.ui.beans;

import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import at.qe.skeleton.external.model.location.LocationAnswerDTO;
import at.qe.skeleton.external.services.GeocodingApiRequestService;
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
  private String locationSearchInput;

  private static final Logger LOGGER = LoggerFactory.getLogger(WeatherApiDemoBean.class);

  private Location location;
  CurrentAndForecastAnswerDTO weatherDTO;

  private boolean isLocationAnswerDTOReady = false;

  public boolean getIsLocationAnswerDTOReady() {
    return isLocationAnswerDTOReady;
  }

  public void setLocationSearchInput(String locationSearchInput) {
    this.locationSearchInput = locationSearchInput;
  }

  public void performLocationSearch()
      throws FailedToSerializeDTOException, FailedJsonToDtoMappingException {
    this.location = locationService.handleLocationSearch(locationSearchInput);
    this.weatherDTO =
        currentAndForecastAnswerService.deserializeDTO(location.getWeather().getWeatherData());

    // todo: add logic for handling errors in ui
  }

  public String getSunsetDateTime() {
    Instant sunsetInstant = this.weatherDTO.currentWeather().sunset();
    String apiResponseTimezone = this.weatherDTO.timezone();
    ZoneId utcZoneId = ZoneId.of(apiResponseTimezone);
    ZonedDateTime sunsetInDesiredZone = sunsetInstant.atZone(utcZoneId);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
    return sunsetInDesiredZone.format(formatter);
  }

  public Location getLocation() {
    return location;
  }

  public CurrentAndForecastAnswerDTO getCurrentAndForecastAnswerDTO() {
    return weatherDTO;
  }

  public String getLocationSearchInput() {
    return locationSearchInput;
  }
}

// todo: introduce error handling
// todo: write tests
