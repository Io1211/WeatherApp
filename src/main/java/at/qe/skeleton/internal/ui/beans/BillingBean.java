package at.qe.skeleton.internal.ui.beans;

import at.qe.skeleton.internal.helper.WarningHelper;
import at.qe.skeleton.internal.model.SubscriptionPeriod;
import at.qe.skeleton.internal.model.Userx;
import at.qe.skeleton.internal.services.SubscriptionService;
import at.qe.skeleton.internal.services.UserxService;
import at.qe.skeleton.internal.services.exceptions.*;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import java.time.*;
import java.util.*;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("view")
public class BillingBean {

  @Autowired private UserxService userxService;

  @Autowired private SubscriptionService subscriptionService;

  @Autowired private WarningHelper warningHelper;

  private Userx user; // holds the user edited in the detailed view

  private boolean paid;

  private boolean revokeSubscription;

  private Month month;

  private List<Month> months;

  private int year;

  private List<Integer> years;

  @PostConstruct
  public void init() {
    // month and year lists for dropdown menus
    months = List.of(Month.values());
    years =
        IntStream.rangeClosed(2000, LocalDate.now().getYear())
            .boxed()
            .sorted((a, b) -> b.compareTo(a))
            .toList();

    // initialize month and year to previous month so the search works even if the user hasn't set a
    // specific month and year yet
    month = LocalDate.now().getMonth().minus(1);
    year =
        LocalDate.now().getMonth() == Month.JANUARY
            ? LocalDate.now().getYear() - 1
            : LocalDate.now()
                .getYear(); // if this month is january, the previous month is in last year
  }

  public Collection<Userx> getUsers() {
    return userxService.getAllUsers();
  }

  public void onYearOrMonthChange() {
    if ((year > ZonedDateTime.now().getYear())
        || ((year == ZonedDateTime.now().getYear())
            && month.getValue() >= ZonedDateTime.now().getMonthValue())) {
      warningHelper.addMessage(
          "Billing info is only available for past months. You are trying to access it for this or future months.",
          FacesMessage.SEVERITY_ERROR);
    }
  }

  public String isActive(Userx user) {
    if (user.getSubscription() == null) {
      return "-";
    }
    try {
      return subscriptionService.premiumDaysInMonth(user, month, year) != 0 ? "ACTIVE" : "INACTIVE";
    } catch (NotYetAvailableException e) {
      return "ERROR";
    }
  }

  public String getBilledDays(Userx user) {
    if (user.getSubscription() == null) {
      return "-";
    }
    try {
      return String.valueOf(subscriptionService.premiumDaysInMonth(user, month, year));
    } catch (NotYetAvailableException e) {
      return "ERROR";
    }
  }

  public String getPaymentStatus(Userx user) {
    if (user.getSubscription() == null) {
      return "-";
    }
    LocalDate queryDate = LocalDate.of(year, month, 1);
    try {
      if (subscriptionService.premiumDaysInMonth(user, month, year) == 0) {
        return "-";
      }
    } catch (NotYetAvailableException e) {
      warningHelper.addMessage(e.getMessage(), FacesMessage.SEVERITY_WARN);
      return "-";
    }
    return subscriptionService.isMonthPaid(user, queryDate) ? "PAID" : "PENDING";
  }

  public String getTotalPremiumDays(Userx user) {
    if (user.getSubscription() == null) {
      return "-";
    }
    return String.valueOf(subscriptionService.calculateTotalPremiumDays(user));
  }

  public void handlePaymentStatus(Userx user) {
    if (user.getSubscription() == null) {
      return;
    }
    if (paid) {
      try {
        subscriptionService.addPayment(user, LocalDate.of(year, month, 1));
      } catch (ArrayIndexOutOfBoundsException | NotYetAvailableException e) {
        warningHelper.addMessage(e.getMessage(), FacesMessage.SEVERITY_WARN);
        return;
      }
      warningHelper.addMessage(
          "The payment status for %s has been set to paid for %s of %s"
              .formatted(user.getId(), month, year),
          FacesMessage.SEVERITY_INFO);
    } else {
      LocalDate queryDate = LocalDate.of(year, month, 1);
      if (subscriptionService.isMonthPaid(user, queryDate)) {
        try {
          subscriptionService.removePayment(user, queryDate);
        } catch (NoSubscriptionFoundException e) {
          warningHelper.addMessage(e.getMessage(), FacesMessage.SEVERITY_ERROR);
        }
      }
    }
  }

  public void handleRevokeSubscription(Userx user) {
    if (!revokeSubscription) {
      return;
    }
    try {
      subscriptionService.revokeSubscription(user);
      warningHelper.addMessage(
          "Payment status set to failed for user %s. Their subscription has been terminated."
              .formatted(user.getId()),
          FacesMessage.SEVERITY_INFO);
    } catch (NoEmailProvidedException e) {
      warningHelper.addMessage(
          "The subscription was cancelled, however there was a problem when contacting the user. "
              + e.getMessage(),
          FacesMessage.SEVERITY_WARN);
    }
  }

  public boolean userHasSubscription(Userx user) {
    return user.getSubscription() != null;
  }

  public List<SubscriptionPeriod> getSubscriptionPeriods(Userx user) {
    return user.getSubscription().getSubscriptionPeriods();
  }

  public Month getMonth() {
    return month;
  }

  public void setMonth(Month month) {
    this.month = month;
  }

  public int getYear() {
    return year;
  }

  public void setYear(int year) {
    this.year = year;
  }

  public List<Month> getMonths() {
    return months;
  }

  public void setMonths(List<Month> months) {
    this.months = months;
  }

  public List<Integer> getYears() {
    return years;
  }

  public void setYears(List<Integer> years) {
    this.years = years;
  }

  public Userx getUser() {
    return user;
  }

  public void setUser(Userx user) {
    this.user = user;
  }

  public boolean isPaid() {
    return paid;
  }

  public void setPaid(boolean paid) {
    this.paid = paid;
  }

  public boolean getRevokeSubscription() {
    return revokeSubscription;
  }

  public void setRevokeSubscription(boolean revokeSubscription) {
    this.revokeSubscription = revokeSubscription;
  }
}
