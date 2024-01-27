package at.qe.skeleton.internal.services;

import at.qe.skeleton.internal.model.Userx;
import at.qe.skeleton.internal.model.UserxRole;
import at.qe.skeleton.internal.repositories.UserxRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
class RegistrationServiceTest {

  @Mock EmailService mockedEmailService;
  @InjectMocks RegistrationService registrationService;

  @Autowired UserxRepository userxRepository;

  @BeforeEach
  public void init() {
    // since we don`t use Constructor Injection we need to use this method for injecting the real
    // autowired UserxRepo
    ReflectionTestUtils.setField(registrationService, "userRepository", userxRepository);
  }

  // todo: update the exact Exceptions

  @Test
  void sendRegistrationEmail() {
    String email = "test@mail.com";
    String token = "token";

    // these should be added by sendRegistrationEmail method
    String subject = "Confirm your registration";
    String message =
        "\nYour token: " + token + "\n\nIf you did not register, please ignore this email.";

    registrationService.sendRegistrationEmail(email, token);

    verify(mockedEmailService, times(1)).sendEmail(email, subject, message);
  }

  @Test
  @DirtiesContext
  void registerValidUser() {
    // prepare user data
    Set<UserxRole> userRoles = Set.of(UserxRole.REGISTERED_USER, UserxRole.PREMIUM_USER);
    String password = "pw";
    String username = "uwe";
    String email = "uwe@test.at";
    String token = "abcd";

    Userx user = new Userx();
    user.setRoles(userRoles);
    user.setPassword(password);
    user.setUsername(username);
    user.setEmail(email);

    registrationService.registerUser(user, token);

    // get User back from repo and test if registered correctly
    Userx registeredUser = userxRepository.findFirstByEmail(email);

    Assertions.assertEquals(password, registeredUser.getPassword());
    Assertions.assertEquals(username, registeredUser.getUsername());
    // because user has not yet confirmed his registration, enabled should be set to false
    Assertions.assertFalse(registeredUser.isEnabled());
  }

  @Test
  void registrationOfInvalidUser() {}

  @Test
  void confirmRegistrationOfUser() {}

  @Test
  void resendRegistrationEmailToUser() {}

  @Test
  void loadUserByEmail() {}

  @Test
  void loadUserByUsername() {}
}
