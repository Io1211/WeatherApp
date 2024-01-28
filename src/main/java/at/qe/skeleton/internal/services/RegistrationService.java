package at.qe.skeleton.internal.services;

import at.qe.skeleton.internal.model.Userx;
import at.qe.skeleton.internal.model.UserxRole;
import at.qe.skeleton.internal.repositories.UserxRepository;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Service for managing the registration of new users.
 *
 * <p>This service is used to register a new user and to confirm the registration.
 */
@Component
@Scope("application")
public class RegistrationService {

  private final UserxRepository userRepository;

  private final PasswordEncoder passwordEncoder;
  private final EmailService emailService;

  private final TokenService tokenService;

  public RegistrationService(
      PasswordEncoder passwordEncoder,
      EmailService emailService,
      TokenService tokenService,
      UserxRepository userxRepository) {

    this.passwordEncoder = passwordEncoder;
    this.emailService = emailService;
    this.tokenService = tokenService;
    this.userRepository = userxRepository;
  }

  public void sendRegistrationEmail(String email, String token) {
    emailService.sendEmail(
        email,
        "Confirm your registration",
        "\nYour token: " + token + "\n\nIf you did not register, please ignore this email.");
  }

  /**
   * Registers a new user. Throws a RuntimeException if the username or email already exists or
   * email is invalid. Sets the user's role to REGISTERED_USER and enabled to false.
   */
  public void registerUser(Userx user, String token) {
    if (userRepository.findFirstByUsername(user.getUsername()) != null) {
      throw new RuntimeException("Username already exists.");
    }
    if (userRepository.findFirstByEmail(user.getEmail()) != null) {
      throw new RuntimeException("Email already exists.");
    }
    user.setRoles(Set.of(UserxRole.REGISTERED_USER));
    user.setEnabled(false);
    try {
      sendRegistrationEmail(user.getEmail(), token);
    } catch (MailException e) {
      throw new RuntimeException("Invalid Email.");
    }
    userRepository.save(user);
  }

  /** Confirms the registration of a user. Throws a RuntimeException if the token is invalid. */
  public void confirmRegistrationOfUser(String email, String token, String insertedToken) {
    Userx user = userRepository.findFirstByEmail(email);
    if (tokenService.validateToken(insertedToken, token)) {
      user.setEnabled(true);
      user.setPassword(passwordEncoder.encode(user.getPassword()));
      userRepository.save(user);
    } else {
      throw new RuntimeException("Invalid token.");
    }
  }

  public void resendRegistrationEmailToUser(String email, String token) {
    Userx user = userRepository.findFirstByEmail(email);
    if (user == null) {
      throw new RuntimeException("Email does not exist.");
    }
    if (user.isEnabled()) {
      throw new RuntimeException("User is already enabled.");
    }
    try {
      sendRegistrationEmail(user.getEmail(), token);
    } catch (MailException e) {
      throw new RuntimeException("Invalid Email.");
    }
  }
}
