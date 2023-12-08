package at.qe.skeleton.external.model.currentandforecast.misc;

import at.qe.skeleton.external.model.shared.WeatherDTO;
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
 * @param timestamp                       unix timestamp
 * @param sunrise                         unix timestamp
 * @param sunset                          unix timestamp
 * @param moonrise                        unix timestamp
 * @param moonset                         unix timestamp
 * @param moonPhase                       [0,1] see documentation for further details
 * @param summary                         textual summary of weather
 * @param dailyTemperatureAggregation
 * @param feelsLikeTemperatureAggregation
 * @param pressure                        in hPA
 * @param humidity                        % humidity [0, 100]
 * @param dewPoint                        temperature at which water starts to condensate
 * @param windSpeed                       in m/s
 * @param windGust                        in m/s
 * @param windDirection                   in m/s
 * @param clouds                          % cloudiness [0,100]
 * @param uvi                             uv index
 * @param probabilityOfPrecipitation      [0,1]
 * @param rain                            in mm/h (can be null if it is not raining)
 * @param snow                            in mm/h (can be null if it is not snowing)
 * @param weather
 * @see <a href="https://openweathermap.org/api/one-call-3#current">API Documentation</a>
 */
public record DailyWeatherDTO(
        @JsonProperty("dt") Instant timestamp,
        Instant sunrise,
        Instant sunset,
        Instant moonrise,
        Instant moonset,
        @JsonProperty("moon_phase") double moonPhase,
        String summary,
        @JsonProperty("temp") DailyTemperatureAggregationDTO dailyTemperatureAggregation,
        @JsonProperty("feels_like") TemperatureAggregationDTO feelsLikeTemperatureAggregation,
        int pressure,
        int humidity,
        @JsonProperty("dew_point") double dewPoint,
        @JsonProperty("wind_speed") double windSpeed,
        @JsonProperty("wind_gust") double windGust,
        @JsonProperty("wind_deg") double windDirection,
        int clouds,
        int uvi,
        @JsonProperty("pop") int probabilityOfPrecipitation,
        Double rain,
        Double snow,
        @JsonDeserialize(using = WeatherDeserializer.class) WeatherDTO weather
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1;

}
