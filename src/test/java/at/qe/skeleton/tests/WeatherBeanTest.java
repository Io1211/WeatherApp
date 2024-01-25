package at.qe.skeleton.tests;

import at.qe.skeleton.internal.ui.beans.WeatherBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@SpringBootTest
class WeatherBeanTest {

  public WeatherBean weatherBean = new WeatherBean();

  /**
   * Some tests for {@link WeatherBean}. The tests check if timestamps and wind directions are
   * properly converted.
   */
  @Test
  public void formatInstantToDateTimeTest() {
    Instant timestamp = Instant.parse("2023-04-15T12:30:45Z");
    String format = "yyyy-MM-dd HH:mm";
    String result = weatherBean.formatInstantToDateTime(timestamp, format);
    assertEquals("2023-04-15 12:30", result);

    timestamp = Instant.parse("2023-08-27T08:15:30Z");
    format = "yyyy.MM.dd";
    result = weatherBean.formatInstantToDateTime(timestamp, format);
    assertEquals("2023.08.27", result);
  }

  @Test
  public void degreesToCardinalTest() {
    assertEquals("N", weatherBean.degreesToCardinal(0.0));
    assertEquals("E", weatherBean.degreesToCardinal(90.0));
    assertEquals("NNW", weatherBean.degreesToCardinal(337.5));
  }
}
