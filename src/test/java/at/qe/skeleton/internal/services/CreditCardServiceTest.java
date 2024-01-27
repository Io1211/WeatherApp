package at.qe.skeleton.internal.services;

import at.qe.skeleton.internal.model.CardType;
import at.qe.skeleton.internal.model.CreditCard;
import at.qe.skeleton.internal.model.Userx;
import at.qe.skeleton.internal.repositories.CreditCardRepository;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@WebAppConfiguration
class CreditCardServiceTest {

  @Autowired private CreditCardService creditCardService;
  @Autowired private UserxService userxService;
  @Autowired private CreditCardRepository creditCardRepository;

  CreditCard creditCard;
  Userx user2;

  private void resetDB() {
    creditCardRepository.findAll().forEach(c -> creditCardRepository.delete(c));
  }

  @DirtiesContext
  @Test
  @WithMockUser(
      username = "admin",
      authorities = {"ADMIN"})
  void saveAndLoadCreditCardValidDates() {
    // creditCard for testing
    this.creditCard = new CreditCard();
    // crediditCard testData
    user2 = userxService.loadUser("user2");
    CardType amex = CardType.AMERICANEXPRESS;
    String cardNumber = "ABCD EFGH 1234 5678";
    String expirationDateValid = "12/2030";

    // setting up credit card
    creditCard.setUser(user2);
    creditCard.setCardType(amex);
    creditCard.setCardnumber(cardNumber);
    creditCard.setExpirationDate(expirationDateValid);

    // saving and then loading Credit Card
    creditCardService.saveCreditCard(creditCard);
    CreditCard loadedCreditCard = creditCardService.loadCreditCard(creditCard.getUser());

    // Actual Tests wether Credit Card was correctly saved and retourned.
    Assertions.assertEquals(1, creditCardRepository.findAll().size());

    Assertions.assertEquals(creditCard.getId(), loadedCreditCard.getId());
    Assertions.assertEquals(user2, loadedCreditCard.getUser());
    Assertions.assertEquals(amex, loadedCreditCard.getCardType());
    Assertions.assertEquals(cardNumber, loadedCreditCard.getCardnumber());
    Assertions.assertEquals(expirationDateValid, loadedCreditCard.getExpirationDate());
  }

  @DirtiesContext
  @Test
  @WithMockUser(
      username = "admin",
      authorities = {"ADMIN"})
  void safeIllegalExpirationDateCreditcard() {
    // creditCard for testing
    this.creditCard = new CreditCard();

    // crediditCard testData
    user2 = userxService.loadUser("user2");
    CardType amex = CardType.AMERICANEXPRESS;
    String cardNumber = "ABCD EFGH 1234 5678";
    String invalidExpDate1 = "13/2024";
    String invalidExpDate2 = "100/2024";
    String invalidExpDate3 = "04/2000";

    // setting up credit card
    creditCard.setUser(user2);
    creditCard.setCardType(amex);
    creditCard.setCardnumber(cardNumber);
    creditCard.setExpirationDate(invalidExpDate1);

    // making sure repository is empty
    Assertions.assertTrue(creditCardRepository.findAll().isEmpty());

    // actual testing
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> creditCardService.saveCreditCard(creditCard),
        "Saving Credit Cards with invalid expiration Date should not be possible.");
    resetDB();
    Assertions.assertTrue(
        creditCardRepository.findAll().isEmpty(), "resetting the db did not work.");

    // now with different invalid exp date
    creditCard.setExpirationDate(invalidExpDate2);
    Assertions.assertEquals(
        invalidExpDate2,
        creditCard.getExpirationDate(),
        "setting different Exp Date did not work.");
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> creditCardService.saveCreditCard(creditCard),
        "Saving Credit Cards with invalid expiration Date should not be possible.");
    resetDB();
    Assertions.assertTrue(
        creditCardRepository.findAll().isEmpty(), "resetting the db did not work.");

    // third invalid exp date
    creditCard.setExpirationDate(invalidExpDate3);
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> creditCardService.saveCreditCard(creditCard),
        "Saving Credit Cards with invalid expiration Date should not be possible");
  }

  @Test
  void deleteCreditCard() {}

  @Test
  void deleteCreditCardFromUser() {}

  @Test
  void loadCreditCardByUsername() {}

  @Test
  public void testValidateDatetrue() {
    String date = "12/2026";
    boolean result = CreditCardService.validateDate(date);
    assertTrue(result);
  }

  @Test
  public void testValidateDatefalse() {
    String date = "12/2020";
    boolean result = CreditCardService.validateDate(date);
    assertFalse(result);
  }

  // todo: check if frontend works as expected.
}
