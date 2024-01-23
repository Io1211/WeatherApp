package at.qe.skeleton.internal.services.exceptions;

public class NoCreditCardFoundException extends Exception {
  public NoCreditCardFoundException(String message) {
    super(message);
  }
}
