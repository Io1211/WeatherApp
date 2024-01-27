package at.qe.skeleton.internal.ui.controllers;

import at.qe.skeleton.internal.model.CreditCard;
import at.qe.skeleton.internal.repositories.CreditCardRepository;
import at.qe.skeleton.internal.services.CreditCardService;
import at.qe.skeleton.internal.ui.beans.SessionInfoBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Controller for the credit card view. The class is used to manage the credit card of the current
 * user.
 */
@Component
@Scope("view")
public class CreditCardController {

  @Autowired private CreditCardService creditCardService;

  @Autowired private SessionInfoBean sessionInfoBean;

  public CreditCard getCreditcard() {
    return creditCardService.loadCreditCardByUsername(
        sessionInfoBean.getCurrentUser().getUsername());
  }

  public String deleteCreditCard() {
    creditCardService.deleteCreditCard(getCreditcard());

    return "credit_card_details.xhtml?faces-redirect=true";
  }
}

// todo: try to make logical distinction between creditCardController
// and creditCardBean or put both in one class.

// todo: add a message to the delete CreditCard method

// todo: only use the creditCardService not the directory.
