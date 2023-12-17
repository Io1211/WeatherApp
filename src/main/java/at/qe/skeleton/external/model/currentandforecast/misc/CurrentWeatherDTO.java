package at.qe.skeleton.external.model.currentandforecast.misc;

import at.qe.skeleton.external.model.shared.WeatherDTO;
import at.qe.skeleton.external.model.deserialisation.PrecipitationDeserializer;
import at.qe.skeleton.external.model.deserialisation.WeatherDeserializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * This class is part of the skeleton project provided for students of the
 * course "Software Architecture" offered by Innsbruck University.
 * <br><br>
 * This class is used to model the answer to an API call
 *
 * @param timestamp            UNIX timestamp, use timezone or timezoneOffset from parent to convert to {@link java.time.ZonedDateTime}
 * @param sunrise              UNIX timestamp, use timezone or timezoneOffset from parent to convert to {@link java.time.ZonedDateTime}
 * @param sunset               UNIX timestamp, use timezone or timezoneOffset from parent to convert to {@link java.time.ZonedDateTime}
 * @param temperature          in deg celsius
 * @param feelsLikeTemperature in deg celsius
 * @param pressure             in hPa
 * @param humidity             % humidity [0, 100]
 * @param dewPoint             atmospheric temperature below which water droplets begin to condense in deg celsius
 * @param clouds               % cloudiness [0, 100]
 * @param uvi                  uv index
 * @param visibility           in meters
 * @param rain                 in mm/h (is null if it is not raining)
 * @param snow                 current snow in mm/h (is null if it is not snowing)
 * @param windSpeed            in m/s
 * @param windGust             in m/s
 * @param windDirection        wind direction, in metrological degrees
 * @param weather
 * @see <a href="https://openweathermap.org/api/one-call-3#current">API Documentation</a>
 */
public record CurrentWeatherDTO(
        @JsonProperty("dt") Instant timestamp,
        Instant sunrise,
        Instant sunset,
        @JsonProperty("temp") double temperature,
        @JsonProperty("feels_like") double feelsLikeTemperature,
        int pressure,
        int humidity,
        @JsonProperty("dew_point") double dewPoint,
        int clouds,
        int uvi,
        int visibility,
        @JsonDeserialize(using = PrecipitationDeserializer.class) Double rain,
        @JsonDeserialize(using = PrecipitationDeserializer.class) Double snow,
        @JsonProperty("wind_speed") double windSpeed,
        @JsonProperty("wind_gust") double windGust,
        @JsonProperty("wind_deg") double windDirection,
        @JsonDeserialize(using = WeatherDeserializer.class) WeatherDTO weather
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1;

}

