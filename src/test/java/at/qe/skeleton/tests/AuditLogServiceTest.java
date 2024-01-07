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
import at.qe.skeleton.internal.model.UserxRole;
import at.qe.skeleton.internal.services.AuditLogService;
import at.qe.skeleton.internal.model.AuditLog;

/**
 * Some tests for {@link AuditLogService} which test the three core methods.
 */
@SpringBootTest
@WebAppConfiguration
public class AuditLogServiceTest {

    @Autowired
    private AuditLogService auditLogService;

    @Test
    void saveEntryTest() {
        String message = "Test";
        doNothing().when(auditLogRepository).save(any(AuditLog.class));
        auditLogService.saveEntry(message);
        verify(auditLogRepository, times(1)).save(argThat(argument -> {
            return argument.getMessage().equals(message);
        }));
    }

    @Test
    void saveDeletedUserEntryTest() {
        Userx userx = new Userx();
        userx.setUsername("testUser");
        userx.setRoles(Sets.newSet(UserxRole.PREMIUM));
        auditLogService.saveDeletedUserEntry(userx);
        doNothing().when(auditLogService).saveEntry(anyString());
        auditLogService.saveDeletedUserEntry(userx);
        // check for correct delete message
        verify(auditLogService, times(1)).saveEntry("User testUser with role(s) PREMIUM has been deleted.");
    }


    @Test
    void saveCreatedUserEntryTest() {
        Userx userx = new Userx();
        userx.setUsername("testUser");
        userx.setRoles(Sets.newSet(UserxRole.ADMIN));
        doNothing().when(auditLogService).saveEntry(anyString());
        auditLogService.saveCreatedUserEntry(userx);
        // check for correct delete message
        verify(auditLogService, times(1)).saveEntry("User testUser with role(s) ADMIN has been saved.");
    }
}
