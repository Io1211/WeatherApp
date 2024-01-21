package at.qe.skeleton.internal.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import org.antlr.v4.runtime.misc.Pair;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.data.domain.Persistable;

@Entity
public class Subscription implements Persistable<Long>, Serializable {

  @Id @GeneratedValue private Long id;

  private LocalDate signupDate;

  // Premium period is a list of tuples, so that every time the service is activated/canceled start
  // and stop of that period is stored in the same tuple. For every tuple (a, b), a is always the
  // start date and b the cancellation date.
  @ElementCollection
  @Fetch(FetchMode.JOIN)
  private List<Pair<LocalDate, LocalDate>> premiumPeriod;

  @Override
  public Long getId() {
    return this.id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public LocalDate getSignupDate() {
    return signupDate;
  }

  @PrePersist
  public void setStartDate() {
    this.signupDate = LocalDate.now();
  }

  public List<Pair<LocalDate, LocalDate>> getPremiumPeriod() {
    return premiumPeriod;
  }

  public void setPremiumPeriod(List<Pair<LocalDate, LocalDate>> premiumPeriod) {
    this.premiumPeriod = premiumPeriod;
  }

  @Override
  public boolean isNew() {
    return (null == signupDate);
  }
}
