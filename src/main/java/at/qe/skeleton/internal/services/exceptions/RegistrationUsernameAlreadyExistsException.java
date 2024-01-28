package at.qe.skeleton.internal.services.exceptions;

import at.qe.skeleton.internal.model.Userx;

public class RegistrationUsernameAlreadyExistsException extends Exception {
  public RegistrationUsernameAlreadyExistsException(Userx user) {
    super("A user with username " + user.getUsername() + " already exists.");
  }
}
