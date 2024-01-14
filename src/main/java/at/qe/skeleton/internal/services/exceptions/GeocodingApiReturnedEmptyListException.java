package at.qe.skeleton.internal.services.exceptions;

public class GeocodingApiReturnedEmptyListException extends Exception {
  public GeocodingApiReturnedEmptyListException(String message) {
    super(message);
  }
}
