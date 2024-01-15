package at.qe.skeleton.tests;

import at.qe.skeleton.internal.services.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the TokenService class. {@link TokenService} */
public class TokenServiceTest {

  private TokenService tokenService;

  @BeforeEach
  void setUp() {
    tokenService = new TokenService();
  }

  @Test
  void testGenerateToken() {
    String token = tokenService.generateToken();
    assertNotNull(token);
    assertTrue(token.matches("\\d{4}")); // Check if the token is a 4-digit number
  }

  @Test
  void testValidatePasswordResetToken() {
    String token = "1234";
    assertTrue(tokenService.validateToken(token, token));
    assertFalse(tokenService.validateToken(token, "12345"));
  }
}
