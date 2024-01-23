package at.qe.skeleton.tests;

import at.qe.skeleton.UtilityClasses.FacesContextMocker;
import at.qe.skeleton.external.model.location.LocationAnswerDTO;

import at.qe.skeleton.internal.services.CurrentAndForecastAnswerService;
import at.qe.skeleton.internal.services.LocationService;

import at.qe.skeleton.internal.services.exceptions.FailedApiRequest;
import at.qe.skeleton.internal.services.exceptions.GeocodingApiReturnedEmptyListException;

import at.qe.skeleton.internal.ui.controllers.AutoCompleteController;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

public class AutocompletionTest {
  LocationService mockedLocationService;
  CurrentAndForecastAnswerService mockedCurrentAndForeCastAnswerService;
  AutoCompleteController controller;

  ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
  List<LocationAnswerDTO> locationAnswerDTOList;
  String resourcesPath = "src/test/resources/";
  private static final int LIMIT = 5;

  @Before
  public void setUp() throws Exception {
    // load the "autoCompletionApiResponseRom.json" and store it in locationAnswerDTOList;
    locationAnswerDTOList =
        mapper.readValue(
            new File(resourcesPath + "autoCompletionApiResponseRom.json"),
            new TypeReference<>() {});

    // set up the autoCompleteController with mocks
    mockedLocationService = Mockito.mock(LocationService.class);
    mockedCurrentAndForeCastAnswerService = Mockito.mock(CurrentAndForecastAnswerService.class);
    controller =
        new AutoCompleteController(mockedLocationService, mockedCurrentAndForeCastAnswerService);
  }

  @AfterEach
  public void reset() {
    Mockito.reset(mockedLocationService);
    Mockito.reset(mockedCurrentAndForeCastAnswerService);
  }

  // Rom fits very good as test case since it has one api-Answer which does not contain a "state"
  // value -> that means
  // it is returned as null and needs to be ommitted. The test should therefore display only name
  // and country
  // of that Location
  @Test
  public void autoCompleteLocationTest()
      throws FailedApiRequest, GeocodingApiReturnedEmptyListException {
    String query = "Rom";
    String queryToLowerCase = query.toLowerCase();

    when(mockedLocationService.callApi(queryToLowerCase, LIMIT)).thenReturn(locationAnswerDTOList);

    List<String> expectedAnswers = new ArrayList<>();
    expectedAnswers.add("Rome, IT, Lazio");
    expectedAnswers.add("Rom, FR, Nouvelle-Aquitaine");
    expectedAnswers.add("Rom, DE, Mecklenburg-Vorpommern");
    expectedAnswers.add("Rømø, DK, Region of Southern Denmark");
    expectedAnswers.add("Rom, NO");

    List<String> actualAnswers = controller.autoCompleteLocation(query);

    assertEquals(expectedAnswers, actualAnswers);
  }

  public void autoCompletionExceptionHandling() throws Exception {

    String notARealLocation = "this is not a real location";

    // instructing the mocked locationService to throw GeocodingApiReturnedEmptyListException
    when(mockedLocationService.callApi(notARealLocation, 5))
        .thenThrow(
            new GeocodingApiReturnedEmptyListException(
                "We couldnt find a Location called %s".formatted(notARealLocation)));

    Assertions.assertDoesNotThrow(
        () -> controller.autoCompleteLocation(notARealLocation),
        "AutoCompleteController should not throw anything when GeocodingApiReturnedEmptyListException is being "
            + "thrown by the api Service");

    List<String> listResultEmptyApiResponse = controller.autoCompleteLocation(notARealLocation);
    Assertions.assertTrue(listResultEmptyApiResponse.isEmpty());

    verify(mockedLocationService, times(2)).callApi(notARealLocation, 5);
  }

  @Test
  public void handleApiRequestException() throws Exception {
    String validLocationName = "nürnberg";
    String failedApiMessage = "Something is wrong with the API-Server";
    // using a utility Class in order to mock static FacesContext Object
    FacesContext facesContext = FacesContextMocker.mockFacesContext();

    when(mockedLocationService.callApi(validLocationName, 5))
        .thenThrow(new FailedApiRequest(failedApiMessage));

    // Assert that exception gets handled correctly by AutoCompleteController
    Assertions.assertDoesNotThrow(() -> controller.autoCompleteLocation(validLocationName));
    // making sure that mockedLocationService was really called:
    verify(mockedLocationService, times(1)).callApi(validLocationName, 5);
    // making sure that AutoCompleteController calls the FacesContext and adds a message to it
    verify(facesContext, times(1)).addMessage(anyString(), any(FacesMessage.class));
    facesContext.release();
  }
}
