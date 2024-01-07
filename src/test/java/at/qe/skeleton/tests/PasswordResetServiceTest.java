package at.qe.skeleton.tests;

import at.qe.skeleton.internal.model.Userx;
import at.qe.skeleton.internal.services.EmailService;
import at.qe.skeleton.internal.services.PasswordResetService;
import at.qe.skeleton.internal.services.UserxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PasswordResetServiceTest {

    @Mock
    private EmailService emailService;

    @Mock
    private UserxService userxService;

    @InjectMocks
    private PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendPasswordResetEmail() {
        String email = "test@example.com";
        String token = "1234";
        Userx mockUser = new Userx();
        when(userxService.loadUserByEmail(email)).thenReturn(mockUser);
        passwordResetService.sendPasswordResetEmailAndToken(email, token);
        verify(emailService).sendEmail(
                eq(email),
                eq("Reset your password"),
                contains("Your token")
        );
    }

    @Test
    void testSendPasswordResetEmailWithInvalidEmail() {
        String email = "wrong@example.com";
        String token = "1234";
        when(userxService.loadUserByEmail(email)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> {
            passwordResetService.sendPasswordResetEmailAndToken(email, token);
        });
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }
}
