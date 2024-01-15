package at.qe.skeleton.internal.services;

import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import at.qe.skeleton.external.services.WeatherApiRequestService;
import at.qe.skeleton.internal.model.CurrentAndForecastAnswer;
import at.qe.skeleton.internal.repositories.CurrentAndForecastAnswerRepository;
import at.qe.skeleton.internal.services.exceptions.FailedApiRequest;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.SerializationUtils;
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

  private static final Logger LOGGER =
      LoggerFactory.getLogger(CurrentAndForecastAnswerService.class);

  public CurrentAndForecastAnswerDTO callApi(double lon, double lat) throws FailedApiRequest {
    try {
      return this.weatherApiRequestService.retrieveCurrentAndForecastWeather(lat, lon);
    } catch (final Exception e) {
      String errorMessage =
          "An error occurred in the CurrentAndForecastWeather api call with latitude: %s and longitude: %s"
              .formatted(lat, lon);
      LOGGER.error(errorMessage, e);
      throw new FailedApiRequest(errorMessage);
    }
  }

  /**
   * Takes the Json result of the API call that has been mapped to a CurrentAndForeCastAnswerDTO and
   * serializes it to a byte array. This is then set as a field of a CurrentAndForecastWeather (the
   * entity) object and persisted via the repository.
   *
   * @param answerDTO the API call that has been mapped to a CurrentAndForecastAnswerDTO
   * @return the saved CurrentAndForecastWeather object
   */
  public CurrentAndForecastAnswer saveWeather(@NotNull CurrentAndForecastAnswerDTO answerDTO) {
    CurrentAndForecastAnswer currentAndForecastAnswer = new CurrentAndForecastAnswer();
    currentAndForecastAnswer.setWeatherData(serializeDTO(answerDTO));
    return currentAndForecastAnswerRepository.save(currentAndForecastAnswer);
  }

  /**
   * Deserialize a mapped CurrentAndForecastAnswer into a CurrentAndForecastAnswerDTO
   *
   * @param serializedDTO the serialized CurrentAndForecastAnswerDTO to deserialize
   * @return the deserialized CurrentAndForecastAnswerDTO
   */
  public CurrentAndForecastAnswerDTO deserializeDTO(@NotNull byte[] serializedDTO) {
    // Don't remove explicit casting!
    // Intellij says it's useless, but boot will throw errors if it is missing
    return (CurrentAndForecastAnswerDTO) SerializationUtils.deserialize(serializedDTO);
  }

  /**
   * Serialize api answers to be saved in the database as blobs
   *
   * @param answerDTO the api call answer
   * @return the serialized api call
   */
  public byte[] serializeDTO(@NotNull CurrentAndForecastAnswerDTO answerDTO) {
    return SerializationUtils.serialize(answerDTO);
  }
}
