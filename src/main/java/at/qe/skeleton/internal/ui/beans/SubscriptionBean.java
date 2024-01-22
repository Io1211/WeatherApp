package at.qe.skeleton.internal.ui.beans;

import at.qe.skeleton.internal.model.Userx;
import at.qe.skeleton.internal.services.SubscriptionService;
import at.qe.skeleton.internal.services.exceptions.MoneyGlitchAvoidanceException;
import at.qe.skeleton.internal.services.exceptions.NoActivePremiumSubscriptionFoundException;
import at.qe.skeleton.internal.services.exceptions.NoCreditCardFoundException;
import at.qe.skeleton.internal.services.exceptions.NoSubscriptionFoundException;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Bean for subscription management.
 *
 * <p>This bean is used to activate and deactivate the premium subscription for the current user.
 */
@Component
@Scope("session")
public class SubscriptionBean {

  @Autowired private SubscriptionService subscriptionService;

  @Autowired private SessionInfoBean sessionInfoBean;

  private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionBean.class);

  /**
   * Activates the premium subscription for the current user. If no credit card is found, the user
   * will be redirected to the add credit card page.
   *
   * @return the page to redirect to
   */
  public String activatePremiumSubscription() {
    Userx user = sessionInfoBean.getCurrentUser();
    try {
      subscriptionService.activatePremiumSubscription(user);
      FacesContext.getCurrentInstance()
          .addMessage(
              null,
              new FacesMessage(
                  FacesMessage.SEVERITY_INFO,
                  "Success",
                  "Premium activated successfully. Please login again to get access to premium features."));
      LOGGER.info("Subscription activated for user {}", user.getId());
      sessionInfoBean.reloadCurrentUser();
      return "/success_page";
    } catch (NoCreditCardFoundException e) {
      return "/add_credit_card_sub.xhtml";
    }
  }

  /** Deactivates the premium subscription for the current user. */
  public String deactivatePremiumSubscription() {
    Userx user = sessionInfoBean.getCurrentUser();
    try {
      subscriptionService.deactivatePremiumSubscription(user);
    } catch (NoSubscriptionFoundException
        | NoActivePremiumSubscriptionFoundException
        | MoneyGlitchAvoidanceException e) {
      showMessageInPrimefaces(FacesMessage.SEVERITY_WARN, e.getMessage());
      return "/home";
    }
    showMessageInPrimefaces(FacesMessage.SEVERITY_INFO, "Premium deactivated successfully.");
    sessionInfoBean.reloadCurrentUser();
    return "/success_page";
  }

  private void showMessageInPrimefaces(FacesMessage.Severity severity, String summary) {
    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, null));
  }
}
