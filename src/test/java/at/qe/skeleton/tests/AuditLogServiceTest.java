package at.qe.skeleton.tests;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

import at.qe.skeleton.internal.model.Userx;
import at.qe.skeleton.internal.model.UserxRole;
import at.qe.skeleton.internal.services.AuditLogService;
import at.qe.skeleton.internal.model.AuditLog;
import at.qe.skeleton.internal.repositories.AuditLogRepository;

import org.mockito.internal.util.collections.Sets;

import java.util.List;
import java.util.Set;

/**
 * Some tests for {@link AuditLogService} which test the most important methods. The method used to
 * convert roles to a string is not explicitly tested since it is used solely by other methods which
 * are tested here.
 */
@SpringBootTest
@WebAppConfiguration
public class AuditLogServiceTest {

  @Autowired private AuditLogService auditLogService;

  @Autowired private AuditLogRepository auditLogRepository;

  private final Userx testUser = new Userx();

  @AfterEach
  public void resetDB() {
    auditLogRepository.findAll().forEach(auditLogRepository::delete);
  }

  /*
   * Test to ensure that the log entries are saved correctly.
   */
  @Test
  public void saveEntryTest() {

    // create and save a message
    String message = "Test";
    auditLogService.saveEntry(message);

    // check if an AuditLog has been saved and if the messages agree
    assertEquals(1, auditLogRepository.findAll().size());
    assertEquals(auditLogRepository.findAll().get(0).getMessage(), message);
  }

  /*
   * Test to ensure that a log entry is saved when a user is deleted and if the
   * correct log message is saved.
   */
  @Test
  public void saveDeletedUserEntryTest() {

    // create testUser
    String username = "testUser";
    UserxRole userRole = UserxRole.ADMIN;
    testUser.setUsername(username);
    testUser.setRoles(Sets.newSet(userRole));

    // actual method call
    auditLogService.saveDeletedUserEntry(testUser);

    // check expected behavior:
    String expectedMessage =
        "User %s with role(s) %s has been deleted.".formatted(username, userRole);
    List<AuditLog> als = auditLogRepository.findAll();
    Assertions.assertEquals(1, als.size());
    assertEquals(als.get(0).getMessage(), expectedMessage);
  }

  /*
   * Test to ensure that a log entry is saved when a user is created or changed
   * and if the correct log message is saved.
   */
  @Test
  public void saveCreatedUserEntryTest() {

    // create testUser
    String username = "testUser";
    Set<UserxRole> userRoles = Sets.newSet(UserxRole.ADMIN, UserxRole.PREMIUM_USER);
    testUser.setUsername(username);
    testUser.setRoles(userRoles);

    // actual method call
    AuditLog logBeforeSaving = auditLogService.saveCreatedUserEntry(testUser);

    // expected log message
    String expectedMessage =
        "User %s with role(s) ADMIN, PREMIUM_USER has been saved.".formatted(username);

    // check the log entries if the most recent ones match
    // the test will fail if no element has been saved since get(0) can't be done on an empty list
    List<AuditLog> als = auditLogRepository.findAll();
    assertEquals(1, als.size());
    AuditLog logAfterSaving = als.get(0);

    assertEquals(logBeforeSaving, logAfterSaving, "log before and after Saving should be equal");

    assertEquals(
        expectedMessage,
        logAfterSaving.getMessage(),
        "The Log retrieved from db does not display expected message");
    // log

  }

  @Test
  public void auditLogsShouldNotBeEqual() {
    // set up TestUserData
    String username = "testUser";
    Set<UserxRole> userRoles = Sets.newSet(UserxRole.ADMIN, UserxRole.PREMIUM_USER);
    testUser.setUsername(username);
    testUser.setRoles(userRoles);

    AuditLog auditLogOnCreation = auditLogService.saveCreatedUserEntry(testUser);
    AuditLog auditLogOnDeletion = auditLogService.saveDeletedUserEntry(testUser);

    Assertions.assertNotEquals(
        auditLogOnCreation,
        auditLogOnDeletion,
        "according to overwritten equals method, these auditlogs should not be equal");
  }

  @Test
  public void auditLogEqualsTest() {
    // set up TestUserData
    String username = "testUser";
    Set<UserxRole> userRoles = Sets.newSet(UserxRole.ADMIN, UserxRole.PREMIUM_USER);
    testUser.setUsername(username);
    testUser.setRoles(userRoles);
    // a basic auditlog
    AuditLog auditLog = auditLogService.saveCreatedUserEntry(testUser);
    // create anotherAuditlog with same id
    AuditLog auditLogWithSameId = new AuditLog();
    auditLogWithSameId.setId(auditLog.getId());
    // create a different Auditlog
    AuditLog auditLogDifferentId = auditLogService.saveDeletedUserEntry(testUser);

    String notAnAuditLogObject = "not an auditLog";
    AuditLog auditLogNull = null;

    Assertions.assertEquals(
        auditLog, auditLog, "should equal, since it is exact same object reference");
    Assertions.assertEquals(
        auditLog,
        auditLogWithSameId,
        "according to overwritten equals method, these auditlogs should not be equal");
    Assertions.assertNotEquals(
        auditLog, auditLogDifferentId, "Ids should be different, therefore should not equal");
    Assertions.assertNotEquals(
        auditLog,
        notAnAuditLogObject,
        "this is not an auditLog Object and should not equal an auditLog Object");
    Assertions.assertNotEquals(auditLog, auditLogNull, "AuditLog should not equal null");
  }
}
