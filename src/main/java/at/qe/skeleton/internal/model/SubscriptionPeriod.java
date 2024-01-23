package at.qe.skeleton.internal.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import org.springframework.data.domain.Persistable;

@Entity
public class SubscriptionPeriod implements Persistable<Long>, Serializable {

  @Id @GeneratedValue private Long id;

  private boolean active;

  private LocalDate start;

  private LocalDate stop;

  @ManyToOne(optional = false)
  @JoinColumn(name = "subscription_id", nullable = false)
  private Subscription subscription;

  public LocalDate getStart() {
    return start;
  }

  public void setStart(LocalDate start) {
    this.start = start;
  }

  public LocalDate getStop() {
    return stop;
  }

  public void setStop(LocalDate stop) {
    this.stop = stop;
  }

  public Subscription getSubscription() {
    return subscription;
  }

  public void setSubscription(Subscription subscription) {
    this.subscription = subscription;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean status) {
    this.active = status;
  }

  public void setId(Long id) {
    this.id = id;
  }

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public boolean isNew() {
    return start == null;
  }
}
