package at.qe.skeleton.tests;

import at.qe.skeleton.internal.services.EmailService;
import at.qe.skeleton.internal.services.PasswordResetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

class PasswordResetServiceTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendPasswordResetEmail() {
        String email = "test@example.com";
        passwordResetService.sendPasswordResetEmail(email);
        verify(emailService).sendEmail(
                eq(email),
                eq("Reset your password"),
                contains("Please click on the following link to reset your password: http://localhost:8080/resetPassword.xhtml")
        );
    }

    @Test
    void testGeneratePasswordResetToken() {
        String token = passwordResetService.generatePasswordResetToken();
        assertNotNull(token);
    }

    @Test
    void testValidatePasswordResetToken() {
        String token = "1234";
        assertTrue(passwordResetService.validatePasswordResetToken(token, token));
        assertFalse(passwordResetService.validatePasswordResetToken(token, "0000"));
    }
}
