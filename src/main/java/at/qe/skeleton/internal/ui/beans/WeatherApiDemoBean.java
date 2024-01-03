package at.qe.skeleton.internal.ui.beans;

import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import at.qe.skeleton.internal.services.CurrentAndForecastAnswerService;
import at.qe.skeleton.internal.services.FailedJsonToDtoMappingException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  private static final Logger LOGGER = LoggerFactory.getLogger(WeatherApiDemoBean.class);

  private String currentWeather;

  private Long searchId;

  private String searchedWeather;
  // hard coded coordinates of innsbruck
  private double latitude = 47.2692;
  private double longitude = 11.4041;

  public void callApi() {
    currentAndForecastAnswerService.callApi(longitude, latitude);
  }

  public void findAll() throws FailedJsonToDtoMappingException {
    List<CurrentAndForecastAnswerDTO> currentAndForecastAnswerDTOS =
        currentAndForecastAnswerService.getAllCurrentAndForecastWeather();
    if (currentAndForecastAnswerDTOS.isEmpty()) {
      this.searchedWeather = "There are no weather entries present in the database at this moment";
      return;
    }
    StringBuilder renderedWeather = new StringBuilder();
    for (CurrentAndForecastAnswerDTO currentAndForecastAnswerDTO : currentAndForecastAnswerDTOS) {
      renderedWeather.append(
          "Weather id : %s<br>"
              .formatted(currentAndForecastAnswerDTO.currentWeather().weather().id()));
      renderedWeather.append(
          "Description: %s<br>"
              .formatted(currentAndForecastAnswerDTO.currentWeather().weather().description()));
      renderedWeather.append(
          "Title      : %s<br><br>"
              .formatted(currentAndForecastAnswerDTO.currentWeather().weather().title()));
    }
    this.searchedWeather = renderedWeather.toString();
  }

  public void findLastHour() throws FailedJsonToDtoMappingException {
    List<CurrentAndForecastAnswerDTO> weather =
        currentAndForecastAnswerService.getLastHourCurrentAndForecastWeather();
    if (weather == null) {
      this.searchedWeather = "No weather entry was found by that id";
      return;
    }
    StringBuilder renderedWeather = new StringBuilder();
    for (CurrentAndForecastAnswerDTO currentAndForecastAnswerDTO : weather) {
      renderedWeather.append(
          "Weather id : %s<br>"
              .formatted(currentAndForecastAnswerDTO.currentWeather().weather().id()));
      renderedWeather.append(
          "Description: %s<br>"
              .formatted(currentAndForecastAnswerDTO.currentWeather().weather().description()));
      renderedWeather.append(
          "Title      : %s<br><br>"
              .formatted(currentAndForecastAnswerDTO.currentWeather().weather().title()));
    }
    this.searchedWeather = renderedWeather.toString();
  }

  public String getSearchedWeather() {
    return searchedWeather;
  }

  public Long getSearchID() {
    return searchId;
  }

  public void setSearchID(Long searchID) {
    this.searchId = searchID;
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

  public double getLongitude() {
    return longitude;
  }
}
