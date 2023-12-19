package at.qe.skeleton.internal.repositories;

import at.qe.skeleton.internal.model.CurrentAndForecastWeather;

/**
 * Repository for managing {@link CurrentAndForecastWeather} entities.
 */
public interface CurrentAndForecastWeatherRepository extends AbstractRepository<CurrentAndForecastWeather, String> {
    CurrentAndForecastWeather findById(Long id);
}
