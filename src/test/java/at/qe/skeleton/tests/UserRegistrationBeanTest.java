package at.qe.skeleton.tests;

import at.qe.skeleton.internal.model.Userx;
import at.qe.skeleton.internal.services.RegistrationService;
import at.qe.skeleton.internal.services.TokenService;
import at.qe.skeleton.internal.services.UserxService;
import at.qe.skeleton.internal.ui.beans.UserRegistrationBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for the UserRegistrationBean class. {@link UserRegistrationBean} */
class UserRegistrationBeanTest {

  @InjectMocks private UserRegistrationBean userRegistrationBean;

  @Mock private RegistrationService registrationService;

  @Mock private TokenService tokenService;

  @Mock private UserxService userService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testRegister() {
    Userx testUser = new Userx();
    testUser.setUsername("test");
    testUser.setPassword("test");
    testUser.setEmail("test@gmail.com");

    userRegistrationBean.setUser(testUser);
    when(tokenService.generateToken()).thenReturn("12345");


    String result = userRegistrationBean.register();

    assertEquals("confirm_registration", result);
    verify(registrationService).registerUser(testUser, "12345");
  }

  @Test
  void testConfirmRegistrationOfUser() {
    Userx user = new Userx();
    user.setUsername("user");
    user.setEnabled(false);
    userRegistrationBean.setToken("12345");
    userRegistrationBean.setUser(user);

    assertFalse(user.isEnabled());
    userRegistrationBean.setInsertedToken("12345");
    userRegistrationBean.confirmRegistration();

    verify(registrationService).confirmRegistrationOfUser(user.getUsername(), "12345", "12345");
  }
}
