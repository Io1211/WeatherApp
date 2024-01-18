package at.qe.skeleton.internal.services;

import at.qe.skeleton.internal.model.Userx;
import at.qe.skeleton.internal.repositories.CreditCardRepository;
import at.qe.skeleton.internal.services.exceptions.NoCreditCardFoundException;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
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
    List<Pair<LocalDate, LocalDate>> premiumPeriodInMonth =
        user.getSubscription().getPremiumPeriod().stream()
            .filter(
                pair ->
                    ((pair.a.getMonth() == month && pair.a.getYear() == year)
                        || (pair.b.getMonth() == month && pair.b.getYear() == year)))
            .toList();

    return premiumPeriodInMonth.stream()
        .map(pair -> calculatePremiumFromStartAndStop(pair.a, pair.b, month, year))
        .reduce(0, Integer::sum);
  }

  public int calculatePremiumFromStartAndStop(
      LocalDate start, LocalDate stop, Month month, int year) {
    if (start != null && stop != null) {
      return stop.getDayOfMonth() - start.getDayOfMonth();
    } else if (start == null && stop != null) {
      return stop.getDayOfMonth();
    } else if (start != null) { // && stop == null
      return month.length(Year.isLeap(year)) - start.getDayOfMonth();
    } else {
      return month.length(Year.isLeap(year));
    }
  }
}
