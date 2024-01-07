package at.qe.skeleton.tests;

import at.qe.skeleton.internal.services.EmailService;
import at.qe.skeleton.internal.services.PasswordResetService;
import at.qe.skeleton.internal.services.TokenService;
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

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private PasswordResetBean passwordResetBean;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testResetPassword() {
        passwordResetBean.setEmail("test@example.com");
        passwordResetBean.setInsertedToken("12345");
        passwordResetBean.setToken("12345");
        passwordResetBean.setNewPassword("12345");
        passwordResetBean.setNewPasswordRepeat("12345");

        when(passwordResetBean.validatePasswordResetToken()).thenReturn(true);

        String result = passwordResetBean.resetPassword();

        verify(passwordResetService, times(1)).resetPassword(anyString(), anyString());
        assertEquals("successPage", result);

    }
}
