package at.qe.skeleton.internal.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import at.qe.skeleton.internal.model.AuditLog;
import at.qe.skeleton.internal.repositories.AuditLogRepository;
import at.qe.skeleton.internal.model.Userx;
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

    public void saveDeletedUserEntry(Userx userx) {
        saveEntry("User with username " + userx.getUsername() + "and role(s) " + " has been deleted!");
        // + String.join(", ", userx.getRoles())
    }

    //public void saveCreatedUserEntry(Userx userx) {
    //    saveEntry("User with username " + userx.getUsername() + "and role(s) " + String.join(", ", userx.getRoles()) + " has been saved!");
    //}

    //public void saveModifiedUserEntry(Userx userx) {
    //    saveEntry("User with username " + userx.getUsername() + "has changed to the role(s) " + String.join(", ", userx.getRoles()) + ".");
    //}

    //public List<AuditLog> findAll(String username) {
    //    return auditLogRepository.findAll(username);
    //}

    //@PreAuthorize("hasAuthority('ADMIN')")
    
    public List<AuditLog> findAll() {
        return auditLogRepository.findAll();
    }
}
