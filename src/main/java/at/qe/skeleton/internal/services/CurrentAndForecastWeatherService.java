package at.qe.skeleton.internal.services;

import at.qe.skeleton.internal.model.CurrentAndForecastWeather;
import at.qe.skeleton.internal.repositories.CurrentAndForecastWeatherRepository;
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
    public String findWeather(Long id) {
        CurrentAndForecastWeather currentAndForecastWeather = currentAndForecastWeatherRepository.findById(id);
        // important: if the byte array the weather is stored as in CurrentAndForecastWeather isn't transformed
        // bach to String in the correct way, the output will be gibberish
        return new String(currentAndForecastWeather.getWeatherData(), StandardCharsets.UTF_8);
    }
}
