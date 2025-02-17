package at.qe.skeleton.tests;

import at.qe.skeleton.internal.model.UserxRole;
import at.qe.skeleton.internal.services.UserxService;
import at.qe.skeleton.internal.ui.beans.SessionInfoBean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Some very basic tests for {@link UserxService}.
 *
 * <p>This class is part of the skeleton project provided for students of the courses "Software
 * Architecture" and "Software Engineering" offered by the University of Innsbruck.
 */
@SpringBootTest
@WebAppConfiguration
public class SessionInfoBeanTest {

  @Autowired SessionInfoBean sessionInfoBean;

  @Autowired UserxService userService;

  @Test
  @WithMockUser(
      username = "user1",
      authorities = {"REGISTERED_USER"})
  public void testLoggedIn() {
    Assertions.assertTrue(
        sessionInfoBean.isLoggedIn(),
        "sessionInfoBean.isLoggedIn does not return true for authenticated user");
    Assertions.assertEquals(
        "user1",
        sessionInfoBean.getCurrentUserName(),
        "sessionInfoBean.getCurrentUserName does not return authenticated user's name");
    Assertions.assertEquals(
        "user1",
        sessionInfoBean.getCurrentUser().getUsername(),
        "sessionInfoBean.getCurrentUser does not return authenticated user");
    Assertions.assertEquals(
        "REGISTERED_USER",
        sessionInfoBean.getCurrentUserRoles(),
        "sessionInfoBean.getCurrentUserRoles does not return authenticated user's roles");
    Assertions.assertTrue(
        sessionInfoBean.hasRole("REGISTERED_USER"),
        "sessionInfoBean.hasRole does not return true for a role the authenticated user has");
    Assertions.assertFalse(
        sessionInfoBean.hasRole("ADMIN"),
        "sessionInfoBean.hasRole does not return false for a role the authenticated user does not have");
  }

  @Test
  public void testNotLoggedIn() {
    Assertions.assertFalse(
        sessionInfoBean.isLoggedIn(),
        "sessionInfoBean.isLoggedIn does return true for not authenticated user");
    Assertions.assertEquals(
        "",
        sessionInfoBean.getCurrentUserName(),
        "sessionInfoBean.getCurrentUserName does not return empty string when not logged in");
    Assertions.assertNull(
        sessionInfoBean.getCurrentUser(),
        "sessionInfoBean.getCurrentUser does not return null when not logged in");
    Assertions.assertEquals(
        "",
        sessionInfoBean.getCurrentUserRoles(),
        "sessionInfoBean.getCurrentUserRoles does not return empty string when not logged in");
    for (UserxRole role : UserxRole.values()) {
      Assertions.assertFalse(
          sessionInfoBean.hasRole(role.name()),
          "sessionInfoBean.hasRole does not return false for all possible roales");
    }
  }
}
