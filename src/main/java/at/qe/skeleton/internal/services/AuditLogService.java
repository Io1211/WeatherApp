package at.qe.skeleton.services;

import at.qe.skeleton.model.AuditLog;
import at.qe.skeleton.repositories.AuditLogRepository;
import at.qe.skeleton.model.Userx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public void saveEntry(String message) {
        AuditLog al = new AuditLog();
        al.setMessage(message);
        al.setDate(LocalDateTime.now());
        auditLogRepository.save(al);
    }

    public String convertRolesToString(Userx userx) {
        return String.join(", ", userx.getRoles());
    }

    public void saveDeletedUserEntry(Userx userx) {
        saveEntry("User with username " + userx.getUsername() + "and role(s) " + convertRolesToString(userx) + " has been deleted!");
    }

    public void saveCreatedUserEntry(Userx userx) {
        saveEntry("User with username " + userx.getUsername() + "and role(s) " + convertRolesToString(userx) + " has been saved!");
    }

    public void saveModifiedUserEntry(Userx userx) {
        saveEntry("User with username " + userx.getUsername() + "has changed to the role(s) " + convertRolesToString(userx) + ".");
    }

    public List<AuditLog> getLogsForUser(String username) {
        return auditLogRepository.findLogsForUser(username);
    }

    public List<AuditLog> getLogsInLastHour() {
        return auditLogRepository.findLogsInLastHour(LocalDateTime.now().minusHours(1));
    }
}
