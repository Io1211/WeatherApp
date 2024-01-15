package at.qe.skeleton.internal.repositories;

import at.qe.skeleton.internal.model.Favorite;

public interface FavoriteRepository extends AbstractRepository<Favorite, String> {
  Favorite findFavoriteByLocation_CityAndUser_Username(String city, String username);
}
