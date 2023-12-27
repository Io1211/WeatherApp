package at.qe.skeleton.internal.services;

import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import at.qe.skeleton.internal.model.CurrentAndForecastAnswer;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.util.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@WebAppConfiguration
class CurrentAndForecastAnswerServiceTest {

    @Autowired
    CurrentAndForecastAnswerService currentAndForecastAnswerService;

    private final String dataFilePath = "src/test/resources/MockCurrentAndForecastAnswers.json";

    private CurrentAndForecastAnswerDTO loadMockResponseFromFile(String filePath) {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        try {
            String mockResponse = IOUtils.toString(new FileInputStream(filePath), StandardCharsets.UTF_8);
            return mapper.readValue(mockResponse, CurrentAndForecastAnswerDTO.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testSaveWeather() {
        CurrentAndForecastAnswerDTO answerDTO = loadMockResponseFromFile(this.dataFilePath);
        CurrentAndForecastAnswer savedAnswer = null;
        try {
            savedAnswer = currentAndForecastAnswerService.saveWeather(answerDTO);
        } catch (FailedToSerializeDTOException e) {
            e.getStackTrace();
        }
        Assertions.assertNotNull(savedAnswer, "Failed to save the mock DTO in the database");
        CurrentAndForecastAnswerDTO justCreatedDTO = null;
        try {
            justCreatedDTO = currentAndForecastAnswerService.findCurrentAndForecastWeatherById(Long.valueOf(savedAnswer.getId()));
        } catch (FailedJsonToDtoMappingException e) {
            e.getStackTrace();
        }
        Assertions.assertNotNull(justCreatedDTO, "No weather DTO found in the database by the id " + savedAnswer.getId());
        Assertions.assertEquals(answerDTO.latitude(), justCreatedDTO.latitude(), "The saved DTO doesn't have the correct latitude being saved");
        Assertions.assertEquals(answerDTO.longitude(), justCreatedDTO.longitude(), "The saved DTO doesn't have the correct longitude being saved");
        Assertions.assertEquals(answerDTO.timezone(), justCreatedDTO.timezone(), "The saved DTO doesn't have the correct timezone being saved");
        Assertions.assertEquals(answerDTO.timezoneOffset(), justCreatedDTO.timezoneOffset(), "The saved DTO doesn't have the correct timezoneOffset being saved");
        Assertions.assertEquals(answerDTO.currentWeather(), justCreatedDTO.currentWeather(), "The saved DTO doesn't have the correct currentWeather being saved");
        Assertions.assertEquals(answerDTO.minutelyPrecipitation(), justCreatedDTO.minutelyPrecipitation(), "The saved DTO doesn't have the correct minutelyPrecipitation being saved");
        Assertions.assertEquals(answerDTO.hourlyWeather(), justCreatedDTO.hourlyWeather(), "The saved DTO doesn't have the correct hourlyWeather being saved");
        Assertions.assertEquals(answerDTO.dailyWeather(), justCreatedDTO.dailyWeather(), "The saved DTO doesn't have the correct dailyWeather being saved");
        Assertions.assertEquals(answerDTO.alerts(), justCreatedDTO.alerts(), "The saved DTO doesn't have the correct alerts being saved");
    }

    @Test
    void testGetAllCurrentAndForecastWeather() {
        CurrentAndForecastAnswerDTO answerDTO = loadMockResponseFromFile(this.dataFilePath);
        List<CurrentAndForecastAnswerDTO> answerDTOList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            answerDTOList.add(answerDTO);
        }
        try {
            for (CurrentAndForecastAnswerDTO weatherDTO : answerDTOList) {
                currentAndForecastAnswerService.saveWeather(weatherDTO);
            }
        } catch (FailedToSerializeDTOException e) {
            e.getStackTrace();
        }
        List<CurrentAndForecastAnswerDTO> justCreatedDTOs = null;
        try {
            justCreatedDTOs = currentAndForecastAnswerService.getAllCurrentAndForecastWeather();
        } catch (FailedJsonToDtoMappingException e) {
            e.getStackTrace();
        }
        Assertions.assertNotNull(justCreatedDTOs, "Failed to retrieve saved DTOs from the database");
        Assertions.assertEquals(justCreatedDTOs.size(), answerDTOList.size(), "The count of retrieved DTOs doesn't match the count of initially stored ones");
        for (int i = 0; i < answerDTOList.size(); i++) {
            CurrentAndForecastAnswerDTO justCreatedDTO = justCreatedDTOs.get(i);
            CurrentAndForecastAnswerDTO controlDTO = answerDTOList.get(i);
            Assertions.assertEquals(controlDTO.latitude(), justCreatedDTO.latitude(), "The " + i + "th saved DTO doesn't have the correct latitude being saved");
            Assertions.assertEquals(controlDTO.longitude(), justCreatedDTO.longitude(), "The " + i + "th saved DTO doesn't have the correct longitude being saved");
            Assertions.assertEquals(controlDTO.timezone(), justCreatedDTO.timezone(), "The " + i + "th saved DTO doesn't have the correct timezone being saved");
            Assertions.assertEquals(controlDTO.timezoneOffset(), justCreatedDTO.timezoneOffset(), "The " + i + "th saved DTO doesn't have the correct timezoneOffset being saved");
            Assertions.assertEquals(controlDTO.currentWeather(), justCreatedDTO.currentWeather(), "The " + i + "th saved DTO doesn't have the correct currentWeather being saved");
            Assertions.assertEquals(controlDTO.minutelyPrecipitation(), justCreatedDTO.minutelyPrecipitation(), "The " + i + "th saved DTO doesn't have the correct minutelyPrecipitation being saved");
            Assertions.assertEquals(controlDTO.hourlyWeather(), justCreatedDTO.hourlyWeather(), "The " + i + "th saved DTO doesn't have the correct hourlyWeather being saved");
            Assertions.assertEquals(controlDTO.dailyWeather(), justCreatedDTO.dailyWeather(), "The " + i + "th saved DTO doesn't have the correct dailyWeather being saved");
            Assertions.assertEquals(controlDTO.alerts(), justCreatedDTO.alerts(), "The " + i + "th saved DTO doesn't have the correct alerts being saved");
        }
    }
}
