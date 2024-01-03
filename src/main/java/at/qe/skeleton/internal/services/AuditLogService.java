package at.qe.skeleton.internal.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import at.qe.skeleton.internal.model.AuditLog;
import at.qe.skeleton.internal.repositories.AuditLogRepository;
import at.qe.skeleton.internal.model.Userx;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

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

    @PreAuthorize("hasAuthority('ADMIN')")
    public void saveDeletedUserEntry(Userx userx) {
        saveEntry("User " + userx.getUsername() + "with role(s) " + " has been deleted.");
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

    @PreAuthorize("hasAuthority('ADMIN')")
    public List<AuditLog> findAll() {
        return auditLogRepository.findAll();
    }
}
