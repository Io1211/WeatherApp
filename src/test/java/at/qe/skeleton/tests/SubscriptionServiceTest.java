package at.qe.skeleton.tests;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

import at.qe.skeleton.internal.model.CreditCard;
import at.qe.skeleton.internal.model.Userx;
import at.qe.skeleton.internal.repositories.CreditCardRepository;
import at.qe.skeleton.internal.services.SubscriptionService;
import at.qe.skeleton.internal.services.UserxService;
import at.qe.skeleton.internal.services.exceptions.NoCreditCardFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SubscriptionServiceTest {

    @Mock
    private CreditCardRepository creditCardRepository;

    @Mock
    private UserxService userxService;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private Userx user;

    @BeforeEach
    void setUp() {
        user = new Userx();
        user.setUsername("testUser");
    }

    @Test
    public void activatePremiumSubscription_Successful() throws NoCreditCardFoundException {
        when(creditCardRepository.findByUserId_Username(user.getUsername())).thenReturn(new CreditCard());
        subscriptionService.activatePremiumSubscription(user);
        verify(userxService).activatePremium(user);
    }

    @Test
    public void activatePremiumSubscription_NoCreditCardFound() throws NoCreditCardFoundException {
        when(creditCardRepository.findByUserId_Username(user.getUsername())).thenReturn(null);
        assertThrows(NoCreditCardFoundException.class, () -> subscriptionService.activatePremiumSubscription(user));
        verify(userxService, never()).activatePremium(user);
    }

    @Test
    public void deactivatePremiumSubscription_Successful() {
        subscriptionService.deactivatePremiumSubscription(user);
        verify(userxService).deactivatePremium(user);
    }

}
