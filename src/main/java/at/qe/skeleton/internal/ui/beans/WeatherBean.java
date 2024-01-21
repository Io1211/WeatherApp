package at.qe.skeleton.internal.ui.beans;

import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import at.qe.skeleton.external.model.currentandforecast.misc.DailyWeatherDTO;
import at.qe.skeleton.external.model.currentandforecast.misc.HourlyWeatherDTO;
import at.qe.skeleton.internal.model.Favorite;
import at.qe.skeleton.internal.model.Location;
import at.qe.skeleton.internal.services.*;
import at.qe.skeleton.internal.services.exceptions.FailedApiRequest;
import at.qe.skeleton.internal.services.exceptions.GeocodingApiReturnedEmptyListException;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Responsible for retrieving and preprocessing all weather data for the UI. 
 * To retrieve the weather data, the bean can perform a location search using the location search api.
 * Additionally, a user can add (toggle) favourites which will be saved to the user.     
 * 
 * Based on 
 * @see WeatherApiDemoBean
 */
@Component
@Scope("view")
public class WeatherBean {

  @Autowired private CurrentAndForecastAnswerService currentAndForecastAnswerService;
  @Autowired private LocationService locationService;
  @Autowired private UserxService userxService;
  @Autowired private FavoriteService favoriteService;

  private static final Logger LOGGER = LoggerFactory.getLogger(WeatherBean.class);

  private String searchedWeather;
  private String locationSearchInput;
  private Location location;
  private CurrentAndForecastAnswerDTO weatherDTO;
  private boolean isLocationAnswerDTOReady = false;


  /**
   * Performs a location search using the provided location search input.
   * Error messages will be logged and displayed in case of failed requests.
   * If the request is sucessfull, the wether data blob will be deserialized and converted to an weatherDTO.
   * Lastly isLocationAnswerDTOReady is set to true to confirm the state if the weatherDTO. 
   */
  public void performLocationSearch() {
    try {
      this.location = locationService.handleLocationSearch(locationSearchInput);
    } catch (FailedApiRequest e) {
      FacesContext.getCurrentInstance()
          .addMessage(
              null,
              new FacesMessage(
                  FacesMessage.SEVERITY_ERROR,
                  "There was an error in an API request: ",
                  e.getMessage()));
      LOGGER.error(e.getMessage());
      return;
    } catch (GeocodingApiReturnedEmptyListException e) {
      FacesContext.getCurrentInstance()
          .addMessage(
              "weatherForm:locationSearch",
              new FacesMessage(
                  FacesMessage.SEVERITY_INFO,
                  "",
                  "Sorry, we couldn't find a location with the name: `%s`".formatted(locationSearchInput)));
      return;
    }
    this.weatherDTO =
        currentAndForecastAnswerService.deserializeDTO(location.getWeather().getWeatherData());
    this.isLocationAnswerDTOReady = true;
  }

  // Todo: move this into the bean for the final location search
  /**
   * Toggles the favorite status for the current location.
   */
  public void toggleFavorite() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    var user = this.userxService.loadUser(auth.getName());

    var favorite = new Favorite();
    favorite.setLocation(this.location);

    this.favoriteService.toggleFavorite(user, favorite);
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
   * Gets the daily weather entries for four days, which is the current day and the next three days.
   *
   * @return The list of daily weather entries.
   */
  public List<DailyWeatherDTO> getDailyWeatherEntries() {
    return weatherDTO.dailyWeather().stream().limit(4).collect(Collectors.toList());
  }

  /**
   * Gets the hourly weather entries for the current and next 24 hours.
   *
   * @return The list of hourly weather entries.
   */
  public List<HourlyWeatherDTO> getHourlyWeatherEntries() {
    return weatherDTO.hourlyWeather().stream().limit(25).collect(Collectors.toList());
  }

  /**
   * Formats an instant timestamp to a date-time string using a given specified format.
   * Used to convert mutliple occurences of timestamps (Type Instant) in the weather details
   * table to the desired formats.
   *
   * @param timestamp Instant timestamp.
   * @param format Desired date-time format so e.g. "HH:mm" or "dd.MM.yyyy - HH:mm".
   * @return A formatted date-time string.
   */
  public static String formatInstantToDateTime(Instant timestamp, String format) {
    LocalDateTime localDateTime = LocalDateTime.ofInstant(timestamp, ZoneOffset.UTC);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
    return localDateTime.format(formatter);
  }

  /**
   * Converts a direction in metrological degrees to a cardinal direction string. 
   *
   * @param degrees Direction in metrological degrees, from 0 to 360.
   * @return a string for the cardinal direction
   */
  public static String degreesToCardinal(Double degrees) {
      String[] directions = {"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"};
      Double dir = ((degrees + 11.25) % 360) / 22.5;
      return directions[dir.intValue()];
  }

  public Long msToKmh(Double mps) {
    return Math.round(mps*3.6);
  }

  public boolean getIsLocationAnswerDTOReady() {
    return isLocationAnswerDTOReady;
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

