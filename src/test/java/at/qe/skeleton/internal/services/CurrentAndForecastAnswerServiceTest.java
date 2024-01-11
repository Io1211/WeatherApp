package at.qe.skeleton.internal.services;

import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import at.qe.skeleton.internal.model.CurrentAndForecastAnswer;
import at.qe.skeleton.internal.repositories.CurrentAndForecastAnswerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

@SpringBootTest
@WebAppConfiguration
class CurrentAndForecastAnswerServiceTest {

  @Autowired CurrentAndForecastAnswerService currentAndForecastAnswerService;

  @Autowired CurrentAndForecastAnswerRepository currentAndForecastAnswerRepository;

  private CurrentAndForecastAnswerDTO mockDTO;

  @BeforeEach
  public void loadMockResponseFromFile() throws IOException {
    this.mockDTO =
        new ObjectMapper()
            .findAndRegisterModules()
            .readValue(
                new File("src/test/resources/WeatherApiResponseMunich.json"),
                CurrentAndForecastAnswerDTO.class);
  }

  @AfterEach
  public void cleanDatabase() {
    List<CurrentAndForecastAnswer> entities = currentAndForecastAnswerRepository.findAll();
    entities.forEach(currentAndForecastAnswerRepository::delete);
  }

  private void checkDTO(CurrentAndForecastAnswerDTO toTestDTO) {
    CurrentAndForecastAnswerDTO referenceDTO = this.mockDTO;
    Assertions.assertEquals(
        referenceDTO.latitude(),
        toTestDTO.latitude(),
        "The saved DTO doesn't have the correct latitude being saved");
    Assertions.assertEquals(
        referenceDTO.longitude(),
        toTestDTO.longitude(),
        "The saved DTO doesn't have the correct longitude being saved");
    Assertions.assertEquals(
        referenceDTO.timezone(),
        toTestDTO.timezone(),
        "The saved DTO doesn't have the correct timezone being saved");
    Assertions.assertEquals(
        referenceDTO.timezoneOffset(),
        toTestDTO.timezoneOffset(),
        "The saved DTO doesn't have the correct timezoneOffset being saved");
    Assertions.assertEquals(
        referenceDTO.currentWeather(),
        toTestDTO.currentWeather(),
        "The saved DTO doesn't have the correct currentWeather being saved");
    Assertions.assertEquals(
        referenceDTO.minutelyPrecipitation(),
        toTestDTO.minutelyPrecipitation(),
        "The saved DTO doesn't have the correct minutelyPrecipitation being saved");
    Assertions.assertEquals(
        referenceDTO.hourlyWeather(),
        toTestDTO.hourlyWeather(),
        "The saved DTO doesn't have the correct hourlyWeather being saved");
    Assertions.assertEquals(
        referenceDTO.dailyWeather(),
        toTestDTO.dailyWeather(),
        "The saved DTO doesn't have the correct dailyWeather being saved");
    Assertions.assertEquals(
        referenceDTO.alerts(),
        toTestDTO.alerts(),
        "The saved DTO doesn't have the correct alerts being saved");
  }

  @Test
  void testSaveWeather() {
    CurrentAndForecastAnswer savedAnswer =
        currentAndForecastAnswerService.saveWeather(this.mockDTO);
    Assertions.assertNotNull(savedAnswer, "Failed to save the mock DTO in the database");
    CurrentAndForecastAnswerDTO justCreatedDTO =
        currentAndForecastAnswerService.deserializeDTO(
            currentAndForecastAnswerRepository.findAll().get(0).getWeatherData());
    Assertions.assertNotNull(
        justCreatedDTO, "Failed to fetch the saved mock DTO from the database");
    checkDTO(justCreatedDTO);
  }

  @Test
  void testGetAllCurrentAndForecastWeather() {
    List<CurrentAndForecastAnswerDTO> answerDTOList = new ArrayList<>();
    IntStream.range(0, 5).forEach(dto -> answerDTOList.add(this.mockDTO));
    answerDTOList.forEach(dto -> currentAndForecastAnswerService.saveWeather(dto));
    List<CurrentAndForecastAnswerDTO> justCreatedDTOs = new ArrayList<>();
    currentAndForecastAnswerRepository
        .findAll()
        .forEach(
            entity ->
                justCreatedDTOs.add(
                    currentAndForecastAnswerService.deserializeDTO(entity.getWeatherData())));
    Assertions.assertNotNull(justCreatedDTOs, "Failed to retrieve saved DTOs from the database");
    Assertions.assertEquals(
        justCreatedDTOs.size(),
        answerDTOList.size(),
        "The count of retrieved DTOs doesn't match the count of initially stored ones");
    IntStream.range(0, justCreatedDTOs.size())
        .forEach(
            i -> {
              Logger.getLogger(CurrentAndForecastAnswerServiceTest.class.getName())
                  .info("Checking the " + i + "th DTO ");
              checkDTO(justCreatedDTOs.get(i));
            });
  }

  @Test
  void testDeserializeDTO() throws IOException {
    byte[] serializedDTO = SerializationUtils.serialize(this.mockDTO);
    Assertions.assertNotNull(serializedDTO, "Failed to serialize the DTO");
    CurrentAndForecastAnswerDTO deserializedDTO =
        currentAndForecastAnswerService.deserializeDTO(serializedDTO);
    Assertions.assertNotNull(deserializedDTO, "Failed to deserialize the DTO");
    checkDTO(deserializedDTO);
  }

  @Test
  void testSerializeDTO() {
    byte[] serializedDTO = currentAndForecastAnswerService.serializeDTO(this.mockDTO);
    Assertions.assertNotNull(serializedDTO, "Failed to serialize the DTO");
    CurrentAndForecastAnswerDTO retrievedDTO =
        currentAndForecastAnswerService.deserializeDTO(serializedDTO);
    Assertions.assertNotNull(retrievedDTO, "Failed to deserialize serialized DTO");
    checkDTO(retrievedDTO);
  }
}
