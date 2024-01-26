package at.qe.skeleton.internal.services.exceptions;

import at.qe.skeleton.internal.model.Userx;

public class NoActivePremiumSubscriptionFoundException extends Exception {
  public NoActivePremiumSubscriptionFoundException(Userx user) {
    super("The user " + user.getId() + "doesn't have an active subscription to cancel.");
  }
}
