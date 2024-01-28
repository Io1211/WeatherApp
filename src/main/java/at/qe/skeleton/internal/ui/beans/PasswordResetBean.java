package at.qe.skeleton.internal.ui.beans;

import at.qe.skeleton.internal.helper.WarningHelper;
import at.qe.skeleton.internal.services.PasswordResetService;
import at.qe.skeleton.internal.services.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import jakarta.faces.application.FacesMessage;

/**
 * Bean for managing the password reset. {@link PasswordResetService}
 *
 * <p>This bean is used to send a password reset email to the user and to reset the password. The is
 * used to send a password reset email to the user and to reset the password. The user is identified
 * by the email address. This class is used in the reset_password.xhtml and the reset_password.xhtml
 * file.
 */
@Component
@Scope("session")
public class PasswordResetBean {

  @Autowired private PasswordResetService passwordResetService;

  @Autowired private TokenService tokenService;

  @Autowired private WarningHelper warningHelper;

  private String email;
  private String token;
  private String insertedToken;
  private String newPassword;
  private String newPasswordRepeat;

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getInsertedToken() {
    return insertedToken;
  }

  public void setInsertedToken(String insertedToken) {
    this.insertedToken = insertedToken;
  }

  public String getNewPassword() {
    return newPassword;
  }

  public void setNewPassword(String newPassword) {
    this.newPassword = newPassword;
  }

  public String getNewPasswordRepeat() {
    return newPasswordRepeat;
  }

  public void setNewPasswordRepeat(String newPasswordRepeat) {
    this.newPasswordRepeat = newPasswordRepeat;
  }

  public String sendPasswordResetEmail() {
    try {
      setToken(tokenService.generateToken());
      passwordResetService.sendPasswordResetEmailAndToken(getEmail(), getToken());
    } catch (IllegalArgumentException e) {
      warningHelper.addMessage(
          "User not found for email " + getEmail(), FacesMessage.SEVERITY_ERROR);
      return null;
    }
    return "reset_password";
  }

  public boolean validatePasswordResetToken() {
    return tokenService.validateToken(getToken(), getInsertedToken());
  }

  public String resetPassword() {
    if (insertedToken == null || insertedToken.isEmpty()) {
      warningHelper.addMessage("Please enter your token", FacesMessage.SEVERITY_ERROR);
      return null;
    }
    if (!validatePasswordResetToken()) {
      warningHelper.addMessage("Invalid token", FacesMessage.SEVERITY_ERROR);
      return null;
    }
    if (newPassword == null || newPassword.isEmpty()) {
      warningHelper.addMessage("Please enter a new password", FacesMessage.SEVERITY_ERROR);
      return null;
    }
    if (newPasswordRepeat == null || newPasswordRepeat.isEmpty()) {
      warningHelper.addMessage("Please repeat your new password", FacesMessage.SEVERITY_ERROR);
      return null;
    }
    if (!newPassword.equals(newPasswordRepeat)) {
      warningHelper.addMessage("Passwords do not match", FacesMessage.SEVERITY_ERROR);
      return null;
    }

    passwordResetService.resetPassword(email, newPassword);
    return "login";
  }
}
