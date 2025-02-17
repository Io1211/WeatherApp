package at.qe.skeleton.tests;

import at.qe.skeleton.internal.helper.WeatherHelper;
import at.qe.skeleton.internal.ui.beans.WeatherBean;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;

@SpringBootTest
class WeatherBeanTest {

  public WeatherHelper weatherHelper = new WeatherHelper();

  /**
   * Some tests for {@link WeatherBean}. The tests check if timestamps and wind directions are
   * properly converted.
   */
  @Test
  public void formatInstantToDateTimeTest() {
    Instant timestamp = Instant.parse("2023-04-15T12:30:45Z");
    String format = "yyyy-MM-dd HH:mm";
    String timezone = "Europe/Paris"; // UTC+2
    String result = weatherHelper.formatInstantToDateTime(timezone, timestamp, format);
    assertEquals("2023-04-15 14:30", result);

    format = "HH:mm";
    timezone = "America/Los_Angeles"; // UTC-7
    result = weatherHelper.formatInstantToDateTime(timezone, timestamp, format);
    assertEquals("05:30", result);

    timestamp = Instant.parse("2023-08-27T08:15:30Z");
    format = "yyyy.MM.dd";
    result = weatherHelper.formatInstantToDateTime(timezone, timestamp, format);
    assertEquals("2023.08.27", result);
  }

  @Test
  public void degreesToCardinalTest() {
    assertEquals("N", weatherHelper.degreesToCardinal(0.0));
    assertEquals("E", weatherHelper.degreesToCardinal(90.0));
    assertEquals("NNW", weatherHelper.degreesToCardinal(337.5));
  }
}
