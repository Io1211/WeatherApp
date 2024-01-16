package at.qe.skeleton.tests;

import at.qe.skeleton.internal.repositories.CreditCardRepository;
import at.qe.skeleton.internal.services.CreditCardService;
import static org.junit.jupiter.api.Assertions.assertEquals;

import at.qe.skeleton.internal.services.UserxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/**
 * Test class for the CreditCardService class.{@link CreditCardService}
 */
public class CreditCardServiceTest {

    @InjectMocks
    private CreditCardService creditCardService;

    @Mock private CreditCardRepository creditCardRepository;

    @BeforeEach
    public void setUp() {

    }

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






}
