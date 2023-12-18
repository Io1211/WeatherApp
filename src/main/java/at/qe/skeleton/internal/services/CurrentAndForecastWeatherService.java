package at.qe.skeleton.internal.services;

import at.qe.skeleton.internal.model.CurrentAndForecastWeather;
import at.qe.skeleton.internal.repositories.CurrentAndForecastWeatherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Component
@Scope("application")
public class CurrentAndForecastWeatherService {

    @Autowired
    private CurrentAndForecastWeatherRepository currentAndForecastWeatherRepository;

    public CurrentAndForecastWeather saveWeather(String currentAndForecastAnswer) {
        CurrentAndForecastWeather currentAndForecastWeather = new CurrentAndForecastWeather();
        currentAndForecastWeather.setWeatherData(currentAndForecastAnswer.getBytes(StandardCharsets.UTF_8));
        currentAndForecastWeather.setCreateDate(LocalDateTime.now());
        return currentAndForecastWeatherRepository.save(currentAndForecastWeather);
    }

    public String findWeather(Long id) {
        CurrentAndForecastWeather currentAndForecastWeather = currentAndForecastWeatherRepository.findById(id);
        // important: if the byte array the weather is stored as in CurrentAndForecastWeather isn't transformed
        // bach to String in the correct way, the output will be gibberish
        return new String(currentAndForecastWeather.getWeatherData(), StandardCharsets.UTF_8);
    }
}
