package at.qe.skeleton.internal.services;

import at.qe.skeleton.internal.model.Userx;
import at.qe.skeleton.internal.repositories.UserxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Service for managing the password reset.
 *
 * <p>This service is used to send a password reset email to the user and to reset the password.
 */
@Component
@Scope("application")
public class PasswordResetService {

  @Autowired private UserxRepository userRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private EmailService emailService;

  /**
   * Sends a password reset email to the user.
   *
   * @param email the email address of the user
   * @param token the token to reset the password
   */
  public void sendPasswordResetEmailAndToken(String email, String token) {
    if (userRepository.findFirstByEmail(email) == null) {
      throw new IllegalArgumentException("User not found for email " + email);
    } else {
      emailService.sendEmail(
          email,
          "Reset your password",
          "\nYour token: "
              + token
              + "\n\nIf you did not request a password reset, please ignore this email.");
    }
  }

  /**
   * Resets the password of the user.
   *
   * @param email the email address of the user
   * @param newPassword the new password
   */
  public void resetPassword(String email, String newPassword) {
    Userx user = userRepository.findFirstByEmail(email);
    if (user == null) {
      throw new IllegalArgumentException("User not found for email " + email);
    } else {
      user.setPassword(passwordEncoder.encode(newPassword));
      userRepository.save(user);
    }
  }

  /** Sends a password reset email to the user. */
  public void sendForgetPasswordEmail(Userx user) {
    emailService.sendEmail(
        user.getEmail(),
        "Reset your password",
        """
To reset your password follow this link:
http://localhost:8080/request_new_password.xhtml

If you did not request a password reset, please ignore this email.
""");
  }
}
