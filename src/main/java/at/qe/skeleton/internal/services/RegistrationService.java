package at.qe.skeleton.internal.services;

import at.qe.skeleton.internal.model.Userx;
import at.qe.skeleton.internal.model.UserxRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Scope("application")
public class RegistrationService {

  @Autowired private UserxService userService;

  @Autowired private EmailService emailService;

  @Autowired private TokenService tokenService;

  public void sendRegistrationEmail(String email, String token) {
    emailService.sendEmail(
        email,
        "Confirm your registration",
        "\nYour token: " + token + "\n\nIf you did not register, please ignore this email.");
  }

  public void registerUser(Userx user, String token) {
    if (userService.loadUser(user.getUsername()) != null) {
      throw new RuntimeException("Username already exists.");
    }
    if (userService.loadUserByEmail(user.getEmail()) != null) {
      throw new RuntimeException("Email already exists.");
    }
    user.setRoles(Set.of(UserxRole.REGISTERED_USER));
    user.setEnabled(false);
    try {
      sendRegistrationEmail(user.getEmail(), token);
    } catch (MailException e) {
      throw new RuntimeException("Invalid Email.");
    }
    userService.saveUser(user);
  }

  public void confirmRegistrationOfUser(String username, String token, String insertedToken) {
    Userx user = userService.loadUser(username);
    if (tokenService.validateToken(insertedToken, token)) {
      user.setEnabled(true);
      userService.saveUser(user);
    } else {
      throw new RuntimeException("Invalid token.");
    }
  }

  public void resendRegistrationEmailToUser(String email, String token) {
    Userx user = userService.loadUserByEmail(email);
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

  public Userx loadUsereByEmail(String email) {
    return userService.loadUserByEmail(email);
  }
}
