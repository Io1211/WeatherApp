package at.qe.skeleton.internal.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.domain.Persistable;

@Entity
public class Subscription implements Persistable<Long>, Serializable {

  @Id @GeneratedValue private Long id;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  private List<SubscriptionPeriod> subscriptionPeriods;

  @CreationTimestamp private LocalDate signupDate;

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

  @Override
  public Long getId() {
    return this.id;
  }

  @Override
  public boolean isNew() {
    return (null == signupDate);
  }
}
