package at.qe.skeleton.internal.services;

import org.junit.jupiter.api.BeforeAll;

import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;

public class EmailServiceTest {
  static EmailService emailService;

  static String email;
  static JavaMailSender mockedMailSender;

  @BeforeAll
  public static void setUp() {
    emailService = new EmailService();
    mockedMailSender = mock(JavaMailSender.class);
    email = "ourmail.com";
    ReflectionTestUtils.setField(emailService, "mailSender", mockedMailSender);
    ReflectionTestUtils.setField(emailService, "projectEmail", email);
  }

  @Test
  public void sendEmailTest() {
    String email = "test@Mail.com";
    String subject = "Tester";
    String message = "you have been tested";

    emailService.sendEmail(email, subject, message);

    verify(mockedMailSender, times(1)).send(any(SimpleMailMessage.class));
  }
}
