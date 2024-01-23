package at.qe.skeleton.internal.ui.beans;

import at.qe.skeleton.internal.model.Userx;
import at.qe.skeleton.internal.services.EmailService;
import at.qe.skeleton.internal.services.SubscriptionService;
import at.qe.skeleton.internal.services.UserxService;
import at.qe.skeleton.internal.services.exceptions.NotYetAvailableException;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.time.LocalDate;
import java.time.Month;
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

  @Autowired private EmailService emailService;

  private Month month;

  private List<Month> months;

  private int year;

  private List<Integer> years;

  @PostConstruct
  public void initMonthAndYearList() {
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
    FacesContext.getCurrentInstance()
        .addMessage(
            null,
            new FacesMessage(
                FacesMessage.SEVERITY_INFO,
                "Showing billing for %s of %s".formatted(year, month),
                null));
  }

  public Collection<Userx> getUsers() {
    return userxService.getAllUsers();
  }

  public String isActive(Userx user) {
    if (user.getSubscription() == null) {
      return "INACTIVE";
    }
    try {
      return subscriptionService.premiumDaysInMonth(user, month, year) != 0 ? "ACTIVE" : "INACTIVE";
    } catch (NotYetAvailableException e) {
      throw new RuntimeException(e);
    }
  }

  public void onMonthChange() {}

  public void onYearChange() {}

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
}
