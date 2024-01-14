package at.qe.skeleton.internal.services.exceptions;

public class FailedApiRequest extends Exception {
  public FailedApiRequest(String message) {
    super(message);
  }
}
