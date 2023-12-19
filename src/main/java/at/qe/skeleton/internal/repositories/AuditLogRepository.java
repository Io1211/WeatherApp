package at.qe.skeleton.repositories;

import at.qe.skeleton.model.AuditLog;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AuditLogRepository extends AbstractRepository<AuditLog, Long> {

    @Query("SELECT al FROM AuditLog al WHERE al.message LIKE %:username% AND al.message LIKE '%has changed to the role(s)%'")
    List<AuditLog> findLogsForUser(@Param("username") String username);
}
