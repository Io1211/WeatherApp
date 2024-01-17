package at.qe.skeleton.internal.services;

import at.qe.skeleton.internal.model.Userx;
import at.qe.skeleton.internal.repositories.CreditCardRepository;
import at.qe.skeleton.internal.services.exceptions.NoCreditCardFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionService {

  @Autowired private UserxService userxService;

  @Autowired CreditCardRepository creditCardRepository;

  public void activatePremiumSubscription(Userx user) throws NoCreditCardFoundException {
    if (creditCardRepository.findByUserId_Username(user.getUsername()) == null) {
      throw new NoCreditCardFoundException("No credit card found");
    }
    userxService.activatePremium(user);
  }

  public void deactivatePremiumSubscription(Userx user) {
    userxService.deactivatePremium(user);
  }
}
