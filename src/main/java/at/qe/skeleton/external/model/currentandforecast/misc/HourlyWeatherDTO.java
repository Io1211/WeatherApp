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
 * @param timestamp                  UNIX timestamp
 * @param temperature                in deg celsius
 * @param feelsLikeTemperature       in deg celsius
 * @param pressure                   om hPA
 * @param humidity                   % humidity [0, 100]
 * @param dewPoint                   temperature when water starts to condensate in
 * @param uvi                        uv index
 * @param clouds                     % cloudiness [0, 100]
 * @param visibility                 in metres
 * @param windSpeed                  in m/s
 * @param windGust                   in m/s
 * @param windDirection              in meteorological degrees
 * @param probabilityOfPrecipitation [0,1]
 * @param rain                       in mm/h can be null if it is not raining
 * @param snow                       in mm/h can be null if it s not snowning
 * @param weather
 * @see <a href="https://openweathermap.org/api/one-call-3#current">API Documentation</a>
 */
public record HourlyWeatherDTO(
        @JsonProperty("dt") Instant timestamp,
        @JsonProperty("temp") double temperature,
        @JsonProperty("feels_like") double feelsLikeTemperature,
        int pressure,
        double humidity,
        @JsonProperty("dew_point") double dewPoint,
        int uvi,
        int clouds,
        int visibility,
        @JsonProperty("wind_speed") double windSpeed,
        @JsonProperty("wind_gust") double windGust,
        @JsonProperty("wind_deg") double windDirection,
        @JsonProperty("pop") int probabilityOfPrecipitation,
        @JsonDeserialize(using = PrecipitationDeserializer.class) Double rain,

        @JsonDeserialize(using = PrecipitationDeserializer.class) Double snow,
        @JsonDeserialize(using = WeatherDeserializer.class) WeatherDTO weather
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1;

}
