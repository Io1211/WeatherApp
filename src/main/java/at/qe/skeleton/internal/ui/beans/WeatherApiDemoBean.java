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

  private static final Logger LOGGER = LoggerFactory.getLogger(WeatherApiDemoBean.class);

  private List<LocationAnswerDTO> locationAnswerDTOS;


  private final Map<String, CurrentAndForecastAnswerDTO> locationNameWeatherMap = new HashMap<>();

  private String locationSearchInput;


  public String getLocationSearchInput() {
    return locationSearchInput;
  }

  public void setLocationSearchInput(String locationSearchInput) {
    this.locationSearchInput = locationSearchInput;
  }

  public void performLocationSearch() {
    String input = this.locationSearchInput;
     this.locationAnswerDTOS = this.geocodingApiRequestService.retrieveLocationsLonLat(input);
  }

  public void performLocationSearchAndWeatherRequest() {
    LOGGER.debug("Performing location search and weather request");
    this.performLocationSearch();
    LOGGER.debug("Number of location answers: {}", locationAnswerDTOS.size());
    this.locationAnswerDTOS.forEach(this::performWeatherApiRequest);
  }

  public void performWeatherApiRequest(LocationAnswerDTO locationAnswerDTO) {
    try {
      CurrentAndForecastAnswerDTO answer =
          this.weatherApiRequestService.retrieveCurrentAndForecastWeather(
              locationAnswerDTO.latitude(), locationAnswerDTO.longitude());

      this.setLocationAnswerWithCurrentWeather(locationAnswerDTO, answer);
      LOGGER.debug("Added entry to locationNameWeatherMap. Map size: {}", locationNameWeatherMap.size());

    } catch (final Exception e) {
      LOGGER.error("error in request", e);
    }
  }


  private void setLocationAnswerWithCurrentWeather(LocationAnswerDTO locationAnswer, CurrentAndForecastAnswerDTO currentAndForecastAnswerDTO) {
    // need name and country, otherwise it will only update the single entry in the set,
    // because all the names are "berlin", wherefore no new entry is added.

    //todo: if state is null we dont want to display it.
    String locationName = "%s, %s, %s".formatted(locationAnswer.name(), locationAnswer.country(), locationAnswer.state());
    this.locationNameWeatherMap.put(locationName, currentAndForecastAnswerDTO);
  }

  // need to convert the map to List of Entries in order to display it with primefaces ui.
  // as soon as Leo is ready with location & weatherResponse Entities we can use a list of those instead of a map in the Bean.
  public List<Map.Entry<String, CurrentAndForecastAnswerDTO>> getLocationNameWeatherMapEntryList() {
    //todo: somehow the entries of the table now add up, we only want the current ones..
    return new ArrayList<>(locationNameWeatherMap.entrySet());
  }

}


