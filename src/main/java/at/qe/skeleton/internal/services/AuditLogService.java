package at.qe.skeleton.internal.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import at.qe.skeleton.internal.model.AuditLog;
import at.qe.skeleton.internal.repositories.AuditLogRepository;
import at.qe.skeleton.internal.model.UserxRole;
import at.qe.skeleton.internal.model.Userx;
import java.time.LocalDateTime;
import java.util.List;

/** This service is used to track all role changes from users. */
@Service
public class AuditLogService {

  @Autowired private AuditLogRepository auditLogRepository;

  /**
   * Saves an entry into the database. The entry is given by a string.
   *
   * @param message is the message which is saves.
   * @return the AuditLog Object
   */
  public AuditLog saveEntry(String message) {
    AuditLog al = new AuditLog();
    al.setMessage(message);
    al.setDate(LocalDateTime.now());
    auditLogRepository.save(al);
    return al;
  }

  /**
   * Concatenates all roles from the set into a string.
   *
   * @param userx is the user whose roles will be converted into a string.
   */
  public String convertRolesToString(Userx userx) {
    StringBuilder rolesAsString = new StringBuilder();
    for (UserxRole userxrole : userx.getRoles()) {
      rolesAsString.append(userxrole.name()).append(", ");
    }
    // removes the last "," at the end
    if (!rolesAsString.isEmpty()) {
      rolesAsString.setLength(rolesAsString.length() - 2);
    }
    return rolesAsString.toString();
  }

  /**
   * Creates the corresponding message which will be logged when a user is deleted.
   *
   * @param userx is the user which is being deleted.
   * @return the Log statement
   */
  public AuditLog saveDeletedUserEntry(Userx userx) {
    String msg =
        "User "
            + userx.getUsername()
            + " with role(s) "
            + convertRolesToString(userx)
            + " has been deleted.";
    return saveEntry(msg);
  }

  /**
   * Creates the corresponding message which will be logged when a user is created.
   *
   * @param userx is the user which has been created.
   * @return the Log statement
   */
  public AuditLog saveCreatedUserEntry(Userx userx) {
    String msg =
        "User "
            + userx.getUsername()
            + " with role(s) "
            + convertRolesToString(userx)
            + " has been saved.";
    return saveEntry(msg);
  }

  /** Displays all audit logs saved. Requires Admin rights. */
  @PreAuthorize("hasAuthority('ADMIN')")
  public List<AuditLog> findAll() {
    return auditLogRepository.findAll();
  }
}
