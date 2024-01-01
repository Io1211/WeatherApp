package at.qe.skeleton.internal.ui.beans;

import at.qe.skeleton.internal.model.CardType;
import at.qe.skeleton.internal.model.CreditCard;
import at.qe.skeleton.internal.services.CreditCardService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Scope("session")
public class CreditCardBean {

  private CreditCard creditCard = new CreditCard();

  @Autowired SessionInfoBean sessionInfoBean;

  @Autowired private CreditCardService creditCardService;

  private List<CardType> cardTypes;

  @PostConstruct
  public void init() {
    cardTypes = Arrays.asList(CardType.values());
  }

  public List<CardType> getCardTypes() {
    return cardTypes;
  }

  public CreditCard getCreditCard() {
    return creditCard;
  }

  private void addMessage(String detail) {
    FacesContext.getCurrentInstance()
        .addMessage(
            null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "CreditCard validation Error", detail));
  }

  public boolean validate() {
    return creditCardService.validate(creditCard.getCardnumber());
  }

  public String saveCreditCard() {
    if (validate()) {
      creditCard.setOwner(sessionInfoBean.getCurrentUser());
      System.out.println(
          "Credit card with number "
              + creditCard.getCardnumber()
              + " from user "
              + getCreditCard().getOwner()
              + " saved.");
      // save credit card in database
    } else {
        addMessage("Credit card number is not valid.");
        return null;

    }
    return "/successPage";
  }
}
