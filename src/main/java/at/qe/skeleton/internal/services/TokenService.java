package at.qe.skeleton.internal.services;

import org.springframework.context.annotation.Scope;
import org.springframework.security.core.token.Token;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@Scope("application")
public class TokenService {

  public String generateToken() {
    Random r = new Random();
    int fourDigit = 1000 + r.nextInt(10000);
    System.out.println(fourDigit);
    return Integer.toString(fourDigit);
  }

  public boolean validateToken(String token, String insertedToken) {
    return token.equals(insertedToken);
  }
}
