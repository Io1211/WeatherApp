package at.qe.skeleton.internal.ui.controllers;

import at.qe.skeleton.internal.model.AuditLog;
import at.qe.skeleton.internal.services.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/*
 * Used to display all the audit logs. 
 */

@Component
@Scope("view")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    public List<AuditLog> findLogs() {
        return auditLogService.findAll();
    }
}

