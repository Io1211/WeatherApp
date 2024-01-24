package at.qe.skeleton.internal.services;

import static java.time.temporal.ChronoUnit.DAYS;

import at.qe.skeleton.internal.model.Payment;
import at.qe.skeleton.internal.model.Subscription;
import at.qe.skeleton.internal.model.SubscriptionPeriod;
import at.qe.skeleton.internal.model.Userx;
import at.qe.skeleton.internal.repositories.CreditCardRepository;
import at.qe.skeleton.internal.services.exceptions.*;
import java.time.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionService {

  @Autowired private UserxService userxService;

  @Autowired private EmailService emailService;

  @Autowired private CreditCardRepository creditCardRepository;

  public void addPayment(Userx user, LocalDate dateTime) {
    if (user.getSubscription().getPayments() == null) {
      user.getSubscription().setPayments(new ArrayList<>());
    }
    Payment payment = new Payment();
    payment.setPaid(true);
    payment.setPaymentDate(dateTime);
    user.getSubscription().getPayments().add(payment);
    userxService.saveUser(user);
  }

  public Payment findPayment(Userx userx, LocalDate date) {
    return userx.getSubscription().getPayments().stream()
        .filter(
            payment -> {
              LocalDate paymentDate = payment.getPaymentDateTime();
              return paymentDate.getMonth() == date.getMonth()
                  && paymentDate.getYear() == date.getYear();
            })
        .findFirst()
        .orElse(null);
  }

  public void revokeSubscription(Userx user) {
    user.setSubscription(null);
    emailService.sendEmail(
        user.getEmail(),
        "Weather-app subscription termination",
        "Dear customer, we regret to inform you, that your subscription has been terminated by a staff member due to the failure to charge your card in the amount of the monthly fee.");
  }

  public boolean isMonthPaid(Userx user, LocalDate date) {
    return user.getSubscription().getPayments().stream()
        .filter(Payment::isPaid)
        .anyMatch(
            payment -> {
              LocalDate paymentDate = payment.getPaymentDateTime();
              return paymentDate.getMonth() == date.getMonth()
                  && paymentDate.getYear() == date.getYear();
            });
  }

  public long calculateTotalPremiumDays(Userx user) {
    return user.getSubscription().getSubscriptionPeriods().stream()
        .mapToLong(sub -> DAYS.between(sub.getStart(), sub.getStop()))
        .sum();
  }

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
    if (creditCardRepository.findByUserId_Username(user.getUsername()) == null) {
      throw new NoCreditCardFoundException("No credit card found");
    }
    if (user.getSubscription() == null) {
      user.setSubscription(new Subscription());
      user.getSubscription().setSignupDate(LocalDate.now());
    }

    Subscription subscription = user.getSubscription();
    if (subscription.getSubscriptionPeriods() == null) {
      user.getSubscription().setSubscriptionPeriods(new ArrayList<>());
    }

    SubscriptionPeriod newPremiumPeriod = new SubscriptionPeriod();
    newPremiumPeriod.setStart(LocalDate.now());
    newPremiumPeriod.setActive(true);
    user.getSubscription().getSubscriptionPeriods().add(newPremiumPeriod);
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
  public void deactivatePremiumSubscription(Userx user)
      throws NoSubscriptionFoundException,
          NoActivePremiumSubscriptionFoundException,
          MoneyGlitchAvoidanceException {
    // These are just precautionary measures. By activating the membership via
    // activatePremiumSubscription these cases should never happen.
    // ---
    Subscription subscription = user.getSubscription();
    if (subscription == null) {
      throw new NoSubscriptionFoundException(user);
    }

    List<SubscriptionPeriod> premiumPeriods = subscription.getSubscriptionPeriods();
    if (premiumPeriods == null || premiumPeriods.isEmpty()) {
      throw new NoActivePremiumSubscriptionFoundException(user);
    }
    // ---

    // Get the currently active membership period or throw an exception if there is none.
    SubscriptionPeriod activeSubscription =
        user.getSubscription().getSubscriptionPeriods().stream()
            .filter(SubscriptionPeriod::isActive)
            .findFirst()
            .orElseThrow(() -> new NoActivePremiumSubscriptionFoundException(user));

    // If a user can activate and deactivate premium the same day they might get away with not
    // paying for the membership.
    if (activeSubscription.getStart().isEqual(LocalDate.now())) {
      throw new MoneyGlitchAvoidanceException(
          "Premium can't be deactivated the day it was activated");
    }

    activeSubscription.setStop(LocalDate.now());
    activeSubscription.setActive(false);

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

    if (user.getSubscription().getSubscriptionPeriods().isEmpty()) {
      return 0;
    }

    // This collects all the subscription tuples that start or stop in the queried month.
    // Makes it easy to calculate the premium period if such tuples are found.
    // Otherwise, the empty list is handled below.
    List<SubscriptionPeriod> premiumPeriodInMonth =
        user.getSubscription().getSubscriptionPeriods().stream()
            .filter(
                subscriptionPeriod ->
                    isInMonth(subscriptionPeriod.getStart(), month, year)
                        || isInMonth(subscriptionPeriod.getStop(), month, year))
            .toList();

    // This handles the case where neither start nor stop of a subscription is in the queried month.
    // If there is no end set to the subscription (.b == null), return the length of the month
    // because the subscription was still active in that month.
    // If an end is set, it means that the subscription wasn't active during the query month ->
    // return 0.
    if (premiumPeriodInMonth.isEmpty()) {
      SubscriptionPeriod lastSubscriptionBeforeMonth =
          getLastSubscriptionBeforeMonth(
              user.getSubscription().getSubscriptionPeriods(), month, year);

      // only one subscription tuple for the user and the start is after the query month
      if (lastSubscriptionBeforeMonth == null) {
        return 0;
      }
      // Subscription doesn't have an end set, ergo is still active.
      if (lastSubscriptionBeforeMonth.isActive()) {
        return month.length(Year.isLeap(year));
      }
      // This means that the end date of the tuple is after the query month and year, ergo the
      // subscription was still active in the query month and year.
      else if (lastSubscriptionBeforeMonth.getStop().isAfter(LocalDate.of(year, month, 1))) {
        return month.length(Year.isLeap(year));
      }
      // This means that the subscription was terminated before the query month and year.
      else if (lastSubscriptionBeforeMonth.getStop().isBefore(LocalDate.of(year, month, 1))) {
        return 0;
      }
    }

    // Calculate the days of active premium during the query month. Sum if multiple start and stops
    // were found.
    return premiumPeriodInMonth.stream()
        .map(
            subscriptionPeriod ->
                calculatePremiumFromStartAndStop(
                    subscriptionPeriod.getStart(), subscriptionPeriod.getStop(), month, year))
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
  public SubscriptionPeriod getLastSubscriptionBeforeMonth(
      List<SubscriptionPeriod> subscriptionPeriods, Month month, int year) {
    // SubscriptionPeriods is never empty. This is checked in the premiumDaysInMonth method
    if (subscriptionPeriods.isEmpty()) {
      throw new IllegalArgumentException(
          "This method doesn't work for empty lists. That case should be handled in premiumDaysInMonth");
    }

    // if there is only one subscription period on record, return the period if the start is before
    // the query month, else return null.
    if (subscriptionPeriods.size() == 1) {
      SubscriptionPeriod subscriptionPeriod = subscriptionPeriods.get(0);
      return subscriptionPeriod.getStart().isBefore(LocalDate.of(year, month, 1))
          ? subscriptionPeriod
          : null;
    }

    // The list of subscription dates is sorted by the start date.
    // Iterating over the sorted list and setting the extracted tuple every time until the start
    // date is after the query month yields the sought tuple.
    subscriptionPeriods.sort(new SubscriptionPeriodsComparator());
    SubscriptionPeriod lastSubscriptionBeforeMonth = null;
    for (SubscriptionPeriod subscriptionPeriod : subscriptionPeriods) {
      if (subscriptionPeriod.getStart().isBefore(LocalDate.of(year, month.getValue(), 1))) {
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
  static class SubscriptionPeriodsComparator implements Comparator<SubscriptionPeriod> {
    @Override
    public int compare(
        SubscriptionPeriod subscriptionPeriod1, SubscriptionPeriod subscriptionPeriod2) {
      if (subscriptionPeriod1.getStart().isBefore(subscriptionPeriod2.getStart())) {
        return -1;
      } else if (subscriptionPeriod1.getStart().isEqual(subscriptionPeriod2.getStart())) {
        return 0;
      } else {
        return 1;
      }
    }
  }
}
