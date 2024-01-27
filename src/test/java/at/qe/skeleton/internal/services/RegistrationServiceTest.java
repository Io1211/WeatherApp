package at.qe.skeleton.internal.services;

import at.qe.skeleton.internal.model.Userx;
import at.qe.skeleton.internal.model.UserxRole;
import at.qe.skeleton.internal.repositories.UserxRepository;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
class RegistrationServiceTest {

  @Mock EmailService mockedEmailService;
  RegistrationService registrationService;

  @Autowired UserxRepository userxRepository;
  @Autowired TokenService tokenService;
  @Autowired PasswordEncoder passwordEncoder;

  /**
   * Since we don`t use Constructor Injection we need to use this method for injecting the real
   * autowired UserxRepo, TokenService and PasswordEncoder. We need to mock the EmailService though,
   * as using the real one would require the registrationService to communicate with external
   * contexts, which is not within the scope of this test class.
   */
  @BeforeEach
  public void init() {
    registrationService =
        new RegistrationService(
            this.passwordEncoder, this.mockedEmailService, this.tokenService, this.userxRepository);
    //    ReflectionTestUtils.setField(registrationService, "userRepository", userxRepository);
    //    ReflectionTestUtils.setField(registrationService, "tokenService", tokenService);
    //    ReflectionTestUtils.setField(registrationService, "passwordEncoder", passwordEncoder);
  }

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
    String token = tokenService.generateToken();

    // create test user from prepared data
    Userx user = new Userx();
    user.setRoles(userRoles);
    user.setPassword(password);
    user.setUsername(username);
    user.setEmail(email);

    // register test user
    registrationService.registerUser(user, token);

    // get User back from repo and test if registered correctly
    Userx registeredUser = userxRepository.findFirstByEmail(email);
    assertEquals(
        password,
        registeredUser.getPassword(),
        "password should be the same as the one user was created with");
    assertEquals(
        username,
        registeredUser.getUsername(),
        "Username should be the same as the one user was created with");
    Assertions.assertFalse(
        registeredUser.isEnabled(),
        "enabled should be set to false since user has not yet confirmed his registration");
  }

  @Test
  void registrationOfInvalidUser() {}

  @Test
  @DirtiesContext
  void confirmRegistrationOfUser() {
    // prepare user data
    Set<UserxRole> userRoles = Set.of(UserxRole.REGISTERED_USER, UserxRole.PREMIUM_USER);
    String password = "pw";
    String username = "uwe";
    String email = "uwe@test.at";
    String token = tokenService.generateToken();

    // create test user from prepared data
    Userx user = new Userx();
    user.setRoles(userRoles);
    user.setPassword(password);
    user.setUsername(username);
    user.setEmail(email);

    // register test user
    registrationService.registerUser(user, token);

    // make sure User is not enabled:
    Userx registeredUser = userxRepository.findFirstByUsername(username);
    assertEquals(
        username,
        registeredUser.getUsername(),
        "Username should be the same as the one user was created with");
    Assertions.assertFalse(
        registeredUser.isEnabled(),
        "User should not be enabled, since he hasnt been confirmed yet");

    assertFalse(
        passwordEncoder.matches(password, registeredUser.getPassword()),
        "the password that was entered by the user should not be encoded yet and therefor not match by the criteria of passwordEncoder");

    // actual call of confirmation method
    registrationService.confirmRegistrationOfUser(username, token, token);

    // update the user after registration
    Userx confirmedUser = userxRepository.findFirstByUsername(username);

    assertTrue(
        confirmedUser.isEnabled(),
        "the newly registered user should be enabled after calling confirmRegistrationOfUser");

    assertTrue(
        passwordEncoder.matches(password, confirmedUser.getPassword()),
        "the stored passwort should be ancoded and match the raw password");
  }

  @Test
  void resendRegistrationEmailToUser() {}

  @Test
  void loadUserByEmail() {}

  @Test
  void loadUserByUsername() {}
}
