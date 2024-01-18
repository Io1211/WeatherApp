package at.qe.skeleton.internal.services;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

/** SubscriptionServiceTest */
@SpringBootTest
@WebAppConfiguration
class SubscriptionServiceTest {

  @Autowired SubscriptionService subscriptionService;

  @Test
  void calculatePremiumFromStartAndStopTest() {
    Month month = Month.JANUARY;
    int year = 2024;

    String startString = "2024-01-10";
    String stopString = "2024-01-23";
    LocalDate start = LocalDate.parse(startString);
    LocalDate stop = LocalDate.parse(stopString);

    // case 1: both start and stop date in the tested month
    int expected = stop.getDayOfMonth() - start.getDayOfMonth();
    Assertions.assertEquals(
        expected, subscriptionService.calculatePremiumFromStartAndStop(start, stop, month, year));

    // case 2: start == null and stop != null (i.e., not in the tested month)
    expected = stop.getDayOfMonth();
    Assertions.assertEquals(
        expected, subscriptionService.calculatePremiumFromStartAndStop(null, stop, month, year));

    // case 3: start != null (i.e., not in the tested month) and stop == null
    expected = month.length(Year.isLeap(year)) - start.getDayOfMonth();
    Assertions.assertEquals(
        expected, subscriptionService.calculatePremiumFromStartAndStop(start, null, month, year));

    // case 4: both start and stop == null (i.e., the subscription isn't started or terminated in
    // the current month)
    expected = month.length(Year.isLeap(year));
    Assertions.assertEquals(
        expected, subscriptionService.calculatePremiumFromStartAndStop(null, null, month, year));
  }
}
