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
        passwordResetService.sendPasswordResetEmailAndToken(email, "1234");
        verify(emailService).sendEmail(
                eq(email),
                eq("Reset your password"),
                contains("Your token")
        );
    }

}
