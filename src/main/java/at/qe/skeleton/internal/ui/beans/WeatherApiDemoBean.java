package at.qe.skeleton.internal.ui.beans;

import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import at.qe.skeleton.external.model.location.LocationAnswerDTO;
import at.qe.skeleton.external.services.GeocodingApiRequestService;
import at.qe.skeleton.external.services.WeatherApiRequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.text.StringEscapeUtils;
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

  private static final Logger LOGGER = LoggerFactory.getLogger(WeatherApiDemoBean.class);

  private List<LocationAnswerDTO> locationAnswerDTOS;


  private final Map<String, String> locationNameWeatherMap = new HashMap<>();

  private String locationSearchInput;

  // hardcoded limit - i.e. the number of locations in the API response
  // TODO: Maybe make it possible to show more than one result from the api
  //          -> in that case the result should not be stored in single LocationAnswerDTO but
  // multiple LocationAnswerDTOs

  public String getLocationSearchInput() {
    return locationSearchInput;
  }

  // an dieser Stelle sind umlaute noch umlaute (= unkodiert)
  public void setLocationSearchInput(String locationSearchInput) {
    this.locationSearchInput = locationSearchInput;

  }

  public void performLocationSearch() {
    String input = this.locationSearchInput;
     this.locationAnswerDTOS = this.geocodingApiRequestService.retrieveLocationsLonLat(input);
  }

  public void performLocationSearchAndWeatherRequest() {
    this.performLocationSearch();
    this.locationAnswerDTOS.forEach(this::performWeatherApiRequest);
  }

  public void performWeatherApiRequest(LocationAnswerDTO locationAnswerDTO) {
    try {
      CurrentAndForecastAnswerDTO answer =
          this.weatherApiRequestService.retrieveCurrentAndForecastWeather(
              locationAnswerDTO.latitude(), locationAnswerDTO.longitude());
      ObjectMapper mapper =
          new ObjectMapper().findAndRegisterModules().enable(SerializationFeature.INDENT_OUTPUT);
      String plainTextAnswer = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(answer);
      String escapedHtmlAnswer = StringEscapeUtils.escapeHtml4(plainTextAnswer);
      String escapedHtmlAnswerWithLineBreaks =
          escapedHtmlAnswer.replace("\n", "<br>").replace(" ", "&nbsp;");

      this.setLocationAnswerWithCurrentWeather(locationAnswerDTO, escapedHtmlAnswerWithLineBreaks);

    } catch (final Exception e) {
      LOGGER.error("error in request", e);
    }
  }


  private void setLocationAnswerWithCurrentWeather(LocationAnswerDTO locationAnswer, String currentWeather) {
    String locationName = locationAnswer.name();
    this.locationNameWeatherMap.put(locationName, currentWeather);
  }

  // need to convert the map to List of Entries in order to display it with primefaces ui.
  // as soon as Leo is ready with location & weatherResponse Entities we can use a list of those instead of a map in the Bean.
  public List<Map.Entry<String, String>> getLocationNameWeatherMapEntryList() {
    return new ArrayList<>(locationNameWeatherMap.entrySet());
  }

}


