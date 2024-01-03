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

    // concatenates all roles from the set into a string
    public String convertRolesToString(Userx userx) {
        StringBuilder rolesAsString = new StringBuilder();
        for (UserxRole userxrole : userx.getRoles()) {
            rolesAsString.append(userxrole.name()).append(", ");
        }
        // removes the last "," at the end
        if (rolesAsString.length() > 0) {
            rolesAsString.setLength(rolesAsString.length() - 2);
        }
        return rolesAsString.toString();
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public void saveDeletedUserEntry(Userx userx) {
        saveEntry("User " + userx.getUsername() + "with role(s) " + convertRolesToString(userx) +" has been deleted.");
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public void saveCreatedUserEntry(Userx userx) {
        saveEntry("User " + userx.getUsername() + "with role(s) " + convertRolesToString(userx) + " has been saved.");
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public List<AuditLog> findAll() {
        return auditLogRepository.findAll();
    }
}
