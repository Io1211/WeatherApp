package at.qe.skeleton.internal.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.data.domain.Persistable;

@Entity
public class Subscription implements Persistable<Long>, Serializable {

  @Id @GeneratedValue private Long id;

  @OneToOne private Userx userx;

  private Date startDate;

  @ElementCollection private List<Pair<LocalDate, LocalDate>> premiumPeriod;

  @Override
  public Long getId() {
    return this.id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Userx getUserx() {
    return userx;
  }

  public void setUserx(Userx userx) {
    this.userx = userx;
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public List<Pair<LocalDate, LocalDate>> getPremiumPeriod() {
    return premiumPeriod;
  }

  public void setPremiumPeriod(List<Pair<LocalDate, LocalDate>> premiumPeriod) {
    this.premiumPeriod = premiumPeriod;
  }

  @Override
  public boolean isNew() {
    return (null == startDate);
  }
}
