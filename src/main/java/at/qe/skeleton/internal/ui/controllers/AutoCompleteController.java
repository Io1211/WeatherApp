package at.qe.skeleton.internal.ui.controllers;

import at.qe.skeleton.external.model.location.LocationAnswerDTO;
import at.qe.skeleton.external.services.GeocodingApiRequestService;
import at.qe.skeleton.external.services.WeatherApiRequestService;
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
  GeocodingApiRequestService geocodingApiRequestService;
  WeatherApiRequestService weatherApiRequestService;

  // todo: it should only depict one value now, since we pick from autocomplete now.
  // todo: dont store the responses
  // todo: refactor WeatherApiDemoBean so that it fits the purpose
  // todo: if state is null it should not show that.
  public AutoCompleteController(
      GeocodingApiRequestService geocodingApiRequestService,
      WeatherApiRequestService weatherApiRequestService) {
    this.geocodingApiRequestService = geocodingApiRequestService;
    this.weatherApiRequestService = weatherApiRequestService;
  }

  public List<String> autoCompleteLocation(String query) {
    String queryLowerCase = query.toLowerCase();
    List<LocationAnswerDTO> locationAnswerDTOList =
        geocodingApiRequestService.retrieveLocationsLonLat(queryLowerCase);
    return locationAnswerDTOList.stream()
        .map(
            location ->
                String.format("%s, %s, %s", location.name(), location.country(), location.state()))
        .toList();
  }
}
