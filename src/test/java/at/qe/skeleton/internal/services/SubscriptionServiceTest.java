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

    LocalDate start = LocalDate.parse("2024-01-10");
    LocalDate stop = LocalDate.parse("2024-01-23");

    // case 1: both start and stop date in the tested month
    int expected = stop.getDayOfMonth() - start.getDayOfMonth();
    Assertions.assertEquals(
        expected,
        subscriptionService.calculatePremiumFromStartAndStop(start, stop, month, year),
        "Error in start & stop in month case");

    start = LocalDate.parse("2023-01-10");
    // case 2: start not in tested month and stop in tested
    expected = stop.getDayOfMonth();
    Assertions.assertEquals(
        expected,
        subscriptionService.calculatePremiumFromStartAndStop(start, stop, month, year),
        "Error in stop only in month case");

    start = LocalDate.parse("2024-01-10");
    stop = LocalDate.parse("2025-01-23");
    // case 3: start in tested month and stop not
    expected = month.length(Year.isLeap(year)) - start.getDayOfMonth();
    Assertions.assertEquals(
        expected,
        subscriptionService.calculatePremiumFromStartAndStop(start, stop, month, year),
        "Error in start only in month case");
  }
}
