package at.qe.skeleton.internal.ui.controllers;

import at.qe.skeleton.external.model.location.LocationAnswerDTO;
import at.qe.skeleton.internal.helper.WarningHelper;
import at.qe.skeleton.internal.services.CurrentAndForecastAnswerService;
import at.qe.skeleton.internal.services.LocationService;
import at.qe.skeleton.internal.services.exceptions.FailedApiRequest;
import jakarta.faces.application.FacesMessage;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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
  WarningHelper warningHelper;

  private static final Logger LOGGER = LoggerFactory.getLogger(AutoCompleteController.class);

  public AutoCompleteController(
      LocationService locationService,
      CurrentAndForecastAnswerService currentAndForecastAnswerService,
      WarningHelper warningHelper) {
    this.locationService = locationService;
    this.currentAndForecastAnswerService = currentAndForecastAnswerService;
    this.warningHelper = warningHelper;
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
    try {
      String queryLowerCase = query.toLowerCase();
      List<LocationAnswerDTO> locationAnswerDTOList = locationService.callApi(queryLowerCase, 5);
      return locationAnswerDTOList.stream().map(this::retrieveLocationName).toList();
    } catch (FailedApiRequest e) {
      warningHelper.addMessage("There was an error in an api request", FacesMessage.SEVERITY_WARN);
      LOGGER.error(e.getMessage());
      return new ArrayList<>();
    } catch (Exception e) {
      return new ArrayList<>();
    }
  }

  public String retrieveLocationName(LocationAnswerDTO locationDTO) {
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
