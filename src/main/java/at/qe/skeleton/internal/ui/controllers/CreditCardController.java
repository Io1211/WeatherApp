package at.qe.skeleton.internal.ui.controllers;

import at.qe.skeleton.internal.model.CreditCard;
import at.qe.skeleton.internal.repositories.CreditCardRepository;
import at.qe.skeleton.internal.services.CreditCardService;
import at.qe.skeleton.internal.ui.beans.SessionInfoBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("view")
public class CreditCardController {

    @Autowired private CreditCardService creditCardService;

    @Autowired private SessionInfoBean sessionInfoBean;

    @Autowired private CreditCardRepository creditCardRepository;

    private CreditCard creditCard;

    public CreditCard getCreditcard() {
        return creditCardRepository.findByUserId_Username(
                sessionInfoBean.getCurrentUser().getUsername());
    }

    public void setCreditCard(CreditCard creditCard) {
        this.creditCard = creditCard;
    }

    public void reloadCreditCard() {
        creditCard = creditCardService.loadCreditCard(creditCard.getId());
    }

    public void saveCreditCard() {
        creditCardService.saveCreditCard(creditCard);
    }

    public void deleteCreditCard() {
        creditCardService.deleteCreditCard(getCreditcard());
    }
}
