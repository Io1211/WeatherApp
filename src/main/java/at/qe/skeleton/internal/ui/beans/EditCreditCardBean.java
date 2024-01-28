package at.qe.skeleton.internal.ui.beans;

import at.qe.skeleton.internal.helper.WarningHelper;
import at.qe.skeleton.internal.model.CardType;
import at.qe.skeleton.internal.model.CreditCard;
import at.qe.skeleton.internal.services.CreditCardService;
import at.qe.skeleton.internal.services.UserxService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;

/**
 * Bean for managing the credit card details of the current user.{@link CreditCardService}
 *
 * <p>This bean is used to manage the credit card of the current user. The credit card details are
 * shown to the user in the input fields if there already exists one. This class is used in the
 * credit_card_details.xhtml file.
 */
@Component
@Scope("session")
public class EditCreditCardBean {

  private CreditCard creditCard;

  @Autowired SessionInfoBean sessionInfoBean;

  @Autowired private CreditCardService creditCardService;

  @Autowired private WarningHelper warningHelper;

  private List<CardType> cardTypes;

  /**
   * Initializes the bean to show the current user's credit card details to the user in the input
   * fields. If the user does not have a credit card yet, a new credit card is created and the
   * fields are empty.
   */
  @PostConstruct
  public void init() {
    cardTypes = Arrays.asList(CardType.values());
    loadCurrentUserCreditCard();
  }

  /**
   * Loads the credit card of the current user if it exists otherwise it returns a new credit card.
   */
  private void loadCurrentUserCreditCard() {
    String username = sessionInfoBean.getCurrentUser().getUsername();
    CreditCard existingCard = creditCardService.loadCreditCardByUsername(username);
    if (existingCard != null) {
      this.creditCard = existingCard;
    } else {
      this.creditCard = new CreditCard();
    }
  }

  public List<CardType> getCardTypes() {
    return cardTypes;
  }

  public CreditCard getCreditCard() {
    return creditCard;
  }

  /**
   * Saves the credit card of the current user. If the credit card already exists, it is updated.
   *
   * @return the page to navigate to after saving the credit card.
   */
  public String saveCreditCard() {
    try {
      creditCard.setUser(sessionInfoBean.getCurrentUser());
      creditCardService.saveCreditCard(creditCard);
      warningHelper.addMessage("Credit card saved.", FacesMessage.SEVERITY_INFO);
      return "credit_card_details.xhtml";
    } catch (IllegalArgumentException e) {
      warningHelper.addMessage(e.getMessage(), FacesMessage.SEVERITY_ERROR);
      return null;
    }
  }

  /**
   * Saves the credit card of the current user in the premium activation context.
   *
   * @return the page to navigate to after saving the credit card.
   */
  public String saveCreditCardPremium() {
    try {
      creditCard.setUser(sessionInfoBean.getCurrentUser());
      creditCardService.saveCreditCard(creditCard);
      warningHelper.addMessage("Credit card saved.", FacesMessage.SEVERITY_INFO);
    } catch (IllegalArgumentException e) {
      warningHelper.addMessage(e.getMessage(), FacesMessage.SEVERITY_ERROR);
      return null;
    }
    return "premium_activation_cc.xhtml";
  }

  /**
   * Update the credit card of the current user by deleting the old one and saving the new one.
   *
   * @return the page to navigate to after the update.
   */
  public String updateCreditCard() {
    creditCardService.deleteCreditCardFromUser(sessionInfoBean.getCurrentUserName());
    return saveCreditCard();
  }

  /**
   * Update the credit card in the premium activation context by deleting the old one and saving the
   * new one.
   *
   * @return the page to navigate to after the update.
   */
  public String updateCreditCardPremium() {
    creditCardService.deleteCreditCardFromUser(sessionInfoBean.getCurrentUserName());
    return saveCreditCardPremium();
  }

  /**
   * This methode is needed to test the class
   *
   * @param mockCard the credit card to be set
   */
  public void setCreditCard(CreditCard mockCard) {
    this.creditCard = mockCard;
  }
}
