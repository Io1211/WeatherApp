package at.qe.skeleton.tests;

import at.qe.skeleton.internal.model.CreditCard;
import at.qe.skeleton.internal.model.Userx;
import at.qe.skeleton.internal.repositories.CreditCardRepository;
import at.qe.skeleton.internal.services.CreditCardService;
import at.qe.skeleton.internal.ui.beans.CreditCardBean;
import at.qe.skeleton.internal.ui.beans.SessionInfoBean;
import jakarta.faces.context.FacesContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.assertNotNull;

/** Some very basic tests for UserBean. {@link CreditCardBean} */
public class CreditCardBeanTest {

    @InjectMocks private CreditCardBean creditCardBean;

    @Mock private CreditCardService creditCardService;

    @Mock private CreditCardRepository creditCardRepository;

    @Mock private SessionInfoBean sessionInfoBean;

    @Mock private FacesContext facesContext;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(sessionInfoBean.getCurrentUser()).thenReturn(new Userx());
    }

    @Test
    public void testInit() {
        creditCardBean.init();
        assertNotNull("CardTypes should not be null", creditCardBean.getCardTypes());
        assertNotNull("CreditCard should not be null", creditCardBean.getCreditCard());
    }

    // test for validate date
    @Test
    public void testValidateDatetrue() {
        String date = "12/2026";
        boolean result = CreditCardService.validateDate(date);
        assertEquals(true, result);
    }

    @Test
    public void testValidateDatefalse() {
        String date = "12/2020";
        boolean result = CreditCardService.validateDate(date);
        assertEquals(false, result);
    }

    @Test
    public void testSaveCreditCard() {
        try (MockedStatic<FacesContext> mockedFacesContext = Mockito.mockStatic(FacesContext.class)) {
            FacesContext facesContextMock = Mockito.mock(FacesContext.class);
            mockedFacesContext.when(FacesContext::getCurrentInstance).thenReturn(facesContextMock);

            Userx mockedUser = mock(Userx.class);
            when(sessionInfoBean.getCurrentUser()).thenReturn(mockedUser);
            CreditCard testCreditCard = new CreditCard();
            testCreditCard.setUserId(mockedUser);
            when(sessionInfoBean.getCurrentUser()).thenReturn(mockedUser);

            creditCardBean.setCreditCard(testCreditCard);

            String result = creditCardBean.saveCreditCard();

            verify(creditCardService, times(1)).saveCreditCard(testCreditCard);

            assertEquals("credit_card_details.xhtml", result);
        }
    }

    @Test
    public void updateCreditCard() {
        try (MockedStatic<FacesContext> mockedFacesContext = Mockito.mockStatic(FacesContext.class)) {
            FacesContext facesContextMock = Mockito.mock(FacesContext.class);
            mockedFacesContext.when(FacesContext::getCurrentInstance).thenReturn(facesContextMock);

            Userx mockedUser = mock(Userx.class);
            when(sessionInfoBean.getCurrentUser()).thenReturn(mockedUser);
            CreditCard testCreditCard = new CreditCard();
            testCreditCard.setUserId(mockedUser);
            when(sessionInfoBean.getCurrentUser()).thenReturn(mockedUser);

            creditCardBean.setCreditCard(testCreditCard);

            String result = creditCardBean.updateCreditCard();

            verify(creditCardService, times(1)).saveCreditCard(testCreditCard);

            assertEquals("credit_card_details.xhtml", result);
        }
    }




}
