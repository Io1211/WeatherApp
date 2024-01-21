package at.qe.skeleton.internal.services;

import static org.junit.jupiter.api.Assertions.*;

import at.qe.skeleton.internal.model.CreditCard;
import at.qe.skeleton.internal.model.Subscription;
import at.qe.skeleton.internal.model.Userx;
import at.qe.skeleton.internal.repositories.CreditCardRepository;
import at.qe.skeleton.internal.repositories.FavoriteRepository;
import at.qe.skeleton.internal.repositories.SubscriptionRepository;
import at.qe.skeleton.internal.repositories.UserxRepository;
import at.qe.skeleton.internal.services.exceptions.NoCreditCardFoundException;
import at.qe.skeleton.internal.services.exceptions.NotYetAvailableException;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.misc.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.web.WebAppConfiguration;

/** SubscriptionServiceTest */
@SpringBootTest
@WebAppConfiguration
class SubscriptionServiceTest {

  @Autowired CreditCardRepository creditCardRepository;

  @Autowired FavoriteRepository favoriteRepository;

  @Autowired UserxRepository userxRepository;

  @Autowired SubscriptionRepository subscriptionRepository;

  @Autowired SubscriptionService subscriptionService;

  @DirtiesContext
  @Test
  void activatePremiumSubscriptionTest() throws NoCreditCardFoundException {
    Userx user = new Userx();
    user.setId("Primus");

    // case 1: throw custom exception when there is no credit card set for the user trying to get
    // premium
    Userx finalUser = user;
    assertThrows(
        NoCreditCardFoundException.class,
        () -> {
          subscriptionService.activatePremiumSubscription(finalUser);
        });

    // set credit card info to prevent exceptions in following tests
    user.setCreditCard(new CreditCard());
    user.getCreditCard().setUserId(user);
    user.setFavorites(new ArrayList<>()); // necessary because of the rules of the user entity
    user = userxRepository.save(user);

    // case 2: no prior subscription on record
    subscriptionService.activatePremiumSubscription(user);
    assertNotNull(user.getSubscription());
    assertNotNull(user.getSubscription().getPremiumPeriod());
    assertEquals(1, user.getSubscription().getPremiumPeriod().size());
    Pair<LocalDate, LocalDate> premiumPeriod = user.getSubscription().getPremiumPeriod().get(0);
    // only compares date and not time so this should be fine
    assertTrue(premiumPeriod.a.isEqual(LocalDate.now()));

    // case 3: add to already existing list of premium periods
    List<Pair<LocalDate, LocalDate>> premiumPeriods = new ArrayList<>();
    premiumPeriods.add(new Pair<>(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 2, 1)));
    premiumPeriods.add(new Pair<>(LocalDate.of(2023, 3, 1), LocalDate.of(2023, 4, 1)));
    premiumPeriods.add(new Pair<>(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 5)));
    // overwrites the value from the previous test case
    user.getSubscription().setPremiumPeriod(premiumPeriods);
    userxRepository.save(user);

    subscriptionService.activatePremiumSubscription(user);
    // 3 entries created in setup + 1 that is set upon activation = 4 subscription periods expected
    assertEquals(4, user.getSubscription().getPremiumPeriod().size());
    Pair<LocalDate, LocalDate> lastPremiumPeriod =
        user.getSubscription().getPremiumPeriod().get(premiumPeriods.size() - 1);
    // only compares date and not time so this should be fine
    assertTrue(lastPremiumPeriod.a.isEqual(LocalDate.now()));
  }

  @DirtiesContext
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
    assertEquals(expected, subscriptionService.premiumDaysInMonth(user, Month.FEBRUARY, 2023));

    // case 2: start not in month but end is
    expected = LocalDate.of(2023, 10, 10).getDayOfMonth();
    assertEquals(expected, subscriptionService.premiumDaysInMonth(user, Month.OCTOBER, 2023));

    // case 3: both start and end in month
    activePeriod =
        new ArrayList<>(List.of(new Pair<>(LocalDate.of(2023, 5, 5), LocalDate.of(2023, 5, 10))));
    subscription.setPremiumPeriod(activePeriod);
    expected = LocalDate.of(2023, 5, 10).getDayOfMonth() - LocalDate.of(2023, 5, 5).getDayOfMonth();
    assertEquals(expected, subscriptionService.premiumDaysInMonth(user, Month.MAY, 2023));

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
    assertEquals(expected, subscriptionService.premiumDaysInMonth(user, Month.MAY, 2023));

    // case 5: subscription started and not stopped (date outside the query month)
    activePeriod = new ArrayList<>(List.of(new Pair<>(LocalDate.of(2023, 3, 5), null)));
    subscription.setPremiumPeriod(activePeriod);
    expected = Month.APRIL.length(Year.isLeap(2023));
    assertEquals(expected, subscriptionService.premiumDaysInMonth(user, Month.APRIL, 2023));

    // case 6: subscription started and stopped (date before or after the query month)
    activePeriod =
        new ArrayList<>(
            List.of(
                new Pair<>(LocalDate.of(2023, 5, 5), LocalDate.of(2023, 5, 10)),
                new Pair<>(LocalDate.of(2023, 8, 15), LocalDate.of(2023, 8, 27))));
    subscription.setPremiumPeriod(activePeriod);
    expected = 0;
    assertEquals(expected, subscriptionService.premiumDaysInMonth(user, Month.JUNE, 2023));
    activePeriod =
        new ArrayList<>(List.of(new Pair<>(LocalDate.of(2023, 5, 5), LocalDate.of(2023, 8, 10))));
    subscription.setPremiumPeriod(activePeriod);
    expected = Month.JUNE.length(Year.isLeap(2023));
    assertEquals(expected, subscriptionService.premiumDaysInMonth(user, Month.JUNE, 2023));

    // case 7: start in month but stop not set (=null)
    activePeriod = new ArrayList<>(List.of(new Pair<>(LocalDate.of(2023, 5, 5), null)));
    subscription.setPremiumPeriod(activePeriod);
    expected = Month.MAY.length(Year.isLeap(2023)) - LocalDate.of(2023, 5, 5).getDayOfMonth();
    assertEquals(expected, subscriptionService.premiumDaysInMonth(user, Month.MAY, 2023));

    // case 8: only one tuple in subscription service (necessary because you can't sort a list of
    // only one tuple (see getLastSubscriptionBeforeMonth)
    activePeriod = new ArrayList<>(List.of(new Pair<>(LocalDate.of(2023, 5, 5), null)));
    subscription.setPremiumPeriod(activePeriod);
    expected = Month.JUNE.length(Year.isLeap(2023));
    assertEquals(expected, subscriptionService.premiumDaysInMonth(user, Month.JUNE, 2023));
    expected = 0;
    assertEquals(expected, subscriptionService.premiumDaysInMonth(user, Month.JANUARY, 2023));

    // case 9: search for the current or a future month doesn't work
    Userx finalUser = user;
    assertThrows(
        NotYetAvailableException.class,
        () -> {
          Month thisMonth = LocalDate.now().getMonth();
          int thisYear = LocalDate.now().getYear();
          subscriptionService.premiumDaysInMonth(finalUser, thisMonth, thisYear);
        });
    assertThrows(
        NotYetAvailableException.class,
        () -> subscriptionService.premiumDaysInMonth(finalUser, Month.JANUARY, 2025));
    if (ZonedDateTime.now().getMonth()
        != Month.JANUARY) { // this doesn't work for the first month of the year
      assertDoesNotThrow(
          () -> {
            Month previousMonth = LocalDate.now().getMonth().minus(1);
            int thisYear = LocalDate.now().getYear();
            finalUser
                .getSubscription()
                .setPremiumPeriod(
                    new ArrayList<>(
                        List.of(
                            new Pair<>(
                                LocalDate.of(thisYear, previousMonth, 1),
                                LocalDate.of(thisYear, previousMonth, 5)))));
            subscriptionService.premiumDaysInMonth(finalUser, previousMonth, thisYear);
          });
    }

    // case 10: user premium period is empty
    user.getSubscription().setPremiumPeriod(new ArrayList<>());
    assertEquals(0, subscriptionService.premiumDaysInMonth(user, Month.JANUARY, 2023));

    // cleanup
    userxRepository.delete(user);
  }

  @Test
  void calculatePremiumFromStartAndStopTest() {
    Month month = Month.JANUARY;
    int year = 2024;

    LocalDate start = LocalDate.of(2024, 1, 10);
    LocalDate stop = LocalDate.of(2024, 1, 23);

    // case 1: both start and stop date in the tested month
    int expected = stop.getDayOfMonth() - start.getDayOfMonth();
    assertEquals(
        expected,
        subscriptionService.calculatePremiumFromStartAndStop(start, stop, month, year),
        "Error in start & stop in month case");

    // case 2: start not in tested month and stop in tested month
    start = LocalDate.of(2023, 1, 10);
    expected = stop.getDayOfMonth();
    assertEquals(
        expected,
        subscriptionService.calculatePremiumFromStartAndStop(start, stop, month, year),
        "Error in stop only in month case");

    // case 3: start in tested month and stop not
    start = LocalDate.of(2024, 1, 10);
    stop = LocalDate.of(2025, 1, 23);
    expected = month.length(Year.isLeap(year)) - start.getDayOfMonth();
    assertEquals(
        expected,
        subscriptionService.calculatePremiumFromStartAndStop(start, stop, month, year),
        "Error in start only in month case");

    // case 4: start in month and stop not set
    start = LocalDate.of(2024, 1, 10);
    stop = null;
    expected = month.length(Year.isLeap(year)) - start.getDayOfMonth();
    assertEquals(
        expected,
        subscriptionService.calculatePremiumFromStartAndStop(start, stop, month, year),
        "Error in start only in month case with (stop not set)");
  }

  @Test
  void getLastSubscriptionBeforeMonthTest() {
    // case 1: method called with an empty list
    List<Pair<LocalDate, LocalDate>> finalSubscriptionPeriods = new ArrayList<>();
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          subscriptionService.getLastSubscriptionBeforeMonth(
              finalSubscriptionPeriods, Month.JANUARY, 2024);
        });

    List<Pair<LocalDate, LocalDate>> subscriptionPeriods = new ArrayList<>();
    LocalDate start = LocalDate.of(2024, 4, 1);
    LocalDate stop = LocalDate.of(2024, 9, 1);
    subscriptionPeriods.add(new Pair<>(start, stop));

    // case 2: there is only one subscription in the list (necessary because the method uses sorting
    // to efficiently get the result, but sorting can't be done with a list of one entry)
    assertNull(
        subscriptionService.getLastSubscriptionBeforeMonth(
            subscriptionPeriods, Month.JANUARY, 2024));
    assertNull(
        subscriptionService.getLastSubscriptionBeforeMonth(
            subscriptionPeriods, Month.JANUARY, 2024));
    Pair<LocalDate, LocalDate> expected = new Pair<>(start, stop);
    assertEquals(
        expected,
        subscriptionService.getLastSubscriptionBeforeMonth(subscriptionPeriods, Month.JUNE, 2024));

    // case 3: many dates in list. Find the correct one.
    subscriptionPeriods =
        new ArrayList<>(
            List.of(
                new Pair<>(LocalDate.of(2024, 1, 1), null),
                new Pair<>(LocalDate.of(2024, 2, 1), null),
                new Pair<>(LocalDate.of(2024, 3, 1), null),
                new Pair<>(LocalDate.of(2024, 4, 1), null),
                new Pair<>(LocalDate.of(2024, 5, 1), null)));
    expected = new Pair<>(LocalDate.of(2024, 3, 1), null);
    assertEquals(
        expected,
        subscriptionService.getLastSubscriptionBeforeMonth(subscriptionPeriods, Month.APRIL, 2024));
  }

  @Test
  void isInMonthTest() {
    assertTrue(subscriptionService.isInMonth(LocalDate.of(2024, 1, 1), Month.JANUARY, 2024));
    assertFalse(subscriptionService.isInMonth(LocalDate.of(2024, 1, 1), Month.JANUARY, 2025));
    assertFalse(subscriptionService.isInMonth(LocalDate.of(2024, 1, 1), Month.FEBRUARY, 2024));
  }

  @Test
  void customComparatorTest() {
    // always compares only the start date of tuples i.e., a in a tuple (a, b).
    SubscriptionService.DatePairComparator comparator =
        new SubscriptionService.DatePairComparator();

    // a == b
    assertEquals(
        0,
        comparator.compare(
            new Pair<>(LocalDate.of(2023, 5, 5), LocalDate.of(2023, 5, 10)),
            new Pair<>(LocalDate.of(2023, 5, 5), LocalDate.of(2023, 5, 27))));

    // a > b (a after b)
    assertEquals(
        1,
        comparator.compare(
            new Pair<>(LocalDate.of(2023, 8, 5), LocalDate.of(2023, 5, 10)),
            new Pair<>(LocalDate.of(2023, 5, 15), LocalDate.of(2023, 5, 27))));

    // a < b (a before b)
    assertEquals(
        -1,
        comparator.compare(
            new Pair<>(LocalDate.of(2023, 5, 5), LocalDate.of(2023, 5, 10)),
            new Pair<>(LocalDate.of(2023, 8, 15), LocalDate.of(2023, 5, 27))));
  }
}
