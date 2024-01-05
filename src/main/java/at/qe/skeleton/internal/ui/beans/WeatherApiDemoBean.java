package at.qe.skeleton.internal.ui.beans;

import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import at.qe.skeleton.internal.model.Location;
import at.qe.skeleton.internal.services.CurrentAndForecastAnswerService;
import at.qe.skeleton.internal.services.FailedJsonToDtoMappingException;
import at.qe.skeleton.internal.services.FailedToSerializeDTOException;
import at.qe.skeleton.internal.services.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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

  public String getLocationSearchInput() {
    return locationSearchInput;
  }

  public void setLocationSearchInput(String locationSearchInput) {
    this.locationSearchInput = locationSearchInput;
  }

  public void performLocationSearch()
      throws FailedToSerializeDTOException, FailedJsonToDtoMappingException {
    Location location = locationService.handleLocationSearch(this.locationSearchInput, null, null);
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
  }

  public String getSearchedWeather() {
    return searchedWeather;
  }

  public void setSearchedWeather(String searchedWeather) {
    this.searchedWeather = searchedWeather;
  }

  public String getCurrentWeather() {
    return currentWeather;
  }

  public void setCurrentWeather(String currentWeather) {
    this.currentWeather = currentWeather;
  }

  public double getLatitude() {
    return latitude;
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
