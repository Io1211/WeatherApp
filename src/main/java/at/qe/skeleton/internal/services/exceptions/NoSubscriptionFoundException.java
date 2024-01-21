package at.qe.skeleton.internal.services.exceptions;

import at.qe.skeleton.internal.model.Userx;

public class NoSubscriptionFoundException extends Exception {
  public NoSubscriptionFoundException(Userx user) {
    super("The user " + user + "doesn't have a subscription associated to their account.");
  }
}
