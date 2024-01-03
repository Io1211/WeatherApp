package at.qe.skeleton.tests;

import at.qe.skeleton.internal.services.EmailService;
import at.qe.skeleton.internal.services.PasswordResetService;
import at.qe.skeleton.internal.ui.beans.PasswordResetBean;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class PasswordResetBeanTest {
    @Mock
    private EmailService emailService;

    @Mock
    private PasswordResetService passwordResetService;

    @InjectMocks
    private PasswordResetBean passwordResetBean;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSendPasswordResetEmail() {
        String email = "test@example.com";
        String token = "12345";

        passwordResetBean.setEmail(email);
        when(passwordResetService.generatePasswordResetToken()).thenReturn(token);

        passwordResetBean.sendPasswordResetEmail();

        verify(passwordResetService, times(1)).generatePasswordResetToken();
        verify(emailService, times(1)).sendEmail(eq(email), anyString(), anyString());

    }

    @Test
    public void testResetPassword() {
        passwordResetBean.setEmail("test@example.com");
        passwordResetBean.setInsertedToken("12345");
        passwordResetBean.setToken("12345");
        passwordResetBean.setNewPassword("12345");
        passwordResetBean.setNewPasswordRepeat("12345");

        when(passwordResetService.validatePasswordResetToken("12345", "12345")).thenReturn(true);

        String result = passwordResetBean.resetPassword();

        verify(passwordResetService, times(1)).resetPassword(anyString(), anyString());
        assertEquals("successPage", result);

    }
}
