package at.qe.skeleton.internal.services;

import at.qe.skeleton.internal.model.Userx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@Scope("application")
public class PasswordResetService {

  @Autowired private UserxService userxService;

  @Autowired private EmailService emailService;

  public void sendPasswordResetEmailAndToken(String email, String token) {
    if (userxService.loadUserByEmail(email) == null) {
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

  public void resetPassword(String email, String newPassword) {
    Userx user = userxService.loadUserByEmail(email);
    if (user == null) {
      //TO DO: exception handling for application internal errors
      throw new IllegalArgumentException("User not found for email " + email);
    } else {
      userxService.setPasswordEncoded(user, newPassword);
      System.out.println("Resetting password for " + email + " to " + user.getPassword());
    }
  }
}
