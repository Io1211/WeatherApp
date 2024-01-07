package at.qe.skeleton.tests;

import at.qe.skeleton.internal.model.CardType;
import at.qe.skeleton.internal.model.Userx;
import at.qe.skeleton.internal.services.CreditCardService;
import at.qe.skeleton.internal.services.UserxService;
import at.qe.skeleton.internal.ui.beans.CreditCardBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Some very basic tests for UserBean.
 */

public class CreditCardBeanTest {

    @Mock
    private CreditCardService creditcardservice;

    @Mock
    private CardType VISA;

    @InjectMocks
    private CreditCardBean creditCardBean;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSetPassword() {
        String testNumber = "123456";
        creditCardBean.getCreditCard().setCardnumber(testNumber);
        assertEquals(testNumber, creditCardBean.getCreditCard().getCardnumber(), "Cardnumber should be set correctly");
    }

    @Test
    public void testSetExpirationDate() {
        Date testExpirationDate = new Date();
        creditCardBean.getCreditCard().setExpirationDate(testExpirationDate);
        assertEquals(testExpirationDate, creditCardBean.getCreditCard().getExpirationDate(), "ExpirationDate should be set correctly");
    }

    @Test
    public void testSetCardType() {
        CardType testCardType = VISA;
        creditCardBean.getCreditCard().setCardType(testCardType);
        assertEquals(testCardType, creditCardBean.getCreditCard().getCardType(), "CardType should be set correctly");
    }

    @Test
    public void testSetOwner() {
        creditCardBean.getCreditCard().setOwner("testUsername");
        assertEquals("testUsername", creditCardBean.getCreditCard().getOwner(), "Owner should be set correctly");
    }

    @Test
    public void testSave() {

    }
}
