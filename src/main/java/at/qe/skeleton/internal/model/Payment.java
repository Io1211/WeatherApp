package at.qe.skeleton.internal.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.io.Serializable;
import java.time.LocalDate;
import org.springframework.data.domain.Persistable;

@Entity
public class Payment implements Persistable<Long>, Serializable {

  @Id @GeneratedValue private Long id;

  private LocalDate paymentDate;

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

  public LocalDate getPaymentDateTime() {
    return paymentDate;
  }

  public void setPaymentDate(LocalDate localDate) {
    this.paymentDate = localDate;
  }

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public boolean isNew() {
    return paymentDate == null;
  }
}
