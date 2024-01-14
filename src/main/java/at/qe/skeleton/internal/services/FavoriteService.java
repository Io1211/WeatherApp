package at.qe.skeleton.internal.services;

import at.qe.skeleton.internal.model.Favorite;
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

  public Favorite loadFavorite(String username, String city) {
    return this.favoriteRepository.findFavoriteByLocation_CityAndUser_Username(city, username);
  }

  @PreAuthorize("hasAuthority('ADMIN') or principal.username eq #user.username")
  public void toggleFavorite(Userx user, Favorite favorite) {
    var existingFavorite =
        user.getFavorites().stream()
            .filter(x -> Objects.equals(x.getLocation().getId(), favorite.getLocation().getId()))
            .findAny();

    if (existingFavorite.isPresent()) {
      user.getFavorites().remove(existingFavorite.get());
    } else {
      // needed because of the bidirectional relationship
      favorite.setUser(user);
      user.getFavorites().add(favorite);
    }
    userRepository.save(user);
  }
}
