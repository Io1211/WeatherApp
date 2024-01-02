package at.qe.skeleton.internal.ui.beans;

import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import at.qe.skeleton.external.model.location.LocationAnswerDTO;
import at.qe.skeleton.external.services.GeocodingApiRequestService;
import at.qe.skeleton.external.services.WeatherApiRequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.annotation.PostConstruct;
import org.apache.commons.text.StringEscapeUtils;
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

  @Autowired private WeatherApiRequestService weatherApiRequestService;

  @Autowired private GeocodingApiRequestService geocodingApiRequestService;

  private static final Logger LOGGER = LoggerFactory.getLogger(WeatherApiDemoBean.class);

  private String currentWeather;

  // these were hard coded coordinates of innsbruck - i want to fill them now from the
  // weather_api_demo.xhtml
  private double latitude;
  private double longitude;
  private String locationSearchInput;



  public String getLocationSearchInput() {
    return locationSearchInput;
  }

  public void setLocationSearchInput(String locationSearchInput) {
    this.locationSearchInput = locationSearchInput;
  }

  public void performLocationSearch() {
    String input = this.locationSearchInput;
    LocationAnswerDTO locationAnswerDTO =
        this.geocodingApiRequestService.retrieveLocationLonLat(input);
    this.longitude = locationAnswerDTO.longitude();
    this.latitude = locationAnswerDTO.latitude();
  }

  public void performWeatherApiRequest() {
    try {
      CurrentAndForecastAnswerDTO answer =
          this.weatherApiRequestService.retrieveCurrentAndForecastWeather(
              getLatitude(), getLongitude());
      ObjectMapper mapper =
          new ObjectMapper().findAndRegisterModules().enable(SerializationFeature.INDENT_OUTPUT);
      String plainTextAnswer = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(answer);
      String escapedHtmlAnswer = StringEscapeUtils.escapeHtml4(plainTextAnswer);
      String escapedHtmlAnswerWithLineBreaks =
          escapedHtmlAnswer.replace("\n", "<br>").replace(" ", "&nbsp;");
      this.setCurrentWeather(escapedHtmlAnswerWithLineBreaks);

    } catch (final Exception e) {
      LOGGER.error("error in request", e);
    }
  }

  public void performLocationSearchAndWeatherRequest() {

    this.performLocationSearch();
    this.performWeatherApiRequest();
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

// todo: introduce error handling
// todo: write tests
