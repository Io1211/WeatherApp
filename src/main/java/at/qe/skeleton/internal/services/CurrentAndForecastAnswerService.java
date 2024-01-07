package at.qe.skeleton.internal.services;

import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import at.qe.skeleton.external.services.WeatherApiRequestService;
import at.qe.skeleton.internal.model.CurrentAndForecastAnswer;
import at.qe.skeleton.internal.repositories.CurrentAndForecastAnswerRepository;
import at.qe.skeleton.internal.services.utils.FailedJsonToDtoMappingException;
import at.qe.skeleton.internal.services.utils.FailedToSerializeDTOException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/** Service for accessing and manipulating CurrentAndForecastWeather data. */
@Component
@Scope("application")
public class CurrentAndForecastAnswerService {

  @Autowired private WeatherApiRequestService weatherApiRequestService;

  @Autowired private CurrentAndForecastAnswerRepository currentAndForecastAnswerRepository;

  private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

  private static final Logger LOGGER =
      LoggerFactory.getLogger(CurrentAndForecastAnswerService.class);

  public CurrentAndForecastAnswerDTO callApi(double lon, double lat) {
    try {
      return this.weatherApiRequestService.retrieveCurrentAndForecastWeather(lat, lon);
    } catch (final Exception e) {
      // TODO: Better error handling
      LOGGER.error("error in request", e);
    }
    return null;
  }

  /**
   * Takes the Json result of the API call that has been mapped to a CurrentAndForeCastAnswerDTO and
   * serializes it to a byte array. This is then set as a field of a CurrentAndForecastWeather (the
   * entity) object and persisted via the repository.
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
   * Returns the api calls made in the last hour
   *
   * @return the api calls made in the last hour as DTOs
   * @throws FailedJsonToDtoMappingException if the retrieved data fails to map back to DTO
   */
  public List<CurrentAndForecastAnswerDTO> getLastHourCurrentAndForecastWeather()
      throws FailedJsonToDtoMappingException {
    ZonedDateTime now = ZonedDateTime.now();
    ZonedDateTime then =
        now.minusMinutes(now.getMinute()).minusSeconds(now.getSecond()).minusNanos(now.getNano());
    List<CurrentAndForecastAnswer> lastHourWeatherData =
        currentAndForecastAnswerRepository.findByTimestampLastCallIsAfter(then);
    List<CurrentAndForecastAnswerDTO> lastHourWeatherDTOs = new ArrayList<>();
    if (!lastHourWeatherData.isEmpty()) {
      for (CurrentAndForecastAnswer weatherData : lastHourWeatherData) {
        lastHourWeatherDTOs.add(deserializeDTO(weatherData.getWeatherData()));
      }
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
    try {
      return this.mapper.readValue(serializedDTO, CurrentAndForecastAnswerDTO.class);
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
    try {
      return this.mapper.writeValueAsBytes(answerDTO);
    } catch (JsonProcessingException e) {
      throw new FailedToSerializeDTOException();
    }
  }
}
