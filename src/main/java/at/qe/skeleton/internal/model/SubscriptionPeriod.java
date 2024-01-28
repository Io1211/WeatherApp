package at.qe.skeleton.internal.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import org.springframework.data.domain.Persistable;

@Entity
public class SubscriptionPeriod implements Persistable<Long>, Serializable {

  @Id @GeneratedValue private Long id;
  private boolean active; // true if the subscription is active, false if it is inactive

  private LocalDate start; // LocalDate is used, because we only need the date for the start

  private LocalDate stop; // LocalDate is used, because we only need the date for the stop

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
