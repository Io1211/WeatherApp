package at.qe.skeleton.repositories;

import at.qe.skeleton.model.AuditLog;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends AbstractRepository<AuditLog, Long> {

    @Query("SELECT al FROM AuditLog al WHERE al.message LIKE %:username%")
    List<AuditLog> findLogsForUser(@Param("username") String username);

    @Query("SELECT al FROM AuditLog al WHERE al.date >= :cutoffTime")
    List<AuditLog> findLogsInLastHour(LocalDateTime cutoffTime);
}
