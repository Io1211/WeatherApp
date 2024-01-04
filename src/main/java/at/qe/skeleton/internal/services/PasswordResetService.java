package at.qe.skeleton.internal.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@Scope("application")
public class PasswordResetService {

    @Autowired
    private EmailService emailService;

    @Autowired
    public TokenService tokenService;

    public void sendPasswordResetEmailAndToken(String email, String token) {
        emailService.sendEmail(email, "Reset your password", "Please click on the following link to reset your password: http://localhost:8080/resetPassword.xhtml" + "\nYour token: " + token + "\n\nIf you did not request a password reset, please ignore this email.");
    }

    public void resetPassword(String email, String newPassword) {
        System.out.println("Resetting password for " + email + " to " + newPassword);
    }
}