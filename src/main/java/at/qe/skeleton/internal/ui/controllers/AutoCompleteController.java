package at.qe.skeleton.internal.ui.controllers;

import at.qe.skeleton.external.model.location.LocationAnswerDTO;
import at.qe.skeleton.external.services.GeocodingApiRequestService;
import at.qe.skeleton.external.services.WeatherApiRequestService;
import at.qe.skeleton.internal.services.CurrentAndForecastAnswerService;
import at.qe.skeleton.internal.services.LocationService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/*
 * Handles the Location Auto Completion with a call to Geocoding Api.
 * here is an exemplary by Primefaces docs: http://www.primefaces.org:8080/showcase/ui/input/autoComplete.xhtml?jfwid=9a08e
 *
 *
 */
@Component
@Scope("view")
public class AutoCompleteController {
  LocationService locationService;
  CurrentAndForecastAnswerService currentAndForecastAnswerService;

  public AutoCompleteController(
      LocationService locationService,
      CurrentAndForecastAnswerService currentAndForecastAnswerService) {
    this.locationService = locationService;
    this.currentAndForecastAnswerService = currentAndForecastAnswerService;
  }

  /**
   * Finds Suggestions for the Location based on the Query String. returns up to 5 Suggestions. In
   * the Return Strings the state or country may be omitted if the actual answer of the Geocoding
   * API does not deliver a valid value for these attributes.
   *
   * @param query The Search String coming from the User in the Search Mask
   * @return A List of Strings representing the Location-name Suggestions for the Auto-Completion,
   *     including name, country and state
   */
  public List<String> autoCompleteLocation(String query) {
    String queryLowerCase = query.toLowerCase();
    List<LocationAnswerDTO> locationAnswerDTOList = locationService.callApi(queryLowerCase, 5);

    return locationAnswerDTOList.stream().map(this::retrieveLocationName).toList();
  }

  private String retrieveLocationName(LocationAnswerDTO locationDTO) {
    if (locationDTO.state() == null) {
      return String.format("%s, %s", locationDTO.name(), locationDTO.country());
    }
    if (locationDTO.country() == null) {
      return String.format("%s, %s", locationDTO.name(), locationDTO.state());
    }
    return String.format(
        "%s, %s, %s", locationDTO.name(), locationDTO.country(), locationDTO.state());
  }
}
