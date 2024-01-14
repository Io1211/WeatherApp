package at.qe.skeleton.internal.ui.beans;

import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import at.qe.skeleton.internal.model.Favorite;
import at.qe.skeleton.internal.model.Location;
import at.qe.skeleton.internal.services.*;
import at.qe.skeleton.internal.services.exceptions.FailedApiRequest;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

  @Autowired private UserxService userxService;

  @Autowired private FavoriteService favoriteService;

  private static final Logger LOGGER = LoggerFactory.getLogger(WeatherApiDemoBean.class);

  private String searchedWeather;

  private String locationSearchInput;

  private double latitude;

  private double longitude;

  private Location location;

  public String getLocationSearchInput() {
    return locationSearchInput;
  }

  public void setLocationSearchInput(String locationSearchInput) {
    this.locationSearchInput = locationSearchInput;
  }

  public void performLocationSearch() {
    try {
      location = locationService.handleLocationSearch(locationSearchInput);
    } catch (FailedApiRequest e) {
      FacesContext.getCurrentInstance()
          .addMessage(
              "searchError",
              new FacesMessage(
                  FacesMessage.SEVERITY_ERROR,
                  "There was an error in an api request",
                  e.getMessage()));
      LOGGER.error(e.getMessage());
      return;
    }
    CurrentAndForecastAnswerDTO weather =
        currentAndForecastAnswerService.deserializeDTO(location.getWeather().getWeatherData());
    this.latitude = weather.latitude();
    this.longitude = weather.longitude();
    this.searchedWeather =
        "City: %s<br>".formatted(location.getCity())
            + "Weather: Lon - %s\tLat - %s<br>".formatted(weather.longitude(), weather.latitude())
            + "Description: %s<br>".formatted(weather.currentWeather().weather().description())
            + "Title      : %s<br><br>".formatted(weather.currentWeather().weather().title());
  }

  // Todo: move this into the bean for the final location search
  public void toggleFavorite() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    var user = this.userxService.loadUser(auth.getName());

    var favorite = new Favorite();
    favorite.setLocation(this.location);

    this.favoriteService.toggleFavorite(user, favorite);
  }

  public Boolean isFavorite() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    var user = this.userxService.loadUser(auth.getName());

    return this.favoriteService.isFavorite(user, this.location);
  }

  public String getSearchedWeather() {
    return searchedWeather;
  }

  public void setSearchedWeather(String searchedWeather) {
    this.searchedWeather = searchedWeather;
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
