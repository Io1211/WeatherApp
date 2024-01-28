package at.qe.skeleton.internal.services;

import at.qe.skeleton.internal.model.Userx;
import at.qe.skeleton.internal.repositories.UserxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PasswordResetServiceTest {

  @Mock EmailService emailService;
  @InjectMocks PasswordResetService passwordResetService;
  @Autowired PasswordEncoder passwordEncoder;
  @Autowired UserxRepository userxRepository;

  @BeforeEach
  public void init() {
    // need to use these because no Constructor Injection
    ReflectionTestUtils.setField(passwordResetService, "userRepository", userxRepository);
    ReflectionTestUtils.setField(passwordResetService, "passwordEncoder", passwordEncoder);
  }

  @Test
  @WithMockUser(username = "user2", authorities = "REGISTERED_USER")
  @DirtiesContext
  public void sendPasswordResetEmail() {
    String email = "testmail";
    String token = "token";
    String subject = "Reset your password";
    String message =
        "\nYour token: %s\n\nIf you did not request a password reset, please ignore this email."
            .formatted(token);

    Userx user = userxRepository.findFirstByUsername("user2");
    user.setEmail(email);
    userxRepository.save(user);

    passwordResetService.sendPasswordResetEmailAndToken(email, token);

    Mockito.verify(emailService, Mockito.times(1)).sendEmail(email, subject, message);
  }

  @Test
  public void sendPasswordResetUserDoesNotExist() {
    String email = "testmail";
    String token = "token";

    assertThrows(
        IllegalArgumentException.class,
        () -> passwordResetService.sendPasswordResetEmailAndToken(email, token),
        "we did not assign that mail to any user so an exception should be thrown.");
  }

  @WithMockUser(username = "user2", authorities = "REGISTERED_USER")
  @DirtiesContext
  @Test
  public void resetPassword() {
    String email = "testmail";
    String newPassword = "completelyNewPassword";

    Userx user = userxRepository.findFirstByUsername("user2");
    user.setEmail(email);
    userxRepository.save(user);
    assertFalse(
        passwordEncoder.matches(newPassword, user.getPassword()),
        "stored password has not been reseted yet, so it should not match the new password");

    passwordResetService.resetPassword(email, newPassword);
    Userx updatedUser = userxRepository.findFirstByUsername("user2");

    assertTrue(
        passwordEncoder.matches(newPassword, updatedUser.getPassword()),
        "stored password has been reseted and should therefor match the new password");
  }

  @Test
  public void resetPasswordEmailNotFound() {
    String email = "testmail";
    String newPassword = "completelyNewPassword";

    assertThrows(
        IllegalArgumentException.class,
        () -> passwordResetService.resetPassword(email, newPassword),
        "the mail has not been assigned to any user yet, so resetPassword should throw an exception");
  }
}
