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

  public void performLocationSearch() {
    Location location = null;
    try {
      location = locationService.getLocation(this.locationSearchInput);
      this.setLatitude(location.getId().getLatitude());
      this.setLongitude(location.getId().getLongitude());
    } catch (FailedJsonToDtoMappingException e) {
      throw new RuntimeException(e);
    } catch (FailedToSerializeDTOException e) {
      throw new RuntimeException(e);
    }
    CurrentAndForecastAnswerDTO weather = null;
    try {
      weather =
          currentAndForecastAnswerService.deserializeDTO(location.getWeather().getWeatherData());
    } catch (FailedJsonToDtoMappingException e) {
      throw new RuntimeException(e);
    }
    if (location == null || weather == null) {
      this.searchedWeather = "Failed to fetch location and weather data";
    }
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
