package at.qe.skeleton.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.mockito.InjectMocks;
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

    @InjectMocks
    private AuditLogService mockedAuditLogService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Mock
    private AuditLogRepository mockedAuditLogRepository;

    @InjectMocks
    private Userx userxMock;

    @Mock
    private UserxService userxService;

    @AfterEach
    public void resetMockito() {
        Mockito.reset(mockedAuditLogRepository);
        auditLogRepository.findAll().forEach(auditLogRepository::delete);
    }


    @Test
    public void convertRolesToStringTest() {
        
        Set<UserxRole> userRoles = new HashSet<>(Arrays.asList(UserxRole.ADMIN, UserxRole.PREMIUM_USER));
        when(userxMock.getRoles()).thenReturn(userRoles);

        String result = auditLogService.convertRolesToString(userxMock);
        assertTrue(result.contains("PREMIUM_USER") && result.contains("ADMIN"));
    }


    @Test
    public void saveEntryTest() {
        
        // create and save a message
        String message = "Test";
        auditLogService.saveEntry(message);
        
        AuditLog expectedAuditLog = new AuditLog();
        expectedAuditLog.setMessage(message);
        
        // check if an AuditLog has been saved and if the messages agree
        assertTrue(auditLogRepository.findAll().size() > 0);
        assertEquals(auditLogRepository.findAll().get(0).getMessage(), message);
    }

    
    @Test
    public void saveDeletedUserEntryTest() {
        
        // this pretends that the user has been deleted and a save is triggered
        auditLogService.saveEntry("User testUser with role(s) ADMIN has been deleted.");
    
        // now create and check if the log message is generated correctly
        userxMock.setUsername("testUser");
        userxMock.setRoles(Sets.newSet(UserxRole.ADMIN));
        ReflectionTestUtils.setField(auditLogService, "auditLogRepository", mockedAuditLogRepository);
        String msg = mockedAuditLogService.saveDeletedUserEntry(userxMock);

        // check the log entries if the most recent ones match
        // the test will fail if no element has been saved since get(0) can't be done on an empty list
        List<AuditLog> als = auditLogRepository.findAll();
        assertTrue(als.size() >= 1);
        assertEquals(als.get(0).getMessage(), msg);
        ReflectionTestUtils.setField(auditLogService, "mockedauditLogRepository", auditLogRepository);
    }
    

    @Test
    public void saveCreatedUserEntryTest() {

        // this pretends that the user has been created and saved which triggers a save
        auditLogService.saveEntry("User testUser with role(s) ADMIN has been saved.");
        
        // now create and check if the log message is generated correctly
        userxMock.setUsername("testUser");
        userxMock.setRoles(Sets.newSet(UserxRole.ADMIN));
        ReflectionTestUtils.setField(auditLogService, "auditLogRepository", mockedAuditLogRepository);
        String msg = mockedAuditLogService.saveCreatedUserEntry(userxMock);

        //check the log entries if the most recent ones match
        // the test will fail if no element has been saved since get(0) can't be done on an empty list
        List<AuditLog> als = auditLogRepository.findAll();
        assertTrue(als.size() >= 1);
        assertEquals(als.get(0).getMessage(), msg);
        ReflectionTestUtils.setField(auditLogService, "mockedauditLogRepository", auditLogRepository);
    }
}
