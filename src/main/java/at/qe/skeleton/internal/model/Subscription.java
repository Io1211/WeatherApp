package at.qe.skeleton.internal.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Persistable;

@Entity
public class Subscription implements Persistable<Long>, Serializable {

  @Id @GeneratedValue private Long id;

  /**
   * The list of {@link SubscriptionPeriod} entities for this subscription. The list is ordered by
   * the start date of the subscription period.
   */
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  private List<SubscriptionPeriod> subscriptionPeriods;

  private LocalDate signupDate; // LocalDate is used, because we only need the date for the signup

  /**
   * The list of {@link Payment} entities for this subscription. The list is ordered by the payment
   * date.
   */
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  private List<Payment> payments;

  public LocalDate getSignupDate() {
    return signupDate;
  }

  public void setSignupDate() {
    this.signupDate = LocalDate.now();
  }

  public List<SubscriptionPeriod> getSubscriptionPeriods() {
    return subscriptionPeriods;
  }

  public void setSubscriptionPeriods(List<SubscriptionPeriod> subscriptionPeriods) {
    this.subscriptionPeriods = subscriptionPeriods;
  }

  public void setSignupDate(LocalDate signupDate) {
    this.signupDate = signupDate;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public List<Payment> getPayments() {
    return payments;
  }

  public void setPayments(List<Payment> paidMonths) {
    this.payments = paidMonths;
  }

  @Override
  public Long getId() {
    return this.id;
  }

  @Override
  public boolean isNew() {
    return (null == signupDate);
  }
}
