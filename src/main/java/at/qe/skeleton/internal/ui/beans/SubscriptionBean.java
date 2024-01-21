package at.qe.skeleton.internal.ui.beans;

import at.qe.skeleton.internal.model.Userx;
import at.qe.skeleton.internal.services.SubscriptionService;
import at.qe.skeleton.internal.services.exceptions.NoActivePremiumSubscriptionFoundException;
import at.qe.skeleton.internal.services.exceptions.NoCreditCardFoundException;
import at.qe.skeleton.internal.services.exceptions.NoSubscriptionFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("session")
public class SubscriptionBean {

  @Autowired private SubscriptionService subscriptionService;

  @Autowired SessionInfoBean sessionInfoBean;

  private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionBean.class);

  public String activatePremiumSubscription() {
    Userx user = sessionInfoBean.getCurrentUser();
    try {
      subscriptionService.activatePremiumSubscription(user);
      LOGGER.info("Subscription activated");
      return "/success_page";
    } catch (NoCreditCardFoundException e) {
      return "add_credit_card_sub.xhtml";
    }
  }

  public void deactivatePremiumSubscription() {
    Userx user = sessionInfoBean.getCurrentUser();
    try {
      subscriptionService.deactivatePremiumSubscription(user);
    }
    // TODO: handle these exceptions appropriately (e.g., via primefacce message)
    catch (NoSubscriptionFoundException e) {
      throw new RuntimeException(e);
    } catch (NoActivePremiumSubscriptionFoundException e) {
      throw new RuntimeException(e);
    }
  }
}
