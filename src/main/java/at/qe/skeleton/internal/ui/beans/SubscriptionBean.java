package at.qe.skeleton.internal.ui.beans;

import at.qe.skeleton.internal.model.Userx;
import at.qe.skeleton.internal.services.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("session")
public class SubscriptionBean {

  @Autowired private SubscriptionService subscriptionService;

  @Autowired SessionInfoBean sessionInfoBean;

  public String activatePremiumSubscription() {
    Userx user = sessionInfoBean.getCurrentUser();
    try {
      subscriptionService.activatePremiumSubscription(user);
      System.out.println("Subscription activated");
      return "/success_page";
    } catch (RuntimeException e) {
      return "add_credit_card_sub.xhtml";
    }
  }

  public void deactivatePremiumSubscription() {
    Userx user = sessionInfoBean.getCurrentUser();
    subscriptionService.deactivatePremiumSubscription(user);
  }
}
