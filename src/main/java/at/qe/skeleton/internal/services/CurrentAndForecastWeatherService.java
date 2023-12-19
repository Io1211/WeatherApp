package at.qe.skeleton.internal.services;

import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import at.qe.skeleton.internal.model.CurrentAndForecastWeather;
import at.qe.skeleton.internal.repositories.CurrentAndForecastWeatherRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Service for accessing and manipulating CurrentAndForecastWeather data.
 */
@Component
@Scope("application")
public class CurrentAndForecastWeatherService {

    @Autowired
    private CurrentAndForecastWeatherRepository currentAndForecastWeatherRepository;

    /**
     * Takes the API call that has been serialized to a Json string and serializes it to a byte array. This is then
     * set as a field of a CurrentAndForecastWeather (the entity) object and persisted via the repository.
     *
     * @param currentAndForecastAnswer the API call that has been serialized to a Json string
     * @return the saved CurrentAndForecastWeather object
     */
    public CurrentAndForecastWeather saveWeather(String currentAndForecastAnswer) {
        CurrentAndForecastWeather currentAndForecastWeather = new CurrentAndForecastWeather();
        currentAndForecastWeather.setWeatherData(currentAndForecastAnswer.getBytes(StandardCharsets.UTF_8));
        return currentAndForecastWeatherRepository.save(currentAndForecastWeather);
    }

    /**
     * Retrieve the persisted CurrentAndForecastWeather objects by their id.
     *
     * @param id the CurrentAndForecastWeather id
     * @return the deserialized CurrentAndForecastWeather object with the corresponding id
     */
    public CurrentAndForecastAnswerDTO findWeather(Long id) throws JsonMappingException {
        CurrentAndForecastWeather currentAndForecastWeather = currentAndForecastWeatherRepository.findById(id);
        ObjectMapper mapper = new ObjectMapper()
                .findAndRegisterModules();
        try {
            return mapper.readValue(new String(currentAndForecastWeather.getWeatherData()), CurrentAndForecastAnswerDTO.class);
        } catch (JsonProcessingException e) {
            throw new JsonMappingException("error while mapping stored Json to CurrentAndForecastWeatherDTO");
        }
    }
}
