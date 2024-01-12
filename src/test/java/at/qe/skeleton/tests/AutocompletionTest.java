package at.qe.skeleton.tests;

import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import at.qe.skeleton.external.model.location.LocationAnswerDTO;
import at.qe.skeleton.internal.model.CurrentAndForecastAnswer;
import at.qe.skeleton.internal.model.Location;
import at.qe.skeleton.internal.services.CurrentAndForecastAnswerService;
import at.qe.skeleton.internal.services.LocationService;
import at.qe.skeleton.internal.services.LocationServiceTest;
import at.qe.skeleton.internal.services.exceptions.FailedApiRequest;
import at.qe.skeleton.internal.ui.beans.WeatherApiDemoBean;
import at.qe.skeleton.internal.ui.controllers.AutoCompleteController;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;
import org.junit.Before;

import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.*;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.time.ZoneId;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AutocompletionTest {
  // todo: add tests

  /*todo: Testfall: Berlin, DE, null
   * -> null sollte nicht angezeigt werden, wenn wir das so auswählen und suchen,
   * kommt irgend eine russische Stadt zurück
   *
   */
  @Mock LocationService locationService;
  @InjectMocks AutoCompleteController controller;

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
  }

  // Rom fits very good as test case since it has one api-Answer which does not contain a "state"
  // value -> that means
  // it is returned as null and needs to be ommitted. The test should therefore display only name
  // and country
  // of that Location
  @Test
  public void autoCompleteLocationTest() throws FailedApiRequest {
    String query = "Rom";
    String queryToLowerCase = query.toLowerCase();

    when(locationService.callApi(queryToLowerCase, LIMIT)).thenReturn(locationAnswerDTOList);

    List<String> expectedAnswers = new ArrayList<>();
    expectedAnswers.add("Rome, IT, Lazio");
    expectedAnswers.add("Rom, FR, Nouvelle-Aquitaine");
    expectedAnswers.add("Rom, DE, Mecklenburg-Vorpommern");
    expectedAnswers.add("Rømø, DK, Region of Southern Denmark");
    expectedAnswers.add("Rom, NO");

    List<String> actualAnswers = controller.autoCompleteLocation(query);

    assertEquals(expectedAnswers, actualAnswers);
  }
}
