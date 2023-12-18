package at.qe.skeleton.internal.repositories;

import at.qe.skeleton.internal.model.CurrentAndForecastWeather;

public interface CurrentAndForecastWeatherRepository extends AbstractRepository<CurrentAndForecastWeather, String> {
    CurrentAndForecastWeather findById(Long id);
}
