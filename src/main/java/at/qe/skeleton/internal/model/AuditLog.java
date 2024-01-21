package at.qe.skeleton.internal.model;

import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity for an AuditLog. The audit log stores a message (string) which is generated when a user is
 * saved (modified) or deleted. It contains a date, message and id.
 *
*/
@Entity
public class AuditLog implements Persistable<Long>, Serializable {

    // Generate an id to be able to store the audit log.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The message saved is a string which is built depending on the manipulation of the user.
    private String message;

    // A timestamp is added to organize the audit logs and track role changes in a meaningful way.
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime date;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override public Long getId() {
        return id;
    }

    // Allows to check whether an audit log is new and no id has been set.
    @Override public boolean isNew() {
        return id == null;
    }

    // Implements an equals method for audit logs.
    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AuditLog auditLog = (AuditLog) o;
        return Objects.equals(id, auditLog.id);
    }

    // Overrides the hashCode method and generates a hash code based on the id of the audit log.
    @Override public int hashCode() {
        return Objects.hash(id);
    }
}
