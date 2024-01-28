package at.qe.skeleton.internal.services;

import at.qe.skeleton.internal.model.Userx;
import at.qe.skeleton.internal.model.UserxRole;
import at.qe.skeleton.internal.repositories.UserxRepository;
import at.qe.skeleton.internal.services.exceptions.*;
import org.springframework.beans.factory.annotation.Autowired;
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

  @Autowired private UserxRepository userRepository;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private EmailService emailService;
  @Autowired private TokenService tokenService;

  public void sendRegistrationEmail(String email, String token) throws MailException {
    emailService.sendEmail(
        email,
        "Confirm your registration",
        "\nYour token: " + token + "\n\nIf you did not register, please ignore this email.");
  }

  /**
   * Registers a new user. Sets the user's role to REGISTERED_USER and enabled to false.
   *
   * @throws RegistrationUsernameAlreadyExistsException when username already exists
   * @throws RegistrationEmailAlreadyExistsException when email already exists
   */
  public void registerUser(Userx user, String token)
      throws RegistrationUsernameAlreadyExistsException, RegistrationEmailAlreadyExistsException {
    if (userRepository.findFirstByUsername(user.getUsername()) != null) {
      throw new RegistrationUsernameAlreadyExistsException(user);
    }
    if (userRepository.findFirstByEmail(user.getEmail()) != null) {
      throw new RegistrationEmailAlreadyExistsException(user.getEmail());
    }
    user.setRoles(Set.of(UserxRole.REGISTERED_USER));
    user.setEnabled(false);

    sendRegistrationEmail(user.getEmail(), token);

    userRepository.save(user);
  }

  /**
   * Confirms the registration of a user.
   *
   * @throws RegistrationInvalidTokenException when token could not be validated
   */
  public void confirmRegistrationOfUser(String username, String token, String insertedToken)
      throws RegistrationInvalidTokenException {
    Userx user = userRepository.findFirstByUsername(username);
    if (tokenService.validateToken(insertedToken, token)) {
      user.setEnabled(true);
      user.setPassword(passwordEncoder.encode(user.getPassword()));
      userRepository.save(user);
    } else {
      throw new RegistrationInvalidTokenException("Invalid token.");
    }
  }

  /**
   * Resends the registration email to the user.
   *
   * @throws NoUserFoundException when no user with the given email exists
   * @throws RegistrationUserAlreadyEnabledException when user is already enabled
   */
  public void resendRegistrationEmailToUser(String email, String token)
      throws NoUserFoundException, RegistrationUserAlreadyEnabledException, MailException {
    Userx user = userRepository.findFirstByEmail(email);
    if (user == null) {
      throw new NoUserFoundException("Email does not exist.");
    }
    if (user.isEnabled()) {
      throw new RegistrationUserAlreadyEnabledException();
    }
    sendRegistrationEmail(user.getEmail(), token);
  }

  public Userx loadUserByEmail(String email) {
    return userRepository.findFirstByEmail(email);
  }

  public Userx loadUserByUsername(String username) {
    return userRepository.findFirstByUsername(username);
  }
}
