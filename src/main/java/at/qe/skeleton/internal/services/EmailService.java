package at.qe.skeleton.internal.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@Scope("application")
public class EmailService {

  @Autowired private JavaMailSender mailSender;

  @Value("${spring.mail.username}")
  private String projectEmail;

  public void sendEmail(String email, String subject, String message) {
    SimpleMailMessage mailMessage = new SimpleMailMessage();
    mailMessage.setFrom(projectEmail);
    mailMessage.setTo(email);
    mailMessage.setText(message);
    mailMessage.setSubject(subject);

    mailSender.send(mailMessage);
    System.out.println(
        "Sending email to " + email + " with subject " + subject + " and message " + message);
  }
}
