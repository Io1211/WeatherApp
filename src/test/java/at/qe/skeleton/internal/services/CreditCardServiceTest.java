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

  // todo: add tests
  //  for if (!validateDate(creditCard.getExpirationDate())) and
  // (creditCardRepository.findByUserId_Username(creditCard.getUser().getUsername()) != null)
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
}
