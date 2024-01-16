package at.qe.skeleton.internal.services;

import at.qe.skeleton.internal.model.CreditCard;
import at.qe.skeleton.internal.repositories.CreditCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Service for accessing and manipulating credit card data.
 *
 * <p>This class saves, load and deletes credit card data. It also validates the expiration date.
 */
@Component
@Scope("application")
public class CreditCardService {

  @Autowired private CreditCardRepository creditCardRepository;

  @Autowired private UserxService userxService;


  public void saveCreditCard(CreditCard creditCard) throws IllegalArgumentException {
    if (!validateDate(creditCard.getExpirationDate())) {
      throw new IllegalArgumentException("Invalid expiration date.");
    }
    if (creditCardRepository.findByUserId_Username(creditCard.getUser().getUsername()) != null) {
      throw new IllegalArgumentException(
          "Credit Card already exists. Please use the Edit button to update it.");
    }
    creditCardRepository.save(creditCard);
  }

  public void deleteCreditCard(CreditCard creditCard) {
    if(userxService.isPremium(creditCard.getUser())) {
      throw new IllegalArgumentException("You are a premium user. You cannot delete your credit card.");
    }

    creditCardRepository.delete(creditCard);
  }

  public void deleteCreditCardFromUser(String username) {
    CreditCard creditCard = creditCardRepository.findByUserId_Username(username);
    if (creditCard != null) {
      creditCardRepository.delete(creditCard);
    }
  }

  public CreditCard loadCreditCard(Long id) {
    return creditCardRepository.findCreditCardByUserId(id);
  }

  public CreditCard loadCreditCardByUsername(String username) {
    return creditCardRepository.findByUserId_Username(username);
  }

  public static boolean validateDate(String date) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");

    try {
      YearMonth yearMonth = YearMonth.parse(date, formatter);
      LocalDate parsedDate = yearMonth.atDay(1);
      LocalDate currentDate = LocalDate.now();

      return parsedDate.isAfter(currentDate);
    } catch (DateTimeParseException e) {
      return false;
    }
  }
}
