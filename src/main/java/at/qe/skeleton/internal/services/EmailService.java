package at.qe.skeleton.internal.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Service for sending emails.
 *
 * <p>This service is used to send emails to the user. The email server is configured in the
 * application.properties file.
 */
@Component
@Scope("application")
public class EmailService {

  @Autowired private JavaMailSender mailSender;

  @Value("${spring.mail.username}")
  private String projectEmail;

  public void sendEmail(String email, String subject, String message)
      throws IllegalArgumentException, MailException {
    if (email == null || email.isEmpty()) {
      throw new IllegalArgumentException("Email address must not be empty");
    }

    SimpleMailMessage mailMessage = new SimpleMailMessage();
    mailMessage.setFrom(projectEmail);
    mailMessage.setTo(email);
    mailMessage.setText(message);
    mailMessage.setSubject(subject);
    mailSender.send(mailMessage);
  }
}
