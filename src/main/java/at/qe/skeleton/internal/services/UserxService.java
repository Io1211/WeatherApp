package at.qe.skeleton.internal.services;

import at.qe.skeleton.internal.model.Userx;
import at.qe.skeleton.internal.repositories.UserxRepository;
import at.qe.skeleton.internal.services.AuditLogService;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;

/**
 * Service for accessing and manipulating user data.
 *
 * <p>This class is part of the skeleton project provided for students of the course "Software
 * Architecture" offered by Innsbruck University.
 */
@Component
@Scope("application")
public class UserxService {

  @Autowired private UserxRepository userRepository;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private AuditLogService auditLogService;

  /**
   * Returns a collection of all users.
   *
   * @return
   */
  public Collection<Userx> getAllUsers() {
    return userRepository.findAll();
  }

  /**
   * Loads a single user identified by its username.
   *
   * @param username the username to search for
   * @return the user with the given username
   */
  public Userx loadUser(String username) {
    return userRepository.findFirstByUsername(username);
  }

  /**
   * Saves the user. This method will also set {@link Userx#createDate} for new entities or {@link
   * Userx#updateDate} for updated entities. The user requesting this operation will also be stored
   * as {@link Userx#createDate} or {@link Userx#updateUser} respectively.
   *
   * @param user the user to save
   * @return the updated user
   */
  public Userx loadUserByEmail(String email) {
    return userRepository.findFirstByEmail(email);
  }

  public Userx saveUser(Userx user) throws JpaSystemException {
    if (user.isNew()) {
      user.setCreateUser(getAuthenticatedUser());
      String password = user.getPassword();
      // Passing null as an argument to the encoder throws IllegalArgumentException,
      // but we want JpaSystemException
      if (password == null) {
        throw new JpaSystemException(new RuntimeException("Password can't be empty"));
      }
      user.setPassword(passwordEncoder.encode(password));
    } else {
      user.setUpdateUser(getAuthenticatedUser());
    }
    auditLogService.saveCreatedUserEntry(user);
    return userRepository.save(user);
  }

  public void setPasswordEncoded(Userx user, String password) {
    user.setPassword(passwordEncoder.encode(password));
    userRepository.save(user);
  }

  /**
   * Deletes the user.
   *
   * @param user the user to delete
   */
  @PreAuthorize("hasAuthority('ADMIN')")
  public void deleteUser(Userx user) {
    userRepository.delete(user);
    auditLogService.saveDeletedUserEntry(user);
  }

  private Userx getAuthenticatedUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return userRepository.findFirstByUsername(auth.getName());
  }
}
