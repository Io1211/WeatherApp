package at.qe.skeleton.repositories;

import at.qe.skeleton.model.AuditLog;
import at.qe.skeleton.model.Userx;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends AbstractRepository<AuditLog, Long> {

    @Query("SELECT al FROM AuditLog al WHERE al.message LIKE %:username%")
    List<AuditLog> findAll(@Param("username") String username);

}
