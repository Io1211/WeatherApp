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

    public void sendPasswordResetEmail(String email) {
        emailService.sendEmail(email, "Reset your password", "Please click on the following link to reset your password: http://localhost:8080/resetPassword.xhtml" + "\nYour token: " + generatePasswordResetToken() + "\n\nIf you did not request a password reset, please ignore this email.");
    }

    public String generatePasswordResetToken() {
        Random r = new Random();
        int fourDigit = 1000 + r.nextInt(10000);
        System.out.println(fourDigit);
        return Integer.toString(fourDigit);
    }

    public boolean validatePasswordResetToken(String token, String insertedToken) {
        return token.equals(insertedToken);
    }

    public void resetPassword(String email, String newPassword) {
        System.out.println("Resetting password for " + email + " to " + newPassword);
    }
}