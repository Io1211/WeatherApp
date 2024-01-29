package at.qe.skeleton.internal.services;

import at.qe.skeleton.internal.model.CardType;
import at.qe.skeleton.internal.model.CreditCard;
import at.qe.skeleton.internal.model.Userx;
import at.qe.skeleton.internal.repositories.CreditCardRepository;
import org.junit.jupiter.api.Assertions;
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
    Assertions.assertNotNull(
        creditCardRepository.findCreditCardByUser(user2),
        "user2 should have stored one credit card");

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
    Assertions.assertNull(
        creditCardRepository.findCreditCardByUser(user2),
        "user2 should not own any creditCards now");

    // actual testing
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> creditCardService.saveCreditCard(creditCard),
        "Saving Credit Cards with invalid expiration Date should not be possible.");

    resetDB();
    Assertions.assertNull(
        creditCardRepository.findCreditCardByUser(user2),
        "user2 should not own any creditCards now");

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
    Assertions.assertNull(
        creditCardRepository.findCreditCardByUser(user2),
        "user2 should not own any creditCards now");

    // third invalid exp date
    creditCard.setExpirationDate(invalidExpDate3);
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> creditCardService.saveCreditCard(creditCard),
        "Saving Credit Cards with invalid expiration Date should not be possible");
  }

  @Test
  @DirtiesContext
  @WithMockUser(
      username = "admin",
      authorities = {"ADMIN"})
  void safeAlreadySavedCreditCard() {
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

    // save credit card twice
    creditCardService.saveCreditCard(creditCard);
    Assertions.assertNotNull(creditCardRepository.findCreditCardByUser(user2));
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> creditCardService.saveCreditCard(creditCard),
        "saving the same credit card twice should throw exception");

    // try saving another credit card.
    // first prepare data for other credit card
    Userx adminUser = userxService.loadUser("admin");
    String otherExpirationDate = "05/2040";
    String otherCardNumber = "1233 1233 1223 1222";
    CardType otherCardType = CardType.MASTERCARD;

    // now setup other creditcard
    CreditCard otherCreditCard = new CreditCard();
    otherCreditCard.setUser(adminUser);
    otherCreditCard.setCardType(otherCardType);
    otherCreditCard.setExpirationDate(otherExpirationDate);
    otherCreditCard.setCardnumber(otherCardNumber);

    Assertions.assertDoesNotThrow(() -> creditCardService.saveCreditCard(otherCreditCard));
    Assertions.assertNotNull(
        creditCardRepository.findCreditCardByUser(user2), "user2 should have stored a creditCard");
    Assertions.assertNotNull(
        creditCardRepository.findCreditCardByUser(adminUser),
        "adminUser should have stored a creditCard");
  }

  @Test
  @DirtiesContext
  public void safeCreditCardWithMissingUser() {
    // creditCard for testing
    this.creditCard = new CreditCard();
    // crediditCard testData
    CardType amex = CardType.AMERICANEXPRESS;
    String cardNumber = "ABCD EFGH 1234 5678";
    String expirationDateValid = "12/2030";
    // setting up credit card without user
    creditCard.setCardType(amex);
    creditCard.setCardnumber(cardNumber);
    creditCard.setExpirationDate(expirationDateValid);

    Assertions.assertThrows(
        Exception.class,
        () -> creditCardService.saveCreditCard(creditCard),
        "saving a credit card without user should throw exception");
  }

  @Test
  @DirtiesContext
  @WithMockUser(username = "user2", authorities = "REGISTERED_USER")
  void deleteCreditCard() {
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
    Assertions.assertNull(creditCardRepository.findCreditCardByUser(user2));

    // adding credit Card
    creditCardService.saveCreditCard(creditCard);

    // db should contain exactly one creditCard now
    Assertions.assertNotNull(creditCardRepository.findCreditCardByUser(user2));

    // deleting the creditcard should result in zero creditcards in db
    creditCardService.deleteCreditCard(creditCard);
    Assertions.assertNull(
        creditCardRepository.findCreditCardByUser(user2),
        "user2 should not own any creditCards now");
  }

  @Test
  @DirtiesContext
  @WithMockUser(username = "user2", authorities = "REGISTERED_USER")
  void deleteCreditCardFromUser() {
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

    // adding credit Card
    creditCardService.saveCreditCard(creditCard);

    // db should contain exactly one creditCard now
    Assertions.assertNotNull(
        creditCardRepository.findCreditCardByUser(user2), "user2 should have a creditCard stored");

    // deleting the creditcard should result in zero creditcards in db
    creditCardService.deleteCreditCardFromUser("user2");

    Assertions.assertNull(
        creditCardRepository.findCreditCardByUser(user2),
        "user2 should not own any creditCards now");
  }

  @Test
  @DirtiesContext
  @WithMockUser(username = "user2", authorities = "REGISTERED_USER")
  void loadCreditCardByUsername() {
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

    // adding credit Card
    creditCardService.saveCreditCard(creditCard);

    CreditCard creditCardCopy = creditCardService.loadCreditCardByUsername("user2");

    Assertions.assertNotNull(creditCardCopy);
    Assertions.assertEquals(user2, creditCardCopy.getUser());
    Assertions.assertEquals(expirationDateValid, creditCardCopy.getExpirationDate());
    Assertions.assertEquals(creditCard.getId(), creditCardCopy.getId());
  }

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
}
