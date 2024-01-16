package at.qe.skeleton.internal.ui.beans;

import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import at.qe.skeleton.internal.model.Favorite;
import at.qe.skeleton.internal.model.Location;
import at.qe.skeleton.internal.services.*;
import at.qe.skeleton.internal.services.exceptions.FailedApiRequest;
import at.qe.skeleton.internal.services.exceptions.GeocodingApiReturnedEmptyListException;
import at.qe.skeleton.internal.ui.controllers.AutoCompleteController;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.primefaces.PrimeFaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

/**
 * Demonstrates the working api and what the raw request data would look like <br>
 * <br>
 * This class is part of the skeleton project provided for students of the course "Software
 * Architecture" offered by Innsbruck University.
 */
@Component
@Scope("session")
public class WeatherApiDemoBean {

  @Autowired private CurrentAndForecastAnswerService currentAndForecastAnswerService;

  @Autowired private LocationService locationService;

  @Autowired private UserxService userxService;

  @Autowired private FavoriteService favoriteService;

  @Autowired private AutoCompleteController autoCompleteController;

  private static final Logger LOGGER = LoggerFactory.getLogger(WeatherApiDemoBean.class);

  private String searchedWeather;

  private String locationSearchInput = "Wien, AT";

  private Location location;
  private CurrentAndForecastAnswerDTO weatherDTO;

  private boolean isLocationAnswerDTOReady = false;

  public boolean getIsLocationAnswerDTOReady() {
    return isLocationAnswerDTOReady;
  }

  public String getLocationSearchInput() {
    return locationSearchInput;
  }

  public void setLocationSearchInput(String locationSearchInput) {
    this.locationSearchInput = locationSearchInput;
  }

  @PostConstruct
  public String performLocationSearch() {
    try {
      this.location = locationService.handleLocationSearch(locationSearchInput);
    } catch (FailedApiRequest e) {
      FacesContext.getCurrentInstance()
          .addMessage(
              null,
              new FacesMessage(
                  FacesMessage.SEVERITY_ERROR,
                  "There was an error in an api request: ",
                  e.getMessage()));
      LOGGER.error(e.getMessage());
      return null;
    } catch (GeocodingApiReturnedEmptyListException e) {
      FacesContext.getCurrentInstance()
          .addMessage(
              "weatherForm:locationSearch",
              new FacesMessage(
                  FacesMessage.SEVERITY_INFO,
                  "",
                  "Sorry, we couldn't find a location with the name: %s"
                      .formatted(locationSearchInput)));
      return null;
    }
    this.weatherDTO =
        currentAndForecastAnswerService.deserializeDTO(location.getWeather().getWeatherData());
    return "/weatherForecast.xhtml?faces-redirect=true";
  }

  public boolean isWeatherForecastView() {
    FacesContext context = FacesContext.getCurrentInstance();
    String viewId = context.getViewRoot().getViewId();
    return "weatherForecast.xhtml".equals(viewId);
  }

  public String getSunsetString() {
    Instant sunsetInstant = this.weatherDTO.currentWeather().sunset();
    String apiResponseTimezone = this.weatherDTO.timezone();
    ZoneId utcZoneId = ZoneId.of(apiResponseTimezone);
    ZonedDateTime sunsetInDesiredZone = sunsetInstant.atZone(utcZoneId);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
    return sunsetInDesiredZone.format(formatter);
  }

  public String getLocationLabel() {
    if (location.getState() == null) {
      return String.format("%s, %s", location.getCity(), location.getCountry());
    }
    if (location.getCountry() == null) {
      return String.format("%s, %s", location.getCountry(), location.getState());
    }
    return String.format(
        "%s, %s, %s", location.getCity(), location.getCountry(), location.getState());
  }

  public Location getLocation() {
    return location;
  }

  public CurrentAndForecastAnswerDTO getWeatherDTO() {
    return weatherDTO;
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

  public void setWeatherDTO(CurrentAndForecastAnswerDTO weatherDTO) {
    this.weatherDTO = weatherDTO;
  }
}
