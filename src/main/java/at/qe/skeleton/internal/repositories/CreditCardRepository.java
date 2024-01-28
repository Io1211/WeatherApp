package at.qe.skeleton.internal.repositories;

import at.qe.skeleton.internal.model.CreditCard;
import at.qe.skeleton.internal.model.Userx;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Repository for managing {@link CreditCard} entities. */
public interface CreditCardRepository extends AbstractRepository<CreditCard, String> {

  @Query("SELECT c FROM CreditCard c WHERE c.number = :number")
  CreditCard findCreditCardByNumber(@Param("number") String number);

  @Query("SELECT c FROM CreditCard c WHERE c.userId = :userId")
  CreditCard findCreditCardByUser(@Param("userId") Userx userId);

  CreditCard findByUserId_Username(String username);

  void deleteAll();
}
