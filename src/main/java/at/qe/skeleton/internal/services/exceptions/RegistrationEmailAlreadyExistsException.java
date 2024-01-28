package at.qe.skeleton.internal.services.exceptions;

public class RegistrationEmailAlreadyExistsException extends Exception {
  public RegistrationEmailAlreadyExistsException(String email) {
    super("A user with email " + email + " already exists.");
  }
}
