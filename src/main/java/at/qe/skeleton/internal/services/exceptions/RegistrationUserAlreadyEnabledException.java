package at.qe.skeleton.internal.services.exceptions;

public class RegistrationUserAlreadyEnabledException extends Exception {
  public RegistrationUserAlreadyEnabledException() {
    super("User is already enabled.");
  }
}
