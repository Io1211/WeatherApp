package at.qe.skeleton.tests;

import at.qe.skeleton.internal.model.Favorite;
import at.qe.skeleton.internal.model.Userx;
import at.qe.skeleton.internal.model.UserxRole;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Tests to ensure that each entity's implementation of equals conforms to the contract. See
 * {@linkplain http://www.jqno.nl/equalsverifier/} for more information.
 *
 * <p>This class is part of the skeleton project provided for students of the courses "Software
 * Architecture" and "Software Engineering" offered by the University of Innsbruck.
 */
public class EqualsImplementationTest {

  @Test
  public void testUserEqualsContract() {
    Userx user1 = new Userx();
    user1.setUsername("user1");
    Userx user2 = new Userx();
    user2.setUsername("user2");
    Favorite favorite1 = new Favorite();
    ReflectionTestUtils.setField(favorite1, "id", 0L);
    Favorite favorite2 = new Favorite();
    ReflectionTestUtils.setField(favorite2, "id", 1L);
    EqualsVerifier.forClass(Userx.class)
        .withPrefabValues(Userx.class, user1, user2)
        .withPrefabValues(Favorite.class, favorite1, favorite2)
        .suppress(Warning.STRICT_INHERITANCE, Warning.ALL_FIELDS_SHOULD_BE_USED)
        .verify();
  }

  @Test
  public void testUserRoleEqualsContract() {
    EqualsVerifier.forClass(UserxRole.class).verify();
  }
}
