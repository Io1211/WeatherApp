package at.qe.skeleton.internal.repositories;

import at.qe.skeleton.internal.model.CreditCard;
import at.qe.skeleton.internal.model.LocationId;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CreditCardRepository extends AbstractRepository<CreditCard, String> {

    @Query("SELECT c FROM CreditCard c WHERE c.number = :number")
    CreditCard findCreditCardByNumber(@Param("number") String number);

    @Query("SELECT c FROM CreditCard c WHERE c.userId.id = :userId")
    CreditCard findCreditCardByUserId(@Param("userId") Long userId);

    CreditCard findByUserId_Username(String username);
}
