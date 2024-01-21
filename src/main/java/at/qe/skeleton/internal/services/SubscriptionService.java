package at.qe.skeleton.internal.services;

import at.qe.skeleton.internal.model.Subscription;
import at.qe.skeleton.internal.model.Userx;
import at.qe.skeleton.internal.repositories.CreditCardRepository;
import at.qe.skeleton.internal.services.exceptions.NoActivePremiumSubscriptionFoundException;
import at.qe.skeleton.internal.services.exceptions.NoCreditCardFoundException;
import at.qe.skeleton.internal.services.exceptions.NoSubscriptionFoundException;
import at.qe.skeleton.internal.services.exceptions.NotYetAvailableException;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionService {

  @Autowired private UserxService userxService;

  @Autowired private CreditCardRepository creditCardRepository;

  /**
   * This method activates a premium subscription and sets the according user role. If the user
   * wasn't premium before, it creates a new subscription for them and appends the list keeping
   * track of the premium periods with the current date for the start and null for the end date
   * (i.e., end not set). If they were premium before, the same operation is performed minus the
   * creation of a new subscription entity.
   *
   * @param user The user who premium shall be activated for
   * @throws NoCreditCardFoundException when the user in question doesn't have credit card info
   *     associated to them
   */
  public void activatePremiumSubscription(Userx user) throws NoCreditCardFoundException {
    if (user.getCreditCard() == null) {
      throw new NoCreditCardFoundException("No credit card found");
    }
    if (user.getSubscription() == null) {
      user.setSubscription(new Subscription());
    }
    Subscription subscription = user.getSubscription();
    if (subscription.getPremiumPeriod() == null) {
      user.getSubscription().setPremiumPeriod(new ArrayList<>());
    }

    List<Pair<LocalDate, LocalDate>> premiumPeriods = user.getSubscription().getPremiumPeriod();
    Pair<LocalDate, LocalDate> newPremiumPeriod = new Pair<>(LocalDate.now(), null);
    premiumPeriods.add(newPremiumPeriod);
    userxService.activatePremium(user);
  }

  /**
   * This method deactivates/terminates a premium subscription. It removes the premium status from a
   * user and sets the last active (i.e., current) subscription to be ended at the date when this
   * method was called. In absence of an active subscription or a subscription at all the according
   * exception is thrown.
   *
   * @param user The user to whom prime status should be revoked
   * @throws NoSubscriptionFoundException when the user in question doesn't have a subscription tied
   *     to their account
   * @throws NoActivePremiumSubscriptionFoundException when the user in question doesn't have any
   *     active premium membership to cancel tied to their account
   */
  // TODO: add tests
  public void deactivatePremiumSubscription(Userx user)
      throws NoSubscriptionFoundException, NoActivePremiumSubscriptionFoundException {
    Subscription subscription = user.getSubscription();
    if (subscription == null) {
      throw new NoSubscriptionFoundException(user);
    }

    List<Pair<LocalDate, LocalDate>> premiumPeriods = subscription.getPremiumPeriod();
    if (premiumPeriods.isEmpty()) {
      throw new NoActivePremiumSubscriptionFoundException(user);
    }

    Pair<LocalDate, LocalDate> lastSubscription = premiumPeriods.get(premiumPeriods.size() - 1);
    if (lastSubscription.b != null) {
      throw new NoActivePremiumSubscriptionFoundException(user);
    }

    Pair<LocalDate, LocalDate> canceledSubscription =
        new Pair<>(lastSubscription.a, LocalDate.now());
    premiumPeriods.remove(lastSubscription);
    premiumPeriods.add(canceledSubscription);
    user.getSubscription().setPremiumPeriod(premiumPeriods);

    userxService.deactivatePremium(user);
  }

  /**
   * This method is the core of the billing calculation. It fetches the active subscription periods
   * of a given user and calculates the count of days the membership was active in the query month
   * of the query year. It assumes that the start of a subscription period A in a tuple (A, B) of
   * dates can't be null because it doesn't make sense to have a subscription end that didn't even
   * start.<br>
   * Some of the calculation is outsourced to {@link #calculatePremiumFromStartAndStop(LocalDate,
   * LocalDate, Month, int) calculatePremiumFromStartAndStop} and this method completes the
   * calculation by covering additional scenarios that are documented in the code.
   *
   * @param user The user to calculate the billing for
   * @param month The month to calculate the billing for
   * @param year The year the of the month to calculate the billing for
   * @return the number of days the membership was active in the query month for the selected user
   * @throws NotYetAvailableException when the query i.e., month and year, is the current month or a
   *     month further in the future. Billing is supported only for passed months.
   */
  public int premiumDaysInMonth(Userx user, Month month, int year) throws NotYetAvailableException {
    if ((year > ZonedDateTime.now().getYear())
        || ((year == ZonedDateTime.now().getYear())
            && month.getValue() >= ZonedDateTime.now().getMonthValue())) {
      throw new NotYetAvailableException(
          "Billing info is only available for past months. You are trying to access it for this or future months.");
    }

    if (user.getSubscription().getPremiumPeriod().isEmpty()) {
      return 0;
    }

    // This collects all the subscription tuples that start or stop in the queried month.
    // Makes it easy to calculate the premium period if such tuples are found.
    // Otherwise, the empty list is handled below.
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

      // only one subscription tuple for the user and the start is after the query month
      if (lastSubscriptionBeforeMonth == null) {
        return 0;
      }
      // Subscription doesn't have an end set, ergo is still active.
      if (lastSubscriptionBeforeMonth.b == null) {
        return month.length(Year.isLeap(year));
      }
      // This means that the end date of the tuple is after the query month and year, ergo the
      // subscription was still active in the query month and year.
      else if (lastSubscriptionBeforeMonth.b.isAfter(LocalDate.of(year, month, 1))) {
        return month.length(Year.isLeap(year));
      }
      // This means that the subscription was terminated before the query month and year.
      else if (lastSubscriptionBeforeMonth.b.isBefore(LocalDate.of(year, month, 1))) {
        return 0;
      }
    }

    // Calculate the days of active premium during the query month. Sum if multiple start and stops
    // were found.
    return premiumPeriodInMonth.stream()
        .map(pair -> calculatePremiumFromStartAndStop(pair.a, pair.b, month, year))
        .reduce(0, Integer::sum);
  }

  /**
   * Given a start date and a stop date as may be taken from a Pair for instance, this method
   * calculates the days of premium subscription during the time period. This method does not cover
   * the case where both start and stop are not in the query month. That case is handled in {@link
   * #premiumDaysInMonth(Userx, Month, int) premiumDaysInMonth}.
   *
   * @param start Date of membership start
   * @param stop Date of membership stop
   * @param month Month to get the billing information for
   * @param year Year of the month to get the billing information for
   * @return the days of the selected month during which the membership was active
   */
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
    // SubscriptionPeriods is never empty. This is checked in the premiumDaysInMonth method
    if (subscriptionPeriods.isEmpty()) {
      throw new IllegalArgumentException(
          "This method doesn't work for empty lists. That case should be handled in premiumDaysInMonth");
    }

    // if there is only one subscription period on record, return the period if the start is before
    // the query month, else return null.
    if (subscriptionPeriods.size() == 1) {
      Pair<LocalDate, LocalDate> subscription = subscriptionPeriods.get(0);
      return subscription.a.isBefore(LocalDate.of(year, month, 1)) ? subscription : null;
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

  /**
   * Shorthand for a commonly used expression that checks if the date passed is in the month and
   * year passed as parameters.
   *
   * @param date Date to check if it is in the given month and year
   * @param month Month to check for
   * @param year Year to check for
   * @return Whether the date is in the month and year passed
   */
  public boolean isInMonth(LocalDate date, Month month, int year) {
    return date != null && date.getMonth() == month && date.getYear() == year;
  }

  /**
   * Comparator used to sort the tuples in the list that keeps track of a user's active premium
   * periods. The tuples are sorted using the date of the membership start i.e., A in a tuple (A,
   * B), in ascending order.
   */
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
