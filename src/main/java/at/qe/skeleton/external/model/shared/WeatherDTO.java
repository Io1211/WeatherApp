package at.qe.skeleton.external.model.shared;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class is part of the skeleton project provided for students of the
 * course "Software Architecture" offered by Innsbruck University.
 * <br><br>
 * This class is used to model the answer to an api call
 * @see <a href="https://openweathermap.org/weather-conditions">Weather conditions | API Documentation</a>
 * @param id the id of the weather
 * @param title the title of the weather
 * @param description of the weather
 * @param icon of the weather (can be used to retrieve images)
 */
public record WeatherDTO(long id,
                         @JsonProperty("main") String title,
                         String description,
                         String icon) {
}
