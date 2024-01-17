package at.qe.skeleton.internal.ui.beans;

import at.qe.skeleton.internal.model.Userx;
import at.qe.skeleton.internal.services.SubscriptionService;
import at.qe.skeleton.internal.services.exceptions.NoCreditCardFoundException;
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
      sessionInfoBean.reloadCurrentUser();
      return "/success_page";
    } catch (NoCreditCardFoundException e) {
      return "add_credit_card_sub.xhtml";
    }
  }

  public void deactivatePremiumSubscription() {
    Userx user = sessionInfoBean.getCurrentUser();
    subscriptionService.deactivatePremiumSubscription(user);
    sessionInfoBean.reloadCurrentUser();
  }
}
