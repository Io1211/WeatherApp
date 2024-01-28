package at.qe.skeleton.internal.repositories;

import at.qe.skeleton.internal.model.AuditLog;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

/** Repository for audit logs, based on the base repository. */
public interface AuditLogRepository extends AbstractRepository<AuditLog, Long> {

  /**
   * Lists all entries of a given user.
   *
   * @param username is the user by whom whould be filtered.
   */
  @Query("SELECT al FROM AuditLog al WHERE al.message LIKE %:username%")
  List<AuditLog> findRoleChangesByUserName(@Param("username") String username);
}
