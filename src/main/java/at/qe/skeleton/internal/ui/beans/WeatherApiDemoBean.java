package at.qe.skeleton.internal.ui.beans;

import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import at.qe.skeleton.external.model.location.LocationAnswerDTO;
import at.qe.skeleton.external.services.GeocodingApiRequestService;
import at.qe.skeleton.external.services.WeatherApiRequestService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.text.StringEscapeUtils;
import at.qe.skeleton.internal.services.CurrentAndForecastAnswerService;
import at.qe.skeleton.internal.services.FailedJsonToDtoMappingException;
import java.util.List;
import java.util.Optional;

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

  @Autowired private GeocodingApiRequestService geocodingApiRequestService;

  @Autowired private WeatherApiRequestService weatherApiRequestService;

  private static final Logger LOGGER = LoggerFactory.getLogger(WeatherApiDemoBean.class);

  private String currentWeatherPrintable;

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

  private Long searchId;

  private List<CurrentAndForecastAnswerDTO> currentAndForecastAnswerDTOsLastHour;

  private CurrentAndForecastAnswerDTO weatherApiResponse;

  public void callApi() {
    currentAndForecastAnswerService.callApi(longitude, latitude);
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
      LOGGER.info(
          "performing WeatherApiRequest for lon: %s, lat: %s"
              .formatted(this.longitude, this.latitude));
      weatherApiResponse =
          this.weatherApiRequestService.retrieveCurrentAndForecastWeather(
              getLatitude(), getLongitude());
    } catch (final Exception e) {
      // todo: add real error handling
      LOGGER.error("error in WeatherApiRequest", e);
    }
  }

  public void createPrintableWeatherApiResponse() throws JsonProcessingException {
    ObjectMapper mapper =
        new ObjectMapper().findAndRegisterModules().enable(SerializationFeature.INDENT_OUTPUT);
    String plainTextAnswer =
        mapper.writerWithDefaultPrettyPrinter().writeValueAsString(weatherApiResponse);
    String escapedHtmlAnswer = StringEscapeUtils.escapeHtml4(plainTextAnswer);
    String escapedHtmlAnswerWithLineBreaks =
        escapedHtmlAnswer.replace("\n", "<br>").replace(" ", "&nbsp;");
    this.setCurrentWeatherPrintable(escapedHtmlAnswerWithLineBreaks);
  }

  //    StringBuilder renderedWeather = new StringBuilder();
  //    for (CurrentAndForecastAnswerDTO currentAndForecastAnswerDTO : currentAndForecastAnswerDTOS)
  // {
  //      renderedWeather.append(
  //          "Weather id : %s<br>"
  //              .formatted(currentAndForecastAnswerDTO.currentWeather().weather().id()));
  //      renderedWeather.append(
  //          "Description: %s<br>"
  //              .formatted(currentAndForecastAnswerDTO.currentWeather().weather().description()));
  //      renderedWeather.append(
  //          "Title      : %s<br><br>"
  //              .formatted(currentAndForecastAnswerDTO.currentWeather().weather().title()));
  //    }
  //    this.searchedWeather = renderedWeather.toString();
  //  }

  public void findWeatherEntitiesStoredLastHour() throws FailedJsonToDtoMappingException {
    currentAndForecastAnswerDTOsLastHour =
        currentAndForecastAnswerService.getLastHourCurrentAndForecastWeather();
    if (currentAndForecastAnswerDTOsLastHour == null) {
      LOGGER.warn("no weather Entry was found by 'findLastHour'");
    }
    StringBuilder renderedWeather = new StringBuilder();
    for (CurrentAndForecastAnswerDTO currentAndForecastAnswerDTO :
        currentAndForecastAnswerDTOsLastHour) {
      renderedWeather.append(
          "long: %s, lat: %s<br>"
              .formatted(
                  currentAndForecastAnswerDTO.longitude(), currentAndForecastAnswerDTO.latitude()));
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
    LOGGER.info("findLastHour returned these weatherDtos: " + renderedWeather);
  }

  public Long getSearchID() {
    return searchId;
  }

  public void setSearchID(Long searchID) {
    this.searchId = searchID;
  }

  public void performLocationSearchAndWeatherRequest()
      throws FailedJsonToDtoMappingException, JsonProcessingException {
    performLocationSearch();
    // look if we have a recently stored CurrendAndForeCastAnswer Entity in the db
    findWeatherEntitiesStoredLastHour();
    Optional<CurrentAndForecastAnswerDTO> optionalWeatherAnswerDtoFromDb =
        currentAndForecastAnswerDTOsLastHour.stream()
            .filter(dto -> dto.latitude() == this.latitude && dto.longitude() == this.latitude)
            .findAny();
    if (optionalWeatherAnswerDtoFromDb.isPresent()) {
      this.weatherApiResponse = optionalWeatherAnswerDtoFromDb.get();
      LOGGER.info(
          "found recently stored CurrentAndForeCastAnswer Object for lat: %s & long: %s."
              .formatted(this.latitude, this.longitude));
    }
    // if we don´t have anything recently stored we make the api call for the current lat/long
    else {
      performWeatherApiRequest();
    }
    createPrintableWeatherApiResponse();
  }

  public String getCurrentWeatherPrintable() {
    return currentWeatherPrintable;
  }

  public void setCurrentWeatherPrintable(String currentWeather) {
    this.currentWeatherPrintable = currentWeather;
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
