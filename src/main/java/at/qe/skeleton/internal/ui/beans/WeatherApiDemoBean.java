package at.qe.skeleton.internal.ui.beans;

import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import at.qe.skeleton.external.model.location.LocationAnswerDTO;
import at.qe.skeleton.external.services.GeocodingApiRequestService;
import at.qe.skeleton.external.services.WeatherApiRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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

  @Autowired private WeatherApiRequestService weatherApiRequestService;

  @Autowired private GeocodingApiRequestService geocodingApiRequestService;
  private String locationSearchInput;
  private static final Logger LOGGER = LoggerFactory.getLogger(WeatherApiDemoBean.class);

  private LocationAnswerDTO locationAnswerDTO;
  private CurrentAndForecastAnswerDTO currentAndForecastAnswerDTO;

  public void performLocationSearchAndWeatherRequest() {
    performLocationSearch();
    performWeatherApiRequest(this.locationAnswerDTO);
  }

  public void performLocationSearch() {
    try {
      String input = this.locationSearchInput;
      List<LocationAnswerDTO> locationAnswerDTOList =
          this.geocodingApiRequestService.retrieveLocationsLonLat(input, 1);
      this.locationAnswerDTO = locationAnswerDTOList.get(0);
    } catch (final Exception e) {
      // todo: add real logic for handling errors in ui
      LOGGER.error("error in GeocodingAPI request");
    }
  }

  public void performWeatherApiRequest(LocationAnswerDTO locationAnswerDTO) {
    try {
      this.currentAndForecastAnswerDTO =
          this.weatherApiRequestService.retrieveCurrentAndForecastWeather(
              locationAnswerDTO.latitude(), locationAnswerDTO.longitude());
    } catch (final Exception e) {
      // todo: add real logic for handling errors in ui
      LOGGER.error("error in WeatherAPI request", e);
    }
  }

  public double getTemperatureInCelsius() {
    double tempInFahrenheit = this.currentAndForecastAnswerDTO.currentWeather().temperature();
    return (tempInFahrenheit - 30) * 5 / 9;
  }

  public

  public LocationAnswerDTO getLocationAnswerDTO() {
    return locationAnswerDTO;
  }

  public CurrentAndForecastAnswerDTO getCurrentAndForecastAnswerDTO() {
    return currentAndForecastAnswerDTO;
  }

  public String getLocationSearchInput() {
    return locationSearchInput;
  }

  public void setLocationSearchInput(String locationSearchInput) {
    this.locationSearchInput = locationSearchInput;
  }
}
