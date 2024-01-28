package at.qe.skeleton.internal.ui.beans;

import at.qe.skeleton.internal.helper.WarningHelper;
import at.qe.skeleton.internal.model.Userx;
import at.qe.skeleton.internal.services.RegistrationService;
import at.qe.skeleton.internal.services.TokenService;
import at.qe.skeleton.internal.services.exceptions.*;
import jakarta.faces.application.FacesMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Component;

/**
 * Bean for managing the registration of new users.{@link RegistrationService}
 *
 * <p>This bean is used to register a new user and to confirm the registration. The user is
 * registered by entering the username, password and email address. The user is confirmed by
 * entering the token that was sent to the user's email address. This class is used in the
 * registration.xhtml and the confirm_registration.xhtml file.
 */
@Component
@Scope("session")
public class UserRegistrationBean {

  private Userx user = new Userx();

  @Autowired private RegistrationService registrationService;

  @Autowired TokenService tokenService;

  @Autowired private WarningHelper warningHelper;

  private String token;

  private String insertedToken;

  public Userx getUser() {
    return user;
  }

  public void setUser(Userx user) {
    this.user = user;
  }

  public void setPassword(String password) {
    this.user.setPassword(password);
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

  public String register() {
    try {
      setToken(tokenService.generateToken());
      registrationService.registerUser(user, getToken());
      return "confirm_registration";
    } catch (MailException e) {
      warningHelper.addMessage("Invalid email", FacesMessage.SEVERITY_ERROR);
      return null;
    } catch (RegistrationUsernameAlreadyExistsException
        | RegistrationEmailAlreadyExistsException e) {
      warningHelper.addMessage(e.getMessage(), FacesMessage.SEVERITY_ERROR);
      return null;
    }
  }

  /**
   * Resends the registration email with the token to the user based on the inserted email address.
   *
   * @throws RegistrationUserAlreadyEnabledException when user is already enabled
   * @throws NoUserFoundException when no user is found
   */
  public String resendRegistrationEmail() {
    try {
      setToken(tokenService.generateToken());
      registrationService.resendRegistrationEmailToUser(user.getEmail(), getToken());
      user = registrationService.loadUserByEmail(user.getEmail());
      return "confirm_registration";
    } catch (MailException e) {
      warningHelper.addMessage("Invalid email", FacesMessage.SEVERITY_ERROR);
      return null;
    } catch (NoUserFoundException | RegistrationUserAlreadyEnabledException e) {
      warningHelper.addMessage(e.getMessage(), FacesMessage.SEVERITY_ERROR);
      return null;
    }
  }

  public String confirmRegistration() {
    try {
      registrationService.confirmRegistrationOfUser(user.getUsername(), token, insertedToken);
      return "login";
    } catch (RegistrationInvalidTokenException e) {
      warningHelper.addMessage(e.getMessage(), FacesMessage.SEVERITY_ERROR);
      return null;
    }
  }
}
