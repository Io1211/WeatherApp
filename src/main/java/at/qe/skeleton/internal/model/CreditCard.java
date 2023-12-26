package at.qe.skeleton.internal.model;

import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;

import java.io.Serializable;
import java.util.Date;

@Entity
public class CreditCard implements Persistable<String>, Serializable, Comparable<CreditCard> {

  @Id
  @Column(length = 100)
  private String cardnumber;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "username")
  private Userx owner;

  private Date expirationDate;

  @Enumerated(EnumType.STRING)
  private CardType cardType;

  public String getCardnumber() {
    return cardnumber;
  }

  public void setCardnumber(String cardnumber) {
    this.cardnumber = cardnumber;
  }

  public Date getExpirationDate() {
    return expirationDate;
  }

  public void setExpirationDate(Date expirationDate) {
    this.expirationDate = expirationDate;
  }

  public CardType getCardType() {
    return cardType;
  }

  public void setCardType(CardType cardType) {
    this.cardType = cardType;
  }

  public Userx getOwner() {
    return owner;
  }

  public void setOwner(Userx owner) {
    this.owner = owner;
  }

  @Override
  public int compareTo(CreditCard o) {
    return this.cardnumber.compareTo(o.cardnumber);
  }

  @Override
  public String getId() {
    return cardnumber;
  }

  @Override
  public boolean isNew() {
    return cardnumber == null;
  }
}
