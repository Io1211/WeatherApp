package at.qe.skeleton.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.web.WebAppConfiguration;

import at.qe.skeleton.internal.model.Userx;
import at.qe.skeleton.internal.services.AuditLogService;
import at.qe.skeleton.internal.services.AuditLog;

/**
 * Some tests for {@link AuditLogService}.
 */

@SpringBootTest
@WebAppConfiguration
public class AuditLogServiceTest {

    @Autowired
    AuditLogService auditLogService;

}