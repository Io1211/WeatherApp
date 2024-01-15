package at.qe.skeleton.internal.services;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.util.Random;

/** Service for generating and validating tokens. */
@Component
@Scope("application")
public class TokenService {

  /**
   * Generates a random four digit token.
   *
   * @return the generated token
   */
  public String generateToken() {
    Random r = new Random();
    int fourDigit = 1000 + r.nextInt(1000);
    return Integer.toString(fourDigit);
  }

  public boolean validateToken(String token, String insertedToken) {
    return token.equals(insertedToken);
  }
}
