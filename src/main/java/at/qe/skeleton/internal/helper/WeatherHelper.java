package at.qe.skeleton.internal.helper;

import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class WeatherHelper {
  /**
   * Extracts timezone from a given CurrentAndForecastAnswerDTO and converts the given timestamp
   * instant into a Time with the format HH:mm using that timezone. To get the timestamp,
   * currentWeather.timestamp() is used.
   *
   * @param timestamp the Instant timestamp which should be converted
   * @param answerDTO the CurrentAndForecastAnswerDTO which contains the timezone
   * @return current time in the format HH:mm
   */
  public String formatCAFADTOToTime(CurrentAndForecastAnswerDTO answerDTO, Instant timestamp) {
    LocalDateTime localDateTime =
        LocalDateTime.ofInstant(timestamp, ZoneId.of(answerDTO.timezone()));
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
    return localDateTime.format(formatter);
  }

  /**
   * Formats an instant timestamp to a date-time string using a given specified format. Used to
   * convert multiple occurrences of timestamps (Type Instant) in the weather details table to the
   * desired formats.
   *
   * @param timezone String of the timezone.
   * @param timestamp Instant timestamp.
   * @param format Desired date-time format so e.g. "HH:mm" or "dd.MM.yyyy - HH:mm".
   * @return A formatted date-time string.
   */
  public String formatInstantToDateTime(String timezone, Instant timestamp, String format) {
    ZoneId zoneId = ZoneId.of(timezone);
    LocalDateTime localDateTime = LocalDateTime.ofInstant(timestamp, zoneId);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
    return localDateTime.format(formatter);
  }

  /**
   * Converts a direction in metrological degrees to a cardinal direction string.
   *
   * @param degrees Direction in metrological degrees, from 0 to 360.
   * @return a string for the cardinal direction
   */
  public String degreesToCardinal(double degrees) {
    String[] directions = {
      "N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW",
      "NNW"
    };
    int dir = (int) (((degrees + 11.25) % 360) / 22.5);
    return directions[dir];
  }

  /**
   * Method to round the weather to integer degrees. The default precision of 0.01 is just not
   * realistic.
   */
  public Long roundTemp(double temp) {
    return Math.round(temp);
  }

  public Long msToKmh(Double mps) {
    return Math.round(mps * 3.6);
  }
}
