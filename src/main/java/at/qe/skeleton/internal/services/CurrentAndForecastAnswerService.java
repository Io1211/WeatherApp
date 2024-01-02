package at.qe.skeleton.internal.services;

import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import at.qe.skeleton.internal.model.CurrentAndForecastAnswer;
import at.qe.skeleton.internal.repositories.CurrentAndForecastAnswerRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/** Service for accessing and manipulating CurrentAndForecastWeather data. */
@Component
@Scope("application")
public class CurrentAndForecastAnswerService {

  @Autowired private CurrentAndForecastAnswerRepository currentAndForecastAnswerRepository;

  /**
   * sollte es nicht heißen: Takes the WeatherApi Response that has been mapped to a
   * CurrentAndForeCastAnswerDTO ...?
   *
   * <p>Takes the API call that has been serialized to a Json string and serializes it to a byte
   * array. This is then set as a field of a CurrentAndForecastWeather (the entity) object and
   * persisted via the repository.
   *
   * @param answerDTO the API call that has been mapped to a CurrentAndForecastAnswerDTO
   * @return the saved CurrentAndForecastWeather object
   */
  public CurrentAndForecastAnswer saveWeather(@NotNull CurrentAndForecastAnswerDTO answerDTO)
      throws FailedToSerializeDTOException {
    CurrentAndForecastAnswer currentAndForecastAnswer = new CurrentAndForecastAnswer();
    currentAndForecastAnswer.setWeatherData(serializeDTO(answerDTO));
    return currentAndForecastAnswerRepository.save(currentAndForecastAnswer);
  }

  /**
   * Retrieve all the persisted CurrentAndForecastAnswer objects.
   *
   * @return a collection of all the currently stored CurrentAndForecastAnswer API calls as DTOs
   */
  public List<CurrentAndForecastAnswerDTO> getAllCurrentAndForecastWeather()
      throws FailedJsonToDtoMappingException {
    List<CurrentAndForecastAnswer> allWeatherData = currentAndForecastAnswerRepository.findAll();
    List<CurrentAndForecastAnswerDTO> allWeatherDataDTOs = new ArrayList<>();
    for (CurrentAndForecastAnswer currentAndForecastAnswer : allWeatherData) {
      allWeatherDataDTOs.add(deserializeDTO(currentAndForecastAnswer.getWeatherData()));
    }
    return allWeatherDataDTOs;
  }

  /**
   * 1. brauchen wir diese Methode wirklich? wäre es nicht einfacher bereits entweder in
   * CurrentAndForecastAnswer Entity oder in der LocationEntity, welche die CurrentAndForeCastAnswer
   * Entity enthält die Lon/Lat daten zu speichern und mithilfe derer zu schauen ob es einen
   * entsprechenden Eintrag in der Datenbank innerhalb der letzten Stunde gibt? also eine methode,
   * die die lon & lat daten nimmt und gleichzeitig nach timestamp aussortiert.
   *
   * <p>2. die Berechnung der Zeit ((ZonedDateTime.now().minusHours(1)) entspricht nicht den Angaben
   * in der Projekt-Aufgabenstellung. Dort steht: "API Calls sind teuer. Bis zur NÄCHSTEN VOLLEN
   * STUNDE wird derselbe Bericht pro Ort ange zeigt." Es müsste so ungefähr sein: ZonedDateTime now
   * = ZonedDateTime.now(); int currentHour = now.getHour(); ZonedDateTime lastFullHourDateTime =
   * now.minusMinutes(now.getMinute()).minusSeconds(now.getSecond()).minusNanos(now.getNano());
   *
   * <p>Returns the api calls made in the last hour
   *
   * @return the api calls made in the last hour as DTOs
   * @throws FailedJsonToDtoMappingException if the retrieved data fails to map back to DTO
   */
  public List<CurrentAndForecastAnswerDTO> getLastHourCurrentAndForecastWeather()
      throws FailedJsonToDtoMappingException {
    List<CurrentAndForecastAnswer> lastHourWeatherData =
        currentAndForecastAnswerRepository.findByTimestampLastCallIsAfter(
            ZonedDateTime.now().minusHours(1));
    List<CurrentAndForecastAnswerDTO> lastHourWeatherDTOs = new ArrayList<>();
    for (CurrentAndForecastAnswer weatherData : lastHourWeatherData) {
      lastHourWeatherDTOs.add(deserializeDTO(weatherData.getWeatherData()));
    }
    return lastHourWeatherDTOs;
  }

  /**
   * Find weather api calls by their id
   *
   * @param id
   * @return
   * @throws FailedJsonToDtoMappingException if the retrieved serialized data can't be mapped back
   *     to DTO
   */
  public CurrentAndForecastAnswerDTO findCurrentAndForecastWeatherById(Long id)
      throws FailedJsonToDtoMappingException {
    return deserializeDTO(currentAndForecastAnswerRepository.findById(id).getWeatherData());
  }

  /**
   * Deserialize a mapped CurrentAndForecastAnswer into a CurrentAndForecastAnswerDTO
   *
   * @param serializedDTO the serialized CurrentAndForecastAnswerDTO to deserialize
   * @return the deserialized CurrentAndForecastAnswerDTO
   * @throws FailedJsonToDtoMappingException when the mapping fails
   */
  public CurrentAndForecastAnswerDTO deserializeDTO(@NotNull byte[] serializedDTO)
      throws FailedJsonToDtoMappingException {
    ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
    try {
      return mapper.readValue(serializedDTO, CurrentAndForecastAnswerDTO.class);
    } catch (IOException e) {
      throw new FailedJsonToDtoMappingException();
    }
  }

  /**
   * Serialize api answers to be saved in the database as blobs
   *
   * @param answerDTO the api call answer
   * @return the serialized api call
   * @throws FailedToSerializeDTOException when the serialization fails
   */
  public byte[] serializeDTO(@NotNull CurrentAndForecastAnswerDTO answerDTO)
      throws FailedToSerializeDTOException {
    ObjectMapper mapper =
        new ObjectMapper().findAndRegisterModules().enable(SerializationFeature.INDENT_OUTPUT);
    try {
      return mapper
          .writerWithDefaultPrettyPrinter()
          .writeValueAsString(answerDTO)
          .getBytes(StandardCharsets.UTF_8);
    } catch (JsonProcessingException e) {
      throw new FailedToSerializeDTOException();
    }
  }
}
