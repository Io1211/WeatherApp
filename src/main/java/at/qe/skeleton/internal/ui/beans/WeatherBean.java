package at.qe.skeleton.internal.ui.beans;

import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import at.qe.skeleton.external.model.currentandforecast.misc.DailyWeatherDTO;
import at.qe.skeleton.external.model.currentandforecast.misc.HourlyWeatherDTO;
import at.qe.skeleton.internal.model.Location;
import at.qe.skeleton.internal.services.*;
import at.qe.skeleton.internal.services.exceptions.FailedApiRequest;
import at.qe.skeleton.internal.services.exceptions.GeocodingApiReturnedEmptyListException;
import at.qe.skeleton.internal.ui.controllers.IconController;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Responsible for retrieving and preprocessing all weather data for the UI. To retrieve the weather
 * data, the bean can perform a location search using the location search api. Additionally, a user
 * can add (toggle) favourites which will be saved to the user.
 */
@Component
@Scope("session")
public class WeatherBean {

  @Autowired private CurrentAndForecastAnswerService currentAndForecastAnswerService;
  @Autowired private LocationService locationService;
  @Autowired private UserxService userxService;
  @Autowired private FavoriteService favoriteService;
  @Autowired private IconController iconController;
  @Autowired private SessionInfoBean sessionInfoBean;

  private static final Logger LOGGER = LoggerFactory.getLogger(WeatherBean.class);

  private String locationSearchInput = "Vienna, AT";
  private Location location;
  private CurrentAndForecastAnswerDTO weatherDTO;

  /**
   * Performs a location search using the provided location search input. Error messages will be
   * logged and displayed in case of failed requests. If the request is successful, the weather data
   * blob will be deserialized and converted to an weatherDTO.
   */
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
    return "/weather_view.xhtml?faces-redirect=true";
  }

  /** Toggles the favorite status for the current location. */
  public void toggleFavorite() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    var user = this.userxService.loadUser(auth.getName());

    this.favoriteService.toggleFavorite(user, this.location);
  }

  /**
   * Checks whether the current location is marked as a favorite for the authenticated user.
   *
   * @return True if the location is a favorite; otherwise, false.
   */
  public Boolean isFavorite() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    var user = this.userxService.loadUser(auth.getName());

    return this.favoriteService.isFavorite(user, this.location);
  }

  /**
   * Gets the daily weather entries for the current and next 3 days (3+1 daily DTOs) or current and
   * next 8 days (8+1 daily DTOs). The amount of returned daily weatherDTO's depends on
   * sessionInfoBean.isUserPremium().
   *
   * @return The list of daily weather entries.
   */
  public List<DailyWeatherDTO> getDailyWeatherEntries() {
    return weatherDTO.dailyWeather().stream()
        .limit((sessionInfoBean.isUserPremium() ? 9 : 4))
        .toList();
  }

  /**
   * Gets the hourly weather entries for the current and next 24 hours (24+1 hourly DTOs) or current
   * and next 48 hours (48+1 hourly DTOs). The amount of returned daily weatherDTO's depends on
   * sessionInfoBean.isUserPremium().
   *
   * @return The list of hourly weather entries.
   */
  public List<HourlyWeatherDTO> getHourlyWeatherEntries() {
    return weatherDTO.hourlyWeather().stream()
        .limit((sessionInfoBean.isUserPremium() ? 49 : 25))
        .toList();
  }

  public String getLocationSearchInput() {
    return locationSearchInput;
  }

  public void setLocationSearchInput(String locationSearchInput) {
    this.locationSearchInput = locationSearchInput;
  }

  public Location getLocation() {
    return location;
  }

  public CurrentAndForecastAnswerDTO getWeatherDTO() {
    return weatherDTO;
  }

  public void setWeatherDTO(CurrentAndForecastAnswerDTO weatherDTO) {
    this.weatherDTO = weatherDTO;
  }
}
