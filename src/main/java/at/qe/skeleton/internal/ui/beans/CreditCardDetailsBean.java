package at.qe.skeleton.internal.ui.beans;

import at.qe.skeleton.internal.model.CreditCard;
import at.qe.skeleton.internal.services.CreditCardService;
import at.qe.skeleton.internal.ui.beans.SessionInfoBean;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
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

  public CreditCard getCreditcard() {
    return creditCardService.loadCreditCardByUsername(
        sessionInfoBean.getCurrentUser().getUsername());
  }

  private void addMessage(String summary, FacesMessage.Severity severity) {
    FacesContext.getCurrentInstance()
        .addMessage(null, new FacesMessage(severity, summary, "detail"));
  }

  public void deleteCreditCard() {
    creditCardService.deleteCreditCard(getCreditcard());
    addMessage("Credit Card deleted", FacesMessage.SEVERITY_INFO);
  }
}
