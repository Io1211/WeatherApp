package at.qe.skeleton.internal.ui.controllers;

import at.qe.skeleton.internal.model.Userx;
import at.qe.skeleton.internal.model.UserxRole;
import at.qe.skeleton.internal.services.PasswordResetService;
import at.qe.skeleton.internal.services.UserxService;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Controller for the user detail view.
 *
 * <p>This class is part of the skeleton project provided for students of the course "Software
 * Architecture" offered by Innsbruck University.
 */
@Component
@Scope("view")
public class UserDetailController implements Serializable {

  @Autowired private UserxService userService;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private PasswordResetService passwordResetService;
  private Set<UserxRole> userxRoles;

  private Set<UserxRole> initializeRoles;

  /** Attribute to cache the currently displayed user */
  private Userx user;

  private String newPassword;

  @PostConstruct
  public void init() {
    userxRoles = userService.getAllUserxRoles();
  }

  /**
   * Sets the currently displayed user and reloads it form db. This user is targeted by any further
   * calls of {@link #doReloadUser()}, {@link #doSaveUser()} and {@link #doDeleteUser()}.
   *
   * @param user
   */
  public void setUser(Userx user) {
    this.user = user;
    initializeRoles();
    doReloadUser();
  }

  /**
   * Returns the currently displayed user.
   *
   * @return
   */
  public Userx getUser() {
    return user;
  }

  public String getNewPassword() {
    return newPassword;}

  public void setNewPassword(String newPassword) {
    this.newPassword = newPassword;
  }
  /** Action to force a reload of the currently displayed user. */
  public void doReloadUser() {
    user = userService.loadUser(user.getUsername());
  }

  /** Action to save the currently displayed user. */
  public void doSaveUser() {
    resetPassword();
    user.setRoles(initializeRoles);
    user = this.userService.saveUser(user);
  }

  /** Action to delete the currently displayed user. */
  public void doDeleteUser() {
    this.userService.deleteUser(user);
    user = null;
  }

  /**
   * Resets the password of the currently displayed user if a new password has been set.
   */
  public void resetPassword() {
    if (newPassword != null && !newPassword.trim().isEmpty()) {
      user.setPassword(passwordEncoder.encode(newPassword));
    }
  }

    /**
     * Sends a password reset email to the currently displayed user.
     */
  public void sendResetPasswordAdmin() {
    passwordResetService.sendForgetPasswordEmail(user);
  }

    /**
     * Returns a list of all available user roles.
     *
     * @return all available user roles
     */
  public Set<UserxRole> getUserxRoles() {
    return userxRoles;
  }
  /**
   * Initialize current user roles.
   */
  private void initializeRoles() {
    if (user != null && user.getRoles() != null) {
      initializeRoles = new HashSet<>(user.getRoles());
    } else {
      initializeRoles = new HashSet<>();
    }
  }

  /**
   * Returns a list of current user roles.
   *
   * @return all available user roles
   */
  public Set<UserxRole> getInitializedRoles() {
    return initializeRoles;
  }

  public void setInitializedRoles(Set<UserxRole> initializeRoles) {
    this.initializeRoles = initializeRoles;
  }
}
