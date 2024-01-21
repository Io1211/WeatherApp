package at.qe.skeleton.internal.ui.beans;

import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import at.qe.skeleton.external.model.currentandforecast.misc.DailyWeatherDTO;
import at.qe.skeleton.external.model.currentandforecast.misc.HourlyWeatherDTO;
import at.qe.skeleton.internal.model.Favorite;
import at.qe.skeleton.internal.model.Location;
import at.qe.skeleton.internal.model.DailyWeatherEntry;
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
 * Used to retrieve all weather data. Based on WeatherApiDemoBean.
 * 
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

  public boolean getIsLocationAnswerDTOReady() {
    return isLocationAnswerDTOReady;
  }

  public String getLocationSearchInput() {
    return locationSearchInput;
  }

  public void setLocationSearchInput(String locationSearchInput) {
    this.locationSearchInput = locationSearchInput;
  }

  public void performLocationSearch() {
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
      return;
    } catch (GeocodingApiReturnedEmptyListException e) {
      FacesContext.getCurrentInstance()
          .addMessage(
              "weatherForm:locationSearch",
              new FacesMessage(
                  FacesMessage.SEVERITY_INFO,
                  "",
                  "Sorry, we couldn't find a location with the name: `%s`"
                      .formatted(locationSearchInput)));
      return;
    }
    this.weatherDTO =
        currentAndForecastAnswerService.deserializeDTO(location.getWeather().getWeatherData());
    this.isLocationAnswerDTOReady = true;
  }

  public String getSunsetString() {
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

  /*
  public List<DailyWeatherEntry> getDailyWeatherEntries () {
    List<DailyWeatherEntry> dailyWeatherEntries = new ArrayList<>();
    
    for (Integer index = 0; index <= 3; index++) {
      dailyWeatherEntries.add(new DailyWeatherEntry(
        formatInstantToDate(weatherDTO.dailyWeather().get(index).sunrise(), ZoneId.of(weatherDTO.timezone())),
        formatInstantToHHMM(weatherDTO.dailyWeather().get(index).sunrise(), ZoneId.of(weatherDTO.timezone())),
        formatInstantToHHMM(weatherDTO.dailyWeather().get(index).sunset(), ZoneId.of(weatherDTO.timezone())),
        weatherDTO.dailyWeather().get(index).dailyTemperatureAggregation().dayTemperature(),
        weatherDTO.dailyWeather().get(index).dailyTemperatureAggregation().minimumDailyTemperature(),
        weatherDTO.dailyWeather().get(index).dailyTemperatureAggregation().maximumDailyTemperature(),
        // TO-DO: cannot set the feels like temp correctly??
        weatherDTO.dailyWeather().get(index).dailyTemperatureAggregation().dayTemperature(),
        weatherDTO.dailyWeather().get(index).windSpeed(),
        weatherDTO.dailyWeather().get(index).windDirection(),
        weatherDTO.dailyWeather().get(index).windGust(),
        weatherDTO.dailyWeather().get(index).summary(),
        weatherDTO.dailyWeather().get(index).probabilityOfPrecipitation(),
        weatherDTO.dailyWeather().get(index).rain(),
        weatherDTO.dailyWeather().get(index).snow()
      ));
    }
    return dailyWeatherEntries;
  }
   */

  public List<DailyWeatherDTO> getDailyWeatherEntries() {
    return weatherDTO.dailyWeather();
  }

  public List<HourlyWeatherDTO> getHourlyWeatherEntries() {
    return weatherDTO.hourlyWeather().stream().limit(24).collect(Collectors.toList());
  }

  public static String formatInstantToHHMM(Instant timestamp, ZoneId zoneId) {
    LocalDateTime localDateTime = LocalDateTime.ofInstant(timestamp, zoneId);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
    return localDateTime.format(formatter);
  }

  public static String formatInstantToHHMMWithoutTimezone(Instant timestamp) {
    LocalDateTime localDateTime = LocalDateTime.ofInstant(timestamp, ZoneOffset.UTC);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.: HH:mm");
    return localDateTime.format(formatter);
}

  public static String formatInstantToDate(Instant timestamp, ZoneId zoneId) {
    LocalDateTime localDateTime = LocalDateTime.ofInstant(timestamp, zoneId);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    return localDateTime.format(formatter);
  }

}
