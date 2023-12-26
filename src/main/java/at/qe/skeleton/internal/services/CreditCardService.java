package at.qe.skeleton.internal.services;

import at.qe.skeleton.internal.model.CreditCard;
import at.qe.skeleton.internal.repositories.CreditCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("application")
public class CreditCardService {

    @Autowired
    private CreditCardRepository creditCardRepository;
    public boolean validate(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return false;
        }
        if (Integer.parseInt(cardNumber) % 2 == 0) {
            return true;
        }
        return false;
    }

    public void saveCreditCard(CreditCard creditCard) {
        creditCardRepository.save(creditCard);
    }

    public void deleteCreditCard(CreditCard creditCard) {
        creditCardRepository.delete(creditCard);
    }

    public CreditCard loadCreditCard(String username) {
        return creditCardRepository.findByOwnerUsername(username);
    }



}
