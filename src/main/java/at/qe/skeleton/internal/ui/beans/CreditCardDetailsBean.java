package at.qe.skeleton.internal.ui.beans;

import at.qe.skeleton.internal.helper.WarningHelper;
import at.qe.skeleton.internal.model.CreditCard;
import at.qe.skeleton.internal.services.CreditCardService;
import jakarta.faces.application.FacesMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Controller for the credit card view. The class is used to manage the credit card of the current
 * user.
 */
@Component
@Scope("view")
public class CreditCardDetailsBean {

  @Autowired private CreditCardService creditCardService;

  @Autowired private SessionInfoBean sessionInfoBean;

  @Autowired private WarningHelper warningHelper;

  private CreditCard creditCard;

  public CreditCard getCreditcard() {
    if (this.creditCard == null) {
      this.creditCard =
          creditCardService.loadCreditCardByUsername(
              sessionInfoBean.getCurrentUser().getUsername());
    }
    return this.creditCard;
  }

  public void deleteCreditCard() {
    try {
      creditCardService.deleteCreditCard(getCreditcard());
      warningHelper.addMessage("Credit Card deleted", FacesMessage.SEVERITY_INFO);
    } catch (IllegalArgumentException e) {
      warningHelper.addMessage(e.getMessage(), FacesMessage.SEVERITY_ERROR);
    }
  }
}
