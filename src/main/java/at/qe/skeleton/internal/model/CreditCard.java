package at.qe.skeleton.internal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Persistable;

import java.io.Serializable;

/**
 * Represents a credit card in the system.
 *
 * <p>This entity is used to store information about a user's credit card, including its number,
 * expiration date, and the type of card and is linked to a specific user.
 */
@Entity
@Table(name = "CREDIT_CARD")
public class CreditCard implements Persistable<Long>, Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // Long is used for the ID because, because we need a wide range of values.

  /** The {@link Userx} entity associated with this credit card. */
  @NotNull
  @OneToOne
  @JoinColumn(name = "userID")
  private Userx userId;

  @Column(name = "number")
  private String number; // String is used because we don't need to do any calculations with it.

  @Column(name = "expiration_date")
  private String
      expirationDate; // String is used for the expiration date because afterward we don't need to

  // do any calculations with it.

  @Column(name = "cardtype")
  @Enumerated(EnumType.STRING)
  private CardType cardType; // CardType is used to represent a set of possible card types.

  public String getUserId() {
    return userId.getId();
  }

  public Userx getUser() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId.setId(userId);
  }

  public void setUserId(Userx userId) {
    this.userId = userId;
  }

  public String getCardnumber() {
    return number;
  }

  public void setCardnumber(String cardnumber) {
    this.number = cardnumber;
  }

  public String getExpirationDate() {
    return expirationDate;
  }

  public void setExpirationDate(String expirationDate) {
    this.expirationDate = expirationDate;
  }

  public void setCardType(CardType cardType) {
    this.cardType = cardType;
  }

  public CardType getCardType() {
    return cardType;
  }

  @Override
  public Long getId() {
    return id;
  }

  /**
   * A credit card is considered new if it has not been saved in the database, therefor the id is
   * null.
   */
  @Override
  public boolean isNew() {
    return id == null;
  }
}
