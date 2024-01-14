package at.qe.skeleton.internal.ui.beans;

import at.qe.skeleton.internal.model.CardType;
import at.qe.skeleton.internal.model.CreditCard;
import at.qe.skeleton.internal.repositories.CreditCardRepository;
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

    private CreditCard creditCard;

    @Autowired SessionInfoBean sessionInfoBean;

    @Autowired private CreditCardService creditCardService;

    @Autowired private CreditCardRepository creditCardRepository;

    private List<CardType> cardTypes;

    @PostConstruct
    public void init() {
        cardTypes = Arrays.asList(CardType.values());
        loadCurrentUserCreditCard();
    }

    private void loadCurrentUserCreditCard() {
        String username = sessionInfoBean.getCurrentUser().getUsername();
        CreditCard existingCard = creditCardRepository.findByUserId_Username(username);
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

    private void addMessage(String summary) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, summary, "detail"));
    }

    public String saveCreditCard() {
        try {
            creditCard.setUserId(sessionInfoBean.getCurrentUser());
            creditCardService.saveCreditCard(creditCard);
            sessionInfoBean.getCurrentUser().setCreditCard(creditCard);
            addMessage("Credit card saved.");
        } catch (IllegalArgumentException e) {
            addMessage(e.getMessage());
            return null;
        }
        return "credit_card_details.xhtml";
    }

    public String updateCreditCard() {
        creditCardService.deleteCreditCardFromUser(sessionInfoBean.getCurrentUserName());
        return saveCreditCard();
    }

    public void setCreditCard(CreditCard mockCard) {
        this.creditCard = mockCard;
    }
}
