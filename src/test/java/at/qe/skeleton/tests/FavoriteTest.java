package at.qe.skeleton.tests;

import at.qe.skeleton.internal.model.Favorite;
import at.qe.skeleton.internal.model.Location;
import at.qe.skeleton.internal.model.LocationId;
import at.qe.skeleton.internal.model.Userx;
import at.qe.skeleton.internal.repositories.FavoriteRepository;
import at.qe.skeleton.internal.repositories.LocationRepository;
import at.qe.skeleton.internal.services.FavoriteService;
import at.qe.skeleton.internal.services.UserxService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

@SpringBootTest
@WebAppConfiguration
class FavoriteTest {
  @Autowired private UserxService userxService;
  @Autowired private FavoriteService favoriteService;
  @Autowired private LocationRepository locationRepository;

  private static Location location1;

  @BeforeAll
  static void setup() {
    LocationId locationId1 = new LocationId(1, 2);
    location1 = new Location();
    ReflectionTestUtils.setField(location1, "locationId", locationId1);
    location1.setCity("Innsbruck");
  }

  @DirtiesContext
  @Test
  @WithMockUser(
      username = "user1",
      authorities = {"REGISTERED_USER"})
  void testTogglingFavorite() {
    String username = "user1";
    Userx user = userxService.loadUser(username);
    Assertions.assertNotNull(user, "User could not be loaded from test data source");
    Assertions.assertEquals(0, user.getFavorites().size(), "User should have no favorites");

    // location needs to be in database for toggleFavorite to work
    locationRepository.save(location1);
    favoriteService.toggleFavorite(user, location1);

    Assertions.assertEquals(1, user.getFavorites().size(), "User should have one favorite");
    Assertions.assertNotNull(
        favoriteService.loadFavorite(location1.getCity(), "user1"),
        "Favorite should exist after toggle");

    favoriteService.toggleFavorite(user, location1);
    Assertions.assertEquals(
        0, user.getFavorites().size(), "User should have no favorites after toggle");
    Assertions.assertFalse(favoriteService.isFavorite(user, location1));
  }

  @DirtiesContext
  @Test
  @WithMockUser(
      username = "user1",
      authorities = {"REGISTERED_USER"})
  void testIsFavorite() {
    String username = "user1";
    Userx user = userxService.loadUser(username);
    Assertions.assertNotNull(user, "User could not be loaded from test data source");
    Assertions.assertEquals(0, user.getFavorites().size(), "User should have no favorites");

    // location needs to be in database for toggleFavorite to work
    locationRepository.save(location1);
    favoriteService.toggleFavorite(user, location1);
    Assertions.assertTrue(
        favoriteService.isFavorite(user, location1), "IsFavorite should return true after toggle");
  }

  @DirtiesContext
  @Test
  @WithMockUser(
      username = "admin",
      authorities = {"ADMIN"})
  void testCascadeDelete() {
    String username = "user1";
    Userx user = userxService.loadUser(username);
    Assertions.assertNotNull(user, "User could not be loaded from test data source");
    Assertions.assertEquals(0, user.getFavorites().size(), "User should have no favorites");

    // location needs to be in database for toggleFavorite to work
    locationRepository.save(location1);
    favoriteService.toggleFavorite(user, location1);
    Favorite favorite = favoriteService.loadFavorite("Innsbruck", "user1");
    Assertions.assertNotNull(favorite, "Favorite should exist after toggle");

    userxService.deleteUser(user);
    Assertions.assertNull(
        favoriteService.loadFavorite("Innsbruck", "user1"),
        "Favorite should be deleted when user is deleted");
  }
}
