package at.qe.skeleton.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import at.qe.skeleton.internal.model.Userx;
import at.qe.skeleton.internal.model.UserxRole;
import at.qe.skeleton.internal.services.AuditLogService;
import at.qe.skeleton.internal.services.UserxService;
import at.qe.skeleton.internal.model.AuditLog;
import at.qe.skeleton.internal.repositories.AuditLogRepository;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.collections.Sets;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.List;


/**
 * Some tests for {@link AuditLogService} which test the most important methods.
 */
@SpringBootTest
@WebAppConfiguration
public class AuditLogServiceTest {
    
    @Autowired 
    private AuditLogService auditLogService;
    
    @Autowired 
    private AuditLogRepository auditLogRepository;

    @Mock 
    private AuditLogRepository mockedAuditLogRepository; 

    @Mock
    private Userx userxMock;

    @Mock
    private UserxService userxService;

    @AfterEach
    void resetMockito() {
        Mockito.reset(mockedAuditLogRepository);
        auditLogRepository.findAll().forEach(auditLogRepository::delete);
    }


    @Test
    void saveEntryTest() {
        String message = "Test";

        ReflectionTestUtils.setField(auditLogService, "auditLogRepository", mockedAuditLogRepository); 
        auditLogService.saveEntry(message);
        
        AuditLog expectedAuditLog = new AuditLog();
        expectedAuditLog.setMessage(message);
                
        verify(mockedAuditLogRepository, times(1)).save(expectedAuditLog);
    }


    @Test
    void convertRolesToStringTest() {
        
        Set<UserxRole> userRoles = new HashSet<>(Arrays.asList(UserxRole.ADMIN, UserxRole.PREMIUM_USER));
        when(userxMock.getRoles()).thenReturn(userRoles);

        String result = auditLogService.convertRolesToString(userxMock);
        assertTrue(result.contains("PREMIUM_USER") && result.contains("ADMIN"));
    }


    @Test
    void saveDeletedUserEntryTest() {
        //Userx userx = new Userx();
        userxMock.setUsername("testUser");
        userxMock.setRoles(Sets.newSet(UserxRole.PREMIUM_USER));

        // this pretends that the user has been deleted and a save is triggered
        auditLogService.saveDeletedUserEntry(userxMock);
        
        // actually delete the user
        userxService.deleteUser(userxMock);

        //check the log entries if the most recent ones match
        List<AuditLog> als = auditLogRepository.findAll();
        assertEquals(als.get(0), als.get(1));
    }


    @Test
    void saveCreatedUserEntryTest() {
        Userx userx = new Userx();
        userx.setUsername("testUser");
        userx.setRoles(Sets.newSet(UserxRole.ADMIN));

        doNothing().when(auditLogService).saveEntry(anyString());
        
        // save and check log entry
        auditLogService.saveCreatedUserEntry(userx);
        verify(auditLogService, times(1)).saveEntry("User testUser with role(s) ADMIN has been saved.");
    }
}
