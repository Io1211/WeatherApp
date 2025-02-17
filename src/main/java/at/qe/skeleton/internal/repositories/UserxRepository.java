package at.qe.skeleton.internal.repositories;

import at.qe.skeleton.internal.model.Userx;
import at.qe.skeleton.internal.model.UserxRole;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for managing {@link Userx} entities.
 *
 * <p>This class is part of the skeleton project provided for students of the course "Software
 * Architecture" offered by Innsbruck University.
 */
public interface UserxRepository extends AbstractRepository<Userx, String> {

  Userx findFirstByUsername(String username);

  List<Userx> findByUsernameContaining(String username);

  @Query("SELECT u FROM Userx u WHERE CONCAT(u.firstName, ' ', u.lastName) = :wholeName")
  List<Userx> findByWholeNameConcat(@Param("wholeName") String wholeName);

  @Query("SELECT u FROM Userx u WHERE :role MEMBER OF u.roles")
  List<Userx> findByRole(@Param("role") UserxRole role);

  Userx findFirstByEmail(String email);
}
