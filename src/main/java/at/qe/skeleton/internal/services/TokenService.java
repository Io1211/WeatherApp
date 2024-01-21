package at.qe.skeleton.internal.services;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.security.SecureRandom;

/** Service for generating and validating tokens. */
@Component
@Scope("application")
public class TokenService {

  SecureRandom random = new SecureRandom();

  /**
   * Generates a random four digit token.
   *
   * @return the generated token
   */
  public String generateToken() {
    int fourDigit = 1000 + random.nextInt(1000);
    return Integer.toString(fourDigit);
  }

  public boolean validateToken(String token, String insertedToken) {
    return token.equals(insertedToken);
  }
}
