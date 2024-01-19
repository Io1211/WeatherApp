package at.qe.skeleton.internal.services;

import at.qe.skeleton.internal.model.Userx;
import at.qe.skeleton.internal.repositories.CreditCardRepository;
import at.qe.skeleton.internal.services.exceptions.NoCreditCardFoundException;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionService {

  @Autowired private UserxService userxService;

  @Autowired private CreditCardRepository creditCardRepository;

  public void activatePremiumSubscription(Userx user) throws NoCreditCardFoundException {
    if (creditCardRepository.findByUserId_Username(user.getUsername()) == null) {
      throw new NoCreditCardFoundException("No credit card found");
    }
    // TODO: set subscription start (create a new subscriptionPeriod tuple and set the start date.
    // The end stays null until set in deactivate)
    userxService.activatePremium(user);
  }

  public void deactivatePremiumSubscription(Userx user) {
    // TODO: set subscription end (get the list of subscriptionPeriod tuples and get the last one.
    // Set the date there)
    userxService.deactivatePremium(user);
  }

  public int premiumDaysInMonth(Userx user, Month month, int year) {
    // Assumes the start of a subscription period (pair<start, stop> cannot be null cuz it doesn't
    // make sense to have a subscription end but not start)
    if ((year >= ZonedDateTime.now().getYear())
        || ((year == ZonedDateTime.now().getYear())
            && month.getValue() >= ZonedDateTime.now().getMonthValue())) {
      throw new RuntimeException("Billing info is only available for the past months");
    }

    if (user.getSubscription().getPremiumPeriod().isEmpty()) {
      return 0;
    }

    // This collects all the subscription tuples that start or stop in the queried month.
    // Makes it easy to calculate the premium period if such tuples are found.
    // Otherwise, if no such tuples are found, the empty list is handled below.
    List<Pair<LocalDate, LocalDate>> premiumPeriodInMonth =
        user.getSubscription().getPremiumPeriod().stream()
            .filter(pair -> isInMonth(pair.a, month, year) || isInMonth(pair.b, month, year))
            .toList();

    // This handles the case where neither start nor stop of a subscription is in the queried month.
    // If there is no end set to the subscription (.b == null), return the length of the month
    // because the subscription was still active in that month.
    // If an end is set, it means that the subscription wasn't active during the query month ->
    // return 0.
    if (premiumPeriodInMonth.isEmpty()) {
      Pair<LocalDate, LocalDate> lastSubscriptionBeforeMonth =
          getLastSubscriptionBeforeMonth(user.getSubscription().getPremiumPeriod(), month, year);
      if (lastSubscriptionBeforeMonth.b == null) {
        return month.length(Year.isLeap(year));
      } else if (lastSubscriptionBeforeMonth.b.isAfter(LocalDate.of(year, month, 1))) {
        return month.length(Year.isLeap(year));
      } else if (lastSubscriptionBeforeMonth.b.isBefore(LocalDate.of(year, month, 1))) {
        return 0;
      }
    }

    // Calculate the days of active premium during the query month. Sum if multiple start and stops
    // were found.
    return premiumPeriodInMonth.stream()
        .map(pair -> calculatePremiumFromStartAndStop(pair.a, pair.b, month, year))
        .reduce(0, Integer::sum);
  }

  public int calculatePremiumFromStartAndStop(
      LocalDate start, LocalDate stop, Month month, int year) {

    // case 1: start in month, stop not
    if (isInMonth(start, month, year) && !isInMonth(stop, month, year)) {
      return month.length(Year.isLeap(year)) - start.getDayOfMonth();
    }
    // case 2: start & stop in month
    else if (isInMonth(start, month, year) && isInMonth(stop, month, year)) {
      return stop.getDayOfMonth() - start.getDayOfMonth();
    }
    // case 3: start not in month, stop in month
    else {
      return stop.getDayOfMonth();
    }
  }

  /**
   * Fetches the last subscription tuple with start date before the query month. This is used to
   * determine whether the subscription was cancelled or not when the start/end date are outside the
   * query month. subscriptionPeriods should be a mutable list, since it is sorted (throws error
   * otherwise)!
   *
   * @param subscriptionPeriods All the subscription periods a user was signed up for.
   * @param month The month for which to calculate the billing.
   * @param year The year of the month for which to calculate the billing.
   * @return The tuple with the subscription start that is closest to the query month but still
   *     before it
   */
  public Pair<LocalDate, LocalDate> getLastSubscriptionBeforeMonth(
      List<Pair<LocalDate, LocalDate>> subscriptionPeriods, Month month, int year) {

    if (subscriptionPeriods.size() == 1) {
      return subscriptionPeriods.get(0);
    }

    // The list of subscription dates is sorted by the start date.
    // Iterating over the sorted list and setting the extracted tuple every time until the start
    // date is after the query month yields the sought tuple.
    subscriptionPeriods.sort(new DatePairComparator());
    Pair<LocalDate, LocalDate> lastSubscriptionBeforeMonth = null;
    for (Pair<LocalDate, LocalDate> subscriptionPeriod : subscriptionPeriods) {
      if (subscriptionPeriod.a.isBefore(LocalDate.of(year, month.getValue(), 1))) {
        lastSubscriptionBeforeMonth = subscriptionPeriod;
      } else {
        break;
      }
    }
    return lastSubscriptionBeforeMonth;
  }

  public boolean isInMonth(LocalDate date, Month month, int year) {
    return date != null && date.getMonth() == month && date.getYear() == year;
  }

  static class DatePairComparator implements Comparator<Pair<LocalDate, LocalDate>> {
    @Override
    public int compare(Pair<LocalDate, LocalDate> pair1, Pair<LocalDate, LocalDate> pair2) {
      if (pair1.a.isBefore(pair2.a)) {
        return -1;
      } else if (pair1.a.isEqual(pair2.a)) {
        return 0;
      } else {
        return 1;
      }
    }
  }
}
