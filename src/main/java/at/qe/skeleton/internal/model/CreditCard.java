package at.qe.skeleton.internal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Persistable;

import java.io.Serializable;

@Entity
@Table(name = "CREDIT_CARD")
public class CreditCard implements Persistable<Long>, Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @OneToOne
    @JoinColumn(name = "userID")
    private Userx userId;

    @Column(name = "number")
    private String number;

    @Column(name = "expiration_date")
    private String expirationDate;

    @Column(name = "cardtype")
    @Enumerated(EnumType.STRING)
    private CardType cardType;

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

    @Override
    public boolean isNew() {
        return id == null;
    }
}
