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

  @Autowired CreditCardRepository creditCardRepository;

  public void activatePremiumSubscription(Userx user) throws NoCreditCardFoundException {
    if (creditCardRepository.findByUserId_Username(user.getUsername()) == null) {
      throw new NoCreditCardFoundException("No credit card found");
    }
    userxService.activatePremium(user);
  }

  public void deactivatePremiumSubscription(Userx user) {
    userxService.deactivatePremium(user);
  }

  public int premiumDaysInMonth(Userx user, Month month, int year) {
    // assumes the start of a subscription period (pair<start, stop> cannot be null cuz it doesn't
    // make sense)
    // enforce this in the setting of the date
    if ((year >= ZonedDateTime.now().getYear())
        || ((year == ZonedDateTime.now().getYear())
            && month.getValue() >= ZonedDateTime.now().getMonthValue())) {
      throw new RuntimeException("Billing info is only available for the past months");
    }

    List<Pair<LocalDate, LocalDate>> premiumPeriodInMonth =
        user.getSubscription().getPremiumPeriod().stream()
            .filter(pair -> isInMonth(pair.a, month, year) || isInMonth(pair.b, month, year))
            .toList();

    // TODO: handle case start & stop not in month. Figure out if premium is active over that period
    // or not to
    //  calculate the days to bill
    return premiumPeriodInMonth.stream()
        .map(pair -> calculatePremiumFromStartAndStop(pair.a, pair.b, month, year))
        .reduce(0, Integer::sum);
  }

  public int calculatePremiumFromStartAndStop(
      LocalDate start, LocalDate stop, Month month, int year) {
    // assumes the start of a subscription period (pair<start, stop> cannot be null cuz it doesn't
    // make sense)
    // enforce this in the setting of the date

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

  public Pair<LocalDate, LocalDate> getLastSubscriptionBeforeMonth(
      List<Pair<LocalDate, LocalDate>> subscriptionPeriods, Month month, int year) {
    subscriptionPeriods.sort(new DatePairComparator());
    Pair<LocalDate, LocalDate> lastSubscriptionBeforeMonth = null;
    for (Pair<LocalDate, LocalDate> subscriptionPeriod : subscriptionPeriods) {
      if (subscriptionPeriod.a.isBefore(LocalDate.parse("%s-%s-01".formatted(year, month)))) {
        lastSubscriptionBeforeMonth = subscriptionPeriod;
      } else {
        break;
      }
    }
    return lastSubscriptionBeforeMonth;
  }

  public boolean isInMonth(LocalDate date, Month month, int year) {
    return date.getMonth() == month && date.getYear() == year;
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
