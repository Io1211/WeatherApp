package at.qe.skeleton.external.model.currentandforecast;

import at.qe.skeleton.external.model.currentandforecast.misc.AlertDTO;
import at.qe.skeleton.external.model.currentandforecast.misc.CurrentWeatherDTO;
import at.qe.skeleton.external.model.currentandforecast.misc.DailyWeatherDTO;
import at.qe.skeleton.external.model.currentandforecast.misc.PrecipitationByTimestampDTO;
import at.qe.skeleton.external.model.currentandforecast.misc.HourlyWeatherDTO;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * This class is part of the skeleton project provided for students of the
 * course "Software Architecture" offered by Innsbruck University.
 * <br><br>
 * This class is used to model the answer of the current and forecast API Call
 *
 * @param latitude              in deg
 * @param longitude             in deg
 * @param timezone              timezone identifier (e.g Europe/Vienna)
 * @param timezoneOffset        timezone offset
 * @param currentWeather        the current weather
 * @param minutelyPrecipitation rain/snow in the next our (forecast for the next minutes)
 * @param hourlyWeather         weather in the next hours (forecast for the next hours)
 * @param dailyWeather          weather of the next days (forecast for the next days)
 * @param alerts                weather alerts (e.g. warnings)
 * @see <a href="https://openweathermap.org/api/one-call-3#current">API Documentation</a>
 */
public record CurrentAndForecastAnswerDTO(
        @JsonProperty("lat") double latitude,
        @JsonProperty("lon") double longitude,
        String timezone,
        @JsonProperty("timezone_offset") int timezoneOffset,
        @JsonProperty("current") CurrentWeatherDTO currentWeather,
        @JsonProperty("minutely") List<PrecipitationByTimestampDTO> minutelyPrecipitation,
        @JsonProperty("hourly") List<HourlyWeatherDTO> hourlyWeather,
        @JsonProperty("daily") List<DailyWeatherDTO> dailyWeather,
        List<AlertDTO> alerts
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1;

}
