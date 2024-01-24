package at.qe.skeleton.internal.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.io.Serializable;
import java.time.ZonedDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.domain.Persistable;

@Entity
public class Payment implements Persistable<Long>, Serializable {

  @Id @GeneratedValue private Long id;

  @CreationTimestamp private ZonedDateTime paymentDateTime;

  private boolean paid;

  public boolean isPaid() {
    return paid;
  }

  public void setPaid(boolean paid) {
    this.paid = paid;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public ZonedDateTime getPaymentDateTime() {
    return paymentDateTime;
  }

  public void setPaymentDateTime(ZonedDateTime zonedDateTime) {
    this.paymentDateTime = zonedDateTime;
  }

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public boolean isNew() {
    return paymentDateTime == null;
  }
}
