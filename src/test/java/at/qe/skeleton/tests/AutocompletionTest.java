package at.qe.skeleton.tests;

package at.qe.skeleton.tests;

import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import at.qe.skeleton.external.model.location.LocationAnswerDTO;
import at.qe.skeleton.internal.model.CurrentAndForecastAnswer;
import at.qe.skeleton.internal.model.Location;
import at.qe.skeleton.internal.services.CurrentAndForecastAnswerService;
import at.qe.skeleton.internal.services.LocationService;
import at.qe.skeleton.internal.services.LocationServiceTest;
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
import java.util.List;
import java.time.ZoneId;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class AutocompletionTest {
  // todo: add tests

  /*todo: Testfall: Berlin, DE, null
   * -> null sollte nicht angezeigt werden, wenn wir das so auswählen und suchen,
   * kommt irgend eine russische Stadt zurück
   *
   */
    @Mock LocationService locationService;
    @InjectMocks
    AutoCompleteController controller;

    List<LocationAnswerDTO> locationAnswerDTOList;

    @Before
    public void setUp(){
        //load the "autoCompletionApiResponseRom.json" and store it in locationAnswerDTOList;
    }
    @Test
    public void autoCompleteLocationTest () {
        String query = "Rom";
        // return the locationAnswerDTOLIst
        //when(locationService.callApi(query, 5)).thenReturn()

        //check wether the correct suggestions are presented (see screenshot)
    }



}
