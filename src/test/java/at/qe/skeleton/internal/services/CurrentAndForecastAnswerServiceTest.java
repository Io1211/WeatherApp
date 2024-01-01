package at.qe.skeleton.internal.services;

import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import at.qe.skeleton.internal.model.CurrentAndForecastAnswer;
import at.qe.skeleton.internal.repositories.CurrentAndForecastAnswerRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.IntStream;

@SpringBootTest
@WebAppConfiguration
class CurrentAndForecastAnswerServiceTest {

    @Autowired
    CurrentAndForecastAnswerService currentAndForecastAnswerService;

    @Autowired
    CurrentAndForecastAnswerRepository currentAndForecastAnswerRepository;

    private final String dataFilePath = "src/test/resources/MockCurrentAndForecastAnswers.json";

    private void cleanDatabase() {
        List<CurrentAndForecastAnswer> entities = currentAndForecastAnswerRepository.findAll();
        entities.forEach(currentAndForecastAnswerRepository::delete);
    }

    private CurrentAndForecastAnswerDTO loadMockResponseFromFile(String filePath) {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        try {
            String mockResponse = IOUtils.toString(new FileInputStream(filePath), StandardCharsets.UTF_8);
            return mapper.readValue(mockResponse, CurrentAndForecastAnswerDTO.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkDTO(CurrentAndForecastAnswerDTO referenceDTO, CurrentAndForecastAnswerDTO toTestDTO) {
        Assertions.assertEquals(referenceDTO.latitude(), toTestDTO.latitude(), "The saved DTO doesn't have the correct latitude being saved");
        Assertions.assertEquals(referenceDTO.longitude(), toTestDTO.longitude(), "The saved DTO doesn't have the correct longitude being saved");
        Assertions.assertEquals(referenceDTO.timezone(), toTestDTO.timezone(), "The saved DTO doesn't have the correct timezone being saved");
        Assertions.assertEquals(referenceDTO.timezoneOffset(), toTestDTO.timezoneOffset(), "The saved DTO doesn't have the correct timezoneOffset being saved");
        Assertions.assertEquals(referenceDTO.currentWeather(), toTestDTO.currentWeather(), "The saved DTO doesn't have the correct currentWeather being saved");
        Assertions.assertEquals(referenceDTO.minutelyPrecipitation(), toTestDTO.minutelyPrecipitation(), "The saved DTO doesn't have the correct minutelyPrecipitation being saved");
        Assertions.assertEquals(referenceDTO.hourlyWeather(), toTestDTO.hourlyWeather(), "The saved DTO doesn't have the correct hourlyWeather being saved");
        Assertions.assertEquals(referenceDTO.dailyWeather(), toTestDTO.dailyWeather(), "The saved DTO doesn't have the correct dailyWeather being saved");
        Assertions.assertEquals(referenceDTO.alerts(), toTestDTO.alerts(), "The saved DTO doesn't have the correct alerts being saved");
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
            cleanDatabase(); // Otherwise other tests will fail because of entities already saved here
        } catch (FailedJsonToDtoMappingException e) {
            e.getStackTrace();
        }
        Assertions.assertNotNull(justCreatedDTO, "No weather DTO found in the database by the id " + savedAnswer.getId());
        checkDTO(answerDTO, justCreatedDTO);
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
            cleanDatabase(); // Otherwise other tests will fail because of entities already saved here
        } catch (FailedJsonToDtoMappingException e) {
            e.getStackTrace();
        }
        Assertions.assertNotNull(justCreatedDTOs, "Failed to retrieve saved DTOs from the database");
        Assertions.assertEquals(justCreatedDTOs.size(), answerDTOList.size(), "The count of retrieved DTOs doesn't match the count of initially stored ones");
        List<CurrentAndForecastAnswerDTO> finalJustCreatedDTOs = justCreatedDTOs;
        IntStream.range(0, justCreatedDTOs.size()).forEach(i -> {
            Logger.getLogger(CurrentAndForecastAnswerServiceTest.class.getName()).info("Checking the " + i + "th DTO ");
            checkDTO(answerDTOList.get(i), finalJustCreatedDTOs.get(i));
        });
    }

    @Test
    void testGetLastHourCurrentAndForecastWeather() {
        CurrentAndForecastAnswerDTO answerDTO = loadMockResponseFromFile(this.dataFilePath);

        // Create and persist the Entities to be used (to trigger the on-create setting of the timestampLastCall)
        CurrentAndForecastAnswer answerNew = new CurrentAndForecastAnswer();
        CurrentAndForecastAnswer answerOld = new CurrentAndForecastAnswer();
        currentAndForecastAnswerRepository.save(answerNew);
        currentAndForecastAnswerRepository.save(answerOld);

        // Set the desired Entity attributes (i.e., change the timestampLastCall to mimic an old api call)
        // and persist again for the changes to take effect
        answerOld.setTimestampLastCall(ZonedDateTime.now().minusHours(5));
        try {
            answerNew.setWeatherData(currentAndForecastAnswerService.serializeDTO(answerDTO));
            answerOld.setWeatherData(currentAndForecastAnswerService.serializeDTO(answerDTO));
            currentAndForecastAnswerRepository.save(answerNew);
            currentAndForecastAnswerRepository.save(answerOld);
        } catch (FailedToSerializeDTOException e) {
            throw new RuntimeException(e.getMessage());
        }
        List<CurrentAndForecastAnswerDTO> lastHourDTOs = null;
        try {
            // minusMinutes(1) is used to ensure that the timestampLastCall of the query is after the one of the saved Entity previously declared in this test
            Assertions.assertEquals(answerNew.getId(), currentAndForecastAnswerRepository.findByTimestampLastCallIsAfter(ZonedDateTime.now().minusMinutes(1)).get(0).getId(), "Id of the saved DTO doesn't match the id of the newly retrieved one");
            lastHourDTOs = currentAndForecastAnswerService.getLastHourCurrentAndForecastWeather();
            cleanDatabase(); // Otherwise other tests will fail because of entities already saved here
        } catch (FailedJsonToDtoMappingException e) {
            throw new RuntimeException(e.getMessage());
        }
        Assertions.assertNotNull(lastHourDTOs, "Failed to retrieve DTOs from database");
        Assertions.assertEquals(1, lastHourDTOs.size(), "Expected DTO count = 1 but " + lastHourDTOs.size() + " was found instead");
        CurrentAndForecastAnswerDTO lastHourDTO = lastHourDTOs.get(0);
        checkDTO(answerDTO, lastHourDTO);
    }

    @Test
    void testFindCurrentAndForecastWeatherById() {
        CurrentAndForecastAnswerDTO answerDTO = loadMockResponseFromFile(this.dataFilePath);

        CurrentAndForecastAnswer answer = new CurrentAndForecastAnswer();
        CurrentAndForecastAnswer savedAnswer = null;
        try {
            answer.setWeatherData(currentAndForecastAnswerService.serializeDTO(answerDTO));
            savedAnswer = currentAndForecastAnswerRepository.save(answer);
        } catch (FailedToSerializeDTOException e) {
            throw new RuntimeException(e.getMessage());
        }
        CurrentAndForecastAnswerDTO lastHourDTO = null;
        try {
            lastHourDTO = currentAndForecastAnswerService.findCurrentAndForecastWeatherById(Long.valueOf(savedAnswer.getId()));
            cleanDatabase();
        } catch (FailedJsonToDtoMappingException e) {
            throw new RuntimeException(e.getMessage());
        }
        Assertions.assertNotNull(lastHourDTO, "Failed to retrieve DTO with id 1 from the database");
        checkDTO(answerDTO, lastHourDTO);
    }

    @Test
    void testDeserializeDTO() {
        CurrentAndForecastAnswerDTO answerDTO = loadMockResponseFromFile(this.dataFilePath);
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        byte[] serializedDTO = null;
        try {
            serializedDTO = mapper.writeValueAsBytes(answerDTO);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
        Assertions.assertNotNull(serializedDTO, "Failed to serialize the DTO");
        CurrentAndForecastAnswerDTO deserializedDTO = null;
        try {
            deserializedDTO = currentAndForecastAnswerService.deserializeDTO(serializedDTO);
        } catch (FailedJsonToDtoMappingException e) {
            throw new RuntimeException(e.getMessage());
        }
        Assertions.assertNotNull(deserializedDTO, "Failed to deserialize the DTO");
        checkDTO(answerDTO, deserializedDTO);
        Assertions.assertThrows(FailedJsonToDtoMappingException.class, () -> {
            // Appending random strings AFTER the DTO string doesn't cause issues as the mapping can be done as planed
            // and the rest is just discarded I guess
            String faultyStringDTO = "[This should mess up the mapping to DTO]," + mapper.writeValueAsString(answerDTO);
            System.out.println(faultyStringDTO);
            CurrentAndForecastAnswerDTO mappedDTO = currentAndForecastAnswerService.deserializeDTO(faultyStringDTO.getBytes());
        });
    }

    @Test
    void testSerializeDTO() {
        CurrentAndForecastAnswerDTO answerDTO = loadMockResponseFromFile(this.dataFilePath);
        byte[] serializedDTO = null;
        try {
            serializedDTO = currentAndForecastAnswerService.serializeDTO(answerDTO);
        } catch (FailedToSerializeDTOException e) {
            throw new RuntimeException(e.getMessage());
        }
        Assertions.assertNotNull(serializedDTO, "Failed to serialize the DTO");
        CurrentAndForecastAnswerDTO retrievedDTO = null;
        try {
            retrievedDTO = currentAndForecastAnswerService.deserializeDTO(serializedDTO);
        } catch (FailedJsonToDtoMappingException e) {
            throw new RuntimeException(e.getMessage());
        }
        Assertions.assertNotNull(retrievedDTO, "Failed to deserialize serialized DTO");
        Assertions.assertEquals(answerDTO, retrievedDTO, "DTO doesn't match initial state after serialization and deserialization cycle");
    }
}
