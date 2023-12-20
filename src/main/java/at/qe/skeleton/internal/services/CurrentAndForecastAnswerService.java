package at.qe.skeleton.internal.services;

import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import at.qe.skeleton.internal.model.CurrentAndForecastAnswer;
import at.qe.skeleton.internal.repositories.CurrentAndForecastAnswerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/** Service for accessing and manipulating CurrentAndForecastWeather data. */
@Component
@Scope("application")
public class CurrentAndForecastAnswerService {

  @Autowired private CurrentAndForecastAnswerRepository currentAndForecastAnswerRepository;

  /**
   * Takes the API call that has been serialized to a Json string and serializes it to a byte array.
   * This is then set as a field of a CurrentAndForecastWeather (the entity) object and persisted
   * via the repository.
   *
   * @param currentAndForecastAnswerString the API call that has been serialized to a Json string
   * @return the saved CurrentAndForecastWeather object
   */
  public CurrentAndForecastAnswer saveWeather(String currentAndForecastAnswerString) {
    CurrentAndForecastAnswer currentAndForecastAnswer = new CurrentAndForecastAnswer();
    currentAndForecastAnswer.setWeatherData(
        currentAndForecastAnswerString.getBytes(StandardCharsets.UTF_8));
    return currentAndForecastAnswerRepository.save(currentAndForecastAnswer);
  }

  /**
   * Retrieve the persisted CurrentAndForecastAnswer objects by their id.
   *
   * @param id the CurrentAndForecastAnswer id
   * @return the deserialized CurrentAndForecastAnswerDTO with the corresponding id
   */
  public CurrentAndForecastAnswerDTO findWeatherById(Long id) {
    CurrentAndForecastAnswer currentAndForecastAnswer =
        currentAndForecastAnswerRepository.findById(id);
    try {
      return mapSerializedWeatherToDto(currentAndForecastAnswer);
    } catch (FailedJsonToDtoMappingException e) {
      return null;
    }
  }

  /**
   * Retrieve all the persisted CurrentAndForecastAnswer objects.
   *
   * @return a collection of all the currently stored CurrentAndForecastAnswer API calls as DTOs
   */
  public Collection<CurrentAndForecastAnswerDTO> getAllCurrentAndForecastWeather() {
    Collection<CurrentAndForecastAnswerDTO> allWeatherData = new ArrayList<>();
    Collection<CurrentAndForecastAnswer> weather = currentAndForecastAnswerRepository.findAll();
    try {
      for (CurrentAndForecastAnswer currentAndForecastAnswer : weather) {
        allWeatherData.add(mapSerializedWeatherToDto(currentAndForecastAnswer));
      }
      return allWeatherData;
    } catch (FailedJsonToDtoMappingException e) {
      return Collections.emptyList();
    }
  }

  /**
   * Deserialize a mapped CurrentAndForecastAnswer into a CurrentAndForecastAnswerDTO
   *
   * @param currentAndForecastAnswer the persisted CurrentAndForecastAnswer to deserialize
   * @return the deserialized CurrentAndForecastAnswer mapped to a CurrentAndForecastAnswerDTO
   * @throws FailedJsonToDtoMappingException when the mapping fails
   */
  public CurrentAndForecastAnswerDTO mapSerializedWeatherToDto(
      CurrentAndForecastAnswer currentAndForecastAnswer) throws FailedJsonToDtoMappingException {
    ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
    try {
      return mapper.readValue(
          currentAndForecastAnswer.getWeatherData(), CurrentAndForecastAnswerDTO.class);
    } catch (IOException e) {
      throw new FailedJsonToDtoMappingException(
          "Mapping of the retrieved CurrentAndForecastWeather Json back to DTO failed");
    }
    // When there is nothing in the db, .getWeatherData() returns null and thus throws a
    // NullPointerException.
    // If it isn't caught here, the program will stop
    catch (NullPointerException e) {
      return null;
    }
  }
}
