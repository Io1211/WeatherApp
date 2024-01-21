package at.qe.skeleton.internal.services;

import at.qe.skeleton.internal.model.Favorite;
import at.qe.skeleton.internal.model.Location;
import at.qe.skeleton.internal.model.Userx;
import at.qe.skeleton.internal.repositories.FavoriteRepository;
import at.qe.skeleton.internal.repositories.UserxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Scope("application")
public class FavoriteService {

  @Autowired private FavoriteRepository favoriteRepository;

  @Autowired private UserxRepository userRepository;

  /**
   * Loads a single favorite identified by its username and city.
   *
   * @param city the city of the location saved by the favorite
   * @param username the username of the user who has the favorite
   */
  public Favorite loadFavorite(String city, String username) {
    return this.favoriteRepository.findFavoriteByLocation_CityAndUser_Username(city, username);
  }

  /**
   * Enables or disables the favorite for the given user depending on the current state.
   *
   * @param user the user who should get/remove the favorite
   * @param location the location to enable/disable as favorite
   */
  @PreAuthorize("hasAuthority('ADMIN') or principal.username eq #user.username")
  public void toggleFavorite(Userx user, Location location) {
    var existingFavorite =
        user.getFavorites().stream()
            .filter(x -> Objects.equals(x.getLocation().getId(), location.getId()))
            .findAny();

    if (existingFavorite.isPresent()) {
      user.getFavorites().remove(existingFavorite.get());
    } else {
      var favorite = new Favorite();
      favorite.setLocation(location);

      // needed because of the bidirectional relationship
      favorite.setUser(user);
      user.getFavorites().add(favorite);
    }
    userRepository.save(user);
  }

  /** Checks if the given user has the given location saved as favorite. */
  public Boolean isFavorite(Userx user, Location location) {
    return user.getFavorites().stream()
        .anyMatch(x -> Objects.equals(x.getLocation().getId(), location.getId()));
  }
}
