package at.qe.skeleton.internal.services;

import at.qe.skeleton.internal.model.Subscription;
import at.qe.skeleton.internal.model.Userx;
import at.qe.skeleton.internal.repositories.FavoriteRepository;
import at.qe.skeleton.internal.repositories.SubscriptionRepository;
import at.qe.skeleton.internal.repositories.UserxRepository;
import at.qe.skeleton.internal.services.exceptions.NotYetAvailableException;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.misc.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

/** SubscriptionServiceTest */
@SpringBootTest
@WebAppConfiguration
class SubscriptionServiceTest {

  @Autowired FavoriteRepository favoriteRepository;

  @Autowired UserxRepository userxRepository;

  @Autowired SubscriptionRepository subscriptionRepository;

  @Autowired SubscriptionService subscriptionService;

  @Test
  void calculatePremiumFromStartAndStopTest() {
    Month month = Month.JANUARY;
    int year = 2024;

    LocalDate start = LocalDate.of(2024, 1, 10);
    LocalDate stop = LocalDate.of(2024, 1, 23);

    // case 1: both start and stop date in the tested month
    int expected = stop.getDayOfMonth() - start.getDayOfMonth();
    Assertions.assertEquals(
        expected,
        subscriptionService.calculatePremiumFromStartAndStop(start, stop, month, year),
        "Error in start & stop in month case");

    // case 2: start not in tested month and stop in tested month
    start = LocalDate.of(2023, 1, 10);
    expected = stop.getDayOfMonth();
    Assertions.assertEquals(
        expected,
        subscriptionService.calculatePremiumFromStartAndStop(start, stop, month, year),
        "Error in stop only in month case");

    // case 3: start in tested month and stop not
    start = LocalDate.of(2024, 1, 10);
    stop = LocalDate.of(2025, 1, 23);
    expected = month.length(Year.isLeap(year)) - start.getDayOfMonth();
    Assertions.assertEquals(
        expected,
        subscriptionService.calculatePremiumFromStartAndStop(start, stop, month, year),
        "Error in start only in month case");

    // case 4: start in month and stop not set
    start = LocalDate.of(2024, 1, 10);
    stop = null;
    expected = month.length(Year.isLeap(year)) - start.getDayOfMonth();
    Assertions.assertEquals(
        expected,
        subscriptionService.calculatePremiumFromStartAndStop(start, stop, month, year),
        "Error in start only in month case with (stop not set)");
  }

  @Test
  void premiumDaysInMonthTest() throws NotYetAvailableException {
    Userx user = new Userx();
    user.setId("Primus");
    Subscription subscription = new Subscription();

    ArrayList<Pair<LocalDate, LocalDate>> activePeriod =
        new ArrayList<>(List.of(new Pair<>(LocalDate.of(2023, 2, 5), LocalDate.of(2023, 10, 10))));
    subscription.setPremiumPeriod(activePeriod);
    user.setSubscription(subscription);
    user = userxRepository.save(user);

    // case 1: start in month but end not

    // the day of signup itself is never included in the calculation. This way, if you sign up the
    // 1st of the month of 31 days you pay 30 days and if you signed up the last day of last month
    // you pay 31.
    int expected =
        LocalDate.of(2023, 2, 5).getMonth().length(Year.isLeap(2023))
            - LocalDate.of(2023, 2, 5).getDayOfMonth();
    Assertions.assertEquals(
        expected, subscriptionService.premiumDaysInMonth(user, Month.FEBRUARY, 2023));

    // case 2: start not in month but end is
    expected = LocalDate.of(2023, 10, 10).getDayOfMonth();
    Assertions.assertEquals(
        expected, subscriptionService.premiumDaysInMonth(user, Month.OCTOBER, 2023));

    // case 3: both start and end in month
    activePeriod =
        new ArrayList<>(List.of(new Pair<>(LocalDate.of(2023, 5, 5), LocalDate.of(2023, 5, 10))));
    subscription.setPremiumPeriod(activePeriod);
    expected = LocalDate.of(2023, 5, 10).getDayOfMonth() - LocalDate.of(2023, 5, 5).getDayOfMonth();
    Assertions.assertEquals(
        expected, subscriptionService.premiumDaysInMonth(user, Month.MAY, 2023));

    // case 4: multiple start and stops in one month
    activePeriod =
        new ArrayList<>(
            List.of(
                new Pair<>(LocalDate.of(2023, 5, 5), LocalDate.of(2023, 5, 10)),
                new Pair<>(LocalDate.of(2023, 5, 15), LocalDate.of(2023, 5, 27))));
    subscription.setPremiumPeriod(activePeriod);
    expected =
        (LocalDate.of(2023, 5, 10).getDayOfMonth() - LocalDate.of(2023, 5, 5).getDayOfMonth())
            + (LocalDate.of(2023, 5, 27).getDayOfMonth()
                - LocalDate.of(2023, 5, 15).getDayOfMonth());
    Assertions.assertEquals(
        expected, subscriptionService.premiumDaysInMonth(user, Month.MAY, 2023));

    // case 5: subscription started and not stopped (date outside the query month)
    activePeriod = new ArrayList<>(List.of(new Pair<>(LocalDate.of(2023, 3, 5), null)));
    subscription.setPremiumPeriod(activePeriod);
    expected = Month.APRIL.length(Year.isLeap(2023));
    Assertions.assertEquals(
        expected, subscriptionService.premiumDaysInMonth(user, Month.APRIL, 2023));

    // case 6: subscription started and stopped (date outside the query month)
    activePeriod =
        new ArrayList<>(
            List.of(
                new Pair<>(LocalDate.of(2023, 5, 5), LocalDate.of(2023, 5, 10)),
                new Pair<>(LocalDate.of(2023, 8, 15), LocalDate.of(2023, 8, 27))));
    subscription.setPremiumPeriod(activePeriod);
    expected = 0;
    Assertions.assertEquals(
        expected, subscriptionService.premiumDaysInMonth(user, Month.JUNE, 2023));

    // case 7: start in month but stop not set (=null)
    activePeriod = new ArrayList<>(List.of(new Pair<>(LocalDate.of(2023, 5, 5), null)));
    subscription.setPremiumPeriod(activePeriod);
    expected = Month.MAY.length(Year.isLeap(2023)) - LocalDate.of(2023, 5, 5).getDayOfMonth();
    Assertions.assertEquals(
        expected, subscriptionService.premiumDaysInMonth(user, Month.MAY, 2023));

    // case 8: search for the current or a future month
    Userx finalUser = user;
    Assertions.assertThrows(
        NotYetAvailableException.class,
        () -> {
          Month thisMonth = LocalDate.now().getMonth();
          int thisYear = LocalDate.now().getYear();
          subscriptionService.premiumDaysInMonth(finalUser, thisMonth, thisYear);
        });
    Assertions.assertThrows(
        NotYetAvailableException.class,
        () -> subscriptionService.premiumDaysInMonth(finalUser, Month.JANUARY, 2025));

    // cleanup
    userxRepository.delete(user);
  }
}
