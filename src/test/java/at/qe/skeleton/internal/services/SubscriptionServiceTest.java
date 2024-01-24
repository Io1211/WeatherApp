package at.qe.skeleton.internal.services;

import static org.junit.jupiter.api.Assertions.*;

import at.qe.skeleton.internal.model.*;
import at.qe.skeleton.internal.repositories.CreditCardRepository;
import at.qe.skeleton.internal.repositories.FavoriteRepository;
import at.qe.skeleton.internal.repositories.SubscriptionRepository;
import at.qe.skeleton.internal.repositories.UserxRepository;
import at.qe.skeleton.internal.services.exceptions.*;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
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

  public SubscriptionPeriod buildSubscription(LocalDate start, LocalDate stop, boolean active) {
    SubscriptionPeriod subscriptionPeriod = new SubscriptionPeriod();
    subscriptionPeriod.setStart(start);
    subscriptionPeriod.setStop(stop);
    subscriptionPeriod.setActive(active);
    return subscriptionPeriod;
  }

  @DirtiesContext
  @Test
  void activatePremiumSubscriptionTest() throws NoCreditCardFoundException {
    Userx user = new Userx();
    user.setId("Primus");
    TreeSet<UserxRole> roles = new TreeSet<>();
    roles.add(UserxRole.REGISTERED_USER);
    user.setRoles(roles);

    // case 1: throw custom exception when there is no credit card set for the user trying to get
    // premium
    Userx finalUser = user;
    assertThrows(
        NoCreditCardFoundException.class,
        () -> subscriptionService.activatePremiumSubscription(finalUser));

    // set credit card info to prevent exceptions in following tests
    user.setCreditCard(new CreditCard());
    user.getCreditCard().setUserId(user);
    user.setFavorites(new ArrayList<>()); // necessary because of restrictions set in the entity
    user = userxRepository.save(user);

    // case 2: no prior subscription on record
    user.getRoles().remove(UserxRole.PREMIUM_USER);
    assertFalse(user.getRoles().contains(UserxRole.PREMIUM_USER));
    subscriptionService.activatePremiumSubscription(user);
    assertNotNull(user.getSubscription());
    assertEquals(LocalDate.now(), user.getSubscription().getSignupDate());
    assertNotNull(user.getSubscription().getSubscriptionPeriods());
    assertEquals(1, user.getSubscription().getSubscriptionPeriods().size());
    SubscriptionPeriod premiumPeriod =
        user.getSubscription().getSubscriptionPeriods().stream()
            .filter(SubscriptionPeriod::isActive)
            .findFirst()
            .orElseThrow(NullPointerException::new);
    // only compares date and not time so this should be fine
    assertTrue(premiumPeriod.getStart().isEqual(LocalDate.now()));
    assertTrue(
        user.getRoles().contains(UserxRole.PREMIUM_USER),
        "A problem occurred in the activatePremium method of the UserxService.");

    // case 3: add to already existing list of premium periods
    user.getRoles().remove(UserxRole.PREMIUM_USER);
    assertFalse(user.getRoles().contains(UserxRole.PREMIUM_USER));
    SubscriptionPeriod subscriptionPeriod1 =
        buildSubscription(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 2, 1), false);
    SubscriptionPeriod subscriptionPeriod2 =
        buildSubscription(LocalDate.of(2023, 3, 1), LocalDate.of(2023, 4, 1), false);
    SubscriptionPeriod subscriptionPeriod3 =
        buildSubscription(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 5), false);
    List<SubscriptionPeriod> premiumPeriods =
        new ArrayList<>(List.of(subscriptionPeriod1, subscriptionPeriod2, subscriptionPeriod3));
    // overwrites the value from the previous test case
    user.getSubscription().setSubscriptionPeriods(premiumPeriods);
    userxRepository.save(user);

    subscriptionService.activatePremiumSubscription(user);
    // 3 entries created in setup + 1 that is set upon activation = 4 subscription periods expected
    assertEquals(4, user.getSubscription().getSubscriptionPeriods().size());
    SubscriptionPeriod lastPremiumPeriod =
        user.getSubscription().getSubscriptionPeriods().get(premiumPeriods.size() - 1);
    // only compares date and not time so this should be fine
    assertTrue(lastPremiumPeriod.getStart().isEqual(LocalDate.now()));
    assertTrue(
        user.getRoles().contains(UserxRole.PREMIUM_USER),
        "A problem occurred in the activatePremium method of the UserxService.");
  }

  @DirtiesContext
  @Test
  void deactivatePremiumSubscriptionTest()
      throws NoSubscriptionFoundException,
          MoneyGlitchAvoidanceException,
          NoActivePremiumSubscriptionFoundException {
    // case 1: user has no subscription and tries to deactivate premium
    assertThrows(
        NoSubscriptionFoundException.class,
        () -> {
          Userx user = new Userx();
          subscriptionService.deactivatePremiumSubscription(user);
        });

    // case 2: user has a subscription but no premium period on record and thus to terminate
    Userx user = new Userx();
    user.setId("Primus");
    TreeSet<UserxRole> roles = new TreeSet<>();
    roles.add(UserxRole.REGISTERED_USER);
    user.setRoles(roles);
    user.setSubscription(new Subscription());

    final Userx finalUser1 = user;
    assertThrows(
        NoActivePremiumSubscriptionFoundException.class,
        () -> subscriptionService.deactivatePremiumSubscription(finalUser1));

    // case 3: user has a subscription but the last premium period was already terminated so there
    // is nothing to cancel
    user.getSubscription().setSubscriptionPeriods(new ArrayList<>());
    final Userx finalUser2 = user;
    assertThrows(
        NoActivePremiumSubscriptionFoundException.class,
        () -> {
          SubscriptionPeriod subscriptionPeriod =
              buildSubscription(
                  LocalDate.now().minus(Period.of(0, 0, 5)),
                  LocalDate.now().minus(Period.of(0, 0, 3)),
                  false);
          finalUser2
              .getSubscription()
              .setSubscriptionPeriods(new ArrayList<>(List.of(subscriptionPeriod)));
          subscriptionService.deactivatePremiumSubscription(finalUser2);
        });

    // case 4: user tries to cancel the membership the day it was started (we want to avoid this
    // because the current billing would not charge the user. Thus, a malicious user could sign up
    // in the morning and cancel in the evening and never pay a dime)
    final Userx finalUser3 = user;
    assertThrows(
        MoneyGlitchAvoidanceException.class,
        () -> {
          SubscriptionPeriod subscriptionPeriod = buildSubscription(LocalDate.now(), null, true);
          finalUser3
              .getSubscription()
              .setSubscriptionPeriods(new ArrayList<>(List.of(subscriptionPeriod)));
          subscriptionService.deactivatePremiumSubscription(finalUser3);
        });

    // case 5: user has an active membership and tries to cancel it
    SubscriptionPeriod subscriptionPeriod =
        buildSubscription(LocalDate.now().minus(Period.of(0, 0, 3)), null, true);
    user.getSubscription().setSubscriptionPeriods(new ArrayList<>(List.of(subscriptionPeriod)));
    user.addRole(UserxRole.PREMIUM_USER);
    assertTrue(user.getRoles().contains(UserxRole.PREMIUM_USER));

    subscriptionService.deactivatePremiumSubscription(user);

    SubscriptionPeriod expected =
        buildSubscription(LocalDate.now().minus(Period.of(0, 0, 3)), LocalDate.now(), true);
    assertEquals(
        expected,
        user.getSubscription().getSubscriptionPeriods().stream()
            .filter(SubscriptionPeriod::isActive)
            .findFirst()
            .orElseThrow(NullPointerException::new));
    assertFalse(
        user.getRoles().contains(UserxRole.PREMIUM_USER),
        "There is a problem with the deactivatePremium method of the UserxService.");
  }

  //  @DirtiesContext
  //  @Test
  //  void premiumDaysInMonthTest() throws NotYetAvailableException {
  //    Userx user = new Userx();
  //    user.setId("Primus");
  //    Subscription subscription = new Subscription();
  //
  //    ArrayList<Pair<LocalDate, LocalDate>> activePeriod =
  //        new ArrayList<>(List.of(new Pair<>(LocalDate.of(2023, 2, 5), LocalDate.of(2023, 10,
  // 10))));
  //    subscription.setPremiumPeriod(activePeriod);
  //    user.setSubscription(subscription);
  //    user = userxRepository.save(user);
  //
  //    // case 1: start in month but end not
  //
  //    // the day of signup itself is never included in the calculation. This way, if you sign up
  // the
  //    // 1st of the month of 31 days you pay 30 days and if you signed up the last day of last
  // month
  //    // you pay 31.
  //    int expected =
  //        LocalDate.of(2023, 2, 5).getMonth().length(Year.isLeap(2023))
  //            - LocalDate.of(2023, 2, 5).getDayOfMonth();
  //    assertEquals(expected, subscriptionService.premiumDaysInMonth(user, Month.FEBRUARY, 2023));
  //
  //    // case 2: start not in month but end is
  //    expected = LocalDate.of(2023, 10, 10).getDayOfMonth();
  //    assertEquals(expected, subscriptionService.premiumDaysInMonth(user, Month.OCTOBER, 2023));
  //
  //    // case 3: both start and end in month
  //    activePeriod =
  //        new ArrayList<>(List.of(new Pair<>(LocalDate.of(2023, 5, 5), LocalDate.of(2023, 5,
  // 10))));
  //    subscription.setPremiumPeriod(activePeriod);
  //    expected = LocalDate.of(2023, 5, 10).getDayOfMonth() - LocalDate.of(2023, 5,
  // 5).getDayOfMonth();
  //    assertEquals(expected, subscriptionService.premiumDaysInMonth(user, Month.MAY, 2023));
  //
  //    // case 4: multiple start and stops in one month
  //    activePeriod =
  //        new ArrayList<>(
  //            List.of(
  //                new Pair<>(LocalDate.of(2023, 5, 5), LocalDate.of(2023, 5, 10)),
  //                new Pair<>(LocalDate.of(2023, 5, 15), LocalDate.of(2023, 5, 27))));
  //    subscription.setPremiumPeriod(activePeriod);
  //    expected =
  //        (LocalDate.of(2023, 5, 10).getDayOfMonth() - LocalDate.of(2023, 5, 5).getDayOfMonth())
  //            + (LocalDate.of(2023, 5, 27).getDayOfMonth()
  //                - LocalDate.of(2023, 5, 15).getDayOfMonth());
  //    assertEquals(expected, subscriptionService.premiumDaysInMonth(user, Month.MAY, 2023));
  //
  //    // case 5: subscription started and not stopped (date outside the query month)
  //    activePeriod = new ArrayList<>(List.of(new Pair<>(LocalDate.of(2023, 3, 5), null)));
  //    subscription.setPremiumPeriod(activePeriod);
  //    expected = Month.APRIL.length(Year.isLeap(2023));
  //    assertEquals(expected, subscriptionService.premiumDaysInMonth(user, Month.APRIL, 2023));
  //
  //    // case 6: subscription started and stopped (date before or after the query month)
  //    activePeriod =
  //        new ArrayList<>(
  //            List.of(
  //                new Pair<>(LocalDate.of(2023, 5, 5), LocalDate.of(2023, 5, 10)),
  //                new Pair<>(LocalDate.of(2023, 8, 15), LocalDate.of(2023, 8, 27))));
  //    subscription.setPremiumPeriod(activePeriod);
  //    expected = 0;
  //    assertEquals(expected, subscriptionService.premiumDaysInMonth(user, Month.JUNE, 2023));
  //    activePeriod =
  //        new ArrayList<>(List.of(new Pair<>(LocalDate.of(2023, 5, 5), LocalDate.of(2023, 8,
  // 10))));
  //    subscription.setPremiumPeriod(activePeriod);
  //    expected = Month.JUNE.length(Year.isLeap(2023));
  //    assertEquals(expected, subscriptionService.premiumDaysInMonth(user, Month.JUNE, 2023));
  //
  //    // case 7: start in month but stop not set (=null)
  //    activePeriod = new ArrayList<>(List.of(new Pair<>(LocalDate.of(2023, 5, 5), null)));
  //    subscription.setPremiumPeriod(activePeriod);
  //    expected = Month.MAY.length(Year.isLeap(2023)) - LocalDate.of(2023, 5, 5).getDayOfMonth();
  //    assertEquals(expected, subscriptionService.premiumDaysInMonth(user, Month.MAY, 2023));
  //
  //    // case 8: only one tuple in subscription service (necessary because you can't sort a list
  // of
  //    // only one tuple (see getLastSubscriptionBeforeMonth)
  //    activePeriod = new ArrayList<>(List.of(new Pair<>(LocalDate.of(2023, 5, 5), null)));
  //    subscription.setPremiumPeriod(activePeriod);
  //    expected = Month.JUNE.length(Year.isLeap(2023));
  //    assertEquals(expected, subscriptionService.premiumDaysInMonth(user, Month.JUNE, 2023));
  //    expected = 0;
  //    assertEquals(expected, subscriptionService.premiumDaysInMonth(user, Month.JANUARY, 2023));
  //
  //    // case 9: search for the current or a future month doesn't work
  //    Userx finalUser = user;
  //    assertThrows(
  //        NotYetAvailableException.class,
  //        () -> {
  //          Month thisMonth = LocalDate.now().getMonth();
  //          int thisYear = LocalDate.now().getYear();
  //          subscriptionService.premiumDaysInMonth(finalUser, thisMonth, thisYear);
  //        });
  //    assertThrows(
  //        NotYetAvailableException.class,
  //        () -> subscriptionService.premiumDaysInMonth(finalUser, Month.JANUARY, 2025));
  //    if (ZonedDateTime.now().getMonth()
  //        != Month.JANUARY) { // this doesn't work for the first month of the year
  //      assertDoesNotThrow(
  //          () -> {
  //            Month previousMonth = LocalDate.now().getMonth().minus(1);
  //            int thisYear = LocalDate.now().getYear();
  //            finalUser
  //                .getSubscription()
  //                .setPremiumPeriod(
  //                    new ArrayList<>(
  //                        List.of(
  //                            new Pair<>(
  //                                LocalDate.of(thisYear, previousMonth, 1),
  //                                LocalDate.of(thisYear, previousMonth, 5)))));
  //            subscriptionService.premiumDaysInMonth(finalUser, previousMonth, thisYear);
  //          });
  //    }
  //
  //    // case 10: user premium period is empty
  //    user.getSubscription().setPremiumPeriod(new ArrayList<>());
  //    assertEquals(0, subscriptionService.premiumDaysInMonth(user, Month.JANUARY, 2023));
  //
  //    // cleanup
  //    userxRepository.delete(user);
  //  }
  //
  //  @Test
  //  void calculatePremiumFromStartAndStopTest() {
  //    Month month = Month.JANUARY;
  //    int year = 2024;
  //
  //    LocalDate start = LocalDate.of(2024, 1, 10);
  //    LocalDate stop = LocalDate.of(2024, 1, 23);
  //
  //    // case 1: both start and stop date in the tested month
  //    int expected = stop.getDayOfMonth() - start.getDayOfMonth();
  //    assertEquals(
  //        expected,
  //        subscriptionService.calculatePremiumFromStartAndStop(start, stop, month, year),
  //        "Error in start & stop in month case");
  //
  //    // case 2: start not in tested month and stop in tested month
  //    start = LocalDate.of(2023, 1, 10);
  //    expected = stop.getDayOfMonth();
  //    assertEquals(
  //        expected,
  //        subscriptionService.calculatePremiumFromStartAndStop(start, stop, month, year),
  //        "Error in stop only in month case");
  //
  //    // case 3: start in tested month and stop not
  //    start = LocalDate.of(2024, 1, 10);
  //    stop = LocalDate.of(2025, 1, 23);
  //    expected = month.length(Year.isLeap(year)) - start.getDayOfMonth();
  //    assertEquals(
  //        expected,
  //        subscriptionService.calculatePremiumFromStartAndStop(start, stop, month, year),
  //        "Error in start only in month case");
  //
  //    // case 4: start in month and stop not set
  //    start = LocalDate.of(2024, 1, 10);
  //    stop = null;
  //    expected = month.length(Year.isLeap(year)) - start.getDayOfMonth();
  //    assertEquals(
  //        expected,
  //        subscriptionService.calculatePremiumFromStartAndStop(start, stop, month, year),
  //        "Error in start only in month case with (stop not set)");
  //  }
  //
  //  @Test
  //  void getLastSubscriptionBeforeMonthTest() {
  //    // case 1: method called with an empty list
  //    List<Pair<LocalDate, LocalDate>> finalSubscriptionPeriods = new ArrayList<>();
  //    assertThrows(
  //        IllegalArgumentException.class,
  //        () -> {
  //          subscriptionService.getLastSubscriptionBeforeMonth(
  //              finalSubscriptionPeriods, Month.JANUARY, 2024);
  //        });
  //
  //    List<Pair<LocalDate, LocalDate>> subscriptionPeriods = new ArrayList<>();
  //    LocalDate start = LocalDate.of(2024, 4, 1);
  //    LocalDate stop = LocalDate.of(2024, 9, 1);
  //    subscriptionPeriods.add(new Pair<>(start, stop));
  //
  //    // case 2: there is only one subscription in the list (necessary because the method uses
  // sorting
  //    // to efficiently get the result, but sorting can't be done with a list of one entry)
  //    assertNull(
  //        subscriptionService.getLastSubscriptionBeforeMonth(
  //            subscriptionPeriods, Month.JANUARY, 2024));
  //    assertNull(
  //        subscriptionService.getLastSubscriptionBeforeMonth(
  //            subscriptionPeriods, Month.JANUARY, 2024));
  //    Pair<LocalDate, LocalDate> expected = new Pair<>(start, stop);
  //    assertEquals(
  //        expected,
  //        subscriptionService.getLastSubscriptionBeforeMonth(subscriptionPeriods, Month.JUNE,
  // 2024));
  //
  //    // case 3: many dates in list. Find the correct one.
  //    subscriptionPeriods =
  //        new ArrayList<>(
  //            List.of(
  //                new Pair<>(LocalDate.of(2024, 1, 1), null),
  //                new Pair<>(LocalDate.of(2024, 2, 1), null),
  //                new Pair<>(LocalDate.of(2024, 3, 1), null),
  //                new Pair<>(LocalDate.of(2024, 4, 1), null),
  //                new Pair<>(LocalDate.of(2024, 5, 1), null)));
  //    expected = new Pair<>(LocalDate.of(2024, 3, 1), null);
  //    assertEquals(
  //        expected,
  //        subscriptionService.getLastSubscriptionBeforeMonth(subscriptionPeriods, Month.APRIL,
  // 2024));
  //  }
  //
  //  @Test
  //  void isInMonthTest() {
  //    assertTrue(subscriptionService.isInMonth(LocalDate.of(2024, 1, 1), Month.JANUARY, 2024));
  //    assertFalse(subscriptionService.isInMonth(LocalDate.of(2024, 1, 1), Month.JANUARY, 2025));
  //    assertFalse(subscriptionService.isInMonth(LocalDate.of(2024, 1, 1), Month.FEBRUARY, 2024));
  //  }
  //
  //  @Test
  //  void customComparatorTest() {
  //    // always compares only the start date of tuples i.e., a in a tuple (a, b).
  //    SubscriptionService.DatePairComparator comparator =
  //        new SubscriptionService.DatePairComparator();
  //
  //    // a == b
  //    assertEquals(
  //        0,
  //        comparator.compare(
  //            new Pair<>(LocalDate.of(2023, 5, 5), LocalDate.of(2023, 5, 10)),
  //            new Pair<>(LocalDate.of(2023, 5, 5), LocalDate.of(2023, 5, 27))));
  //
  //    // a > b (a after b)
  //    assertEquals(
  //        1,
  //        comparator.compare(
  //            new Pair<>(LocalDate.of(2023, 8, 5), LocalDate.of(2023, 5, 10)),
  //            new Pair<>(LocalDate.of(2023, 5, 15), LocalDate.of(2023, 5, 27))));
  //
  //    // a < b (a before b)
  //    assertEquals(
  //        -1,
  //        comparator.compare(
  //            new Pair<>(LocalDate.of(2023, 5, 5), LocalDate.of(2023, 5, 10)),
  //            new Pair<>(LocalDate.of(2023, 8, 15), LocalDate.of(2023, 5, 27))));
  //  }
}
