package at.qe.skeleton.external.model.currentandforecast.misc;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serial;
import java.io.Serializable;

/**
 * This class is part of the skeleton project provided for students of the course "Software
 * Architecture" offered by Innsbruck University. <br>
 * <br>
 * This class is used to model the answer to an api call
 *
 * @param morningTemperature in deg celsius
 * @param dayTemperature in deg celsius
 * @param eveningTemperature in deg celsius
 * @param nightTemperature in deg celsius
 * @param minimumDailyTemperature in deg celsius
 * @param maximumDailyTemperature in deg celsius
 * @see <a href="https://openweathermap.org/api/one-call-3#current">API Documentation</a>
 */
public record DailyTemperatureAggregationDTO(
    @JsonProperty("morn") double morningTemperature,
    @JsonProperty("day") double dayTemperature,
    @JsonProperty("eve") double eveningTemperature,
    @JsonProperty("night") double nightTemperature,
    @JsonProperty("min") double minimumDailyTemperature,
    @JsonProperty("max") double maximumDailyTemperature)
    implements Serializable {

  @Serial private static final long serialVersionUID = 1;
}
