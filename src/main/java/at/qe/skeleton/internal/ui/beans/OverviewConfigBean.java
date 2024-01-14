package at.qe.skeleton.internal.ui.beans;

import at.qe.skeleton.internal.model.Favorite;
import at.qe.skeleton.internal.model.Userx;
import at.qe.skeleton.internal.services.UserxService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * Bean for configuring what fields are shown in the overview page and the order of the favorites.
 */
@Component
@Scope("view")
public class OverviewConfigBean {

  @Autowired private UserxService userxService;
  private List<Favorite> favorites;

  private Userx user;

  @PostConstruct
  private void init() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    this.user = userxService.loadUser(auth.getName());

    favorites =
        user.getFavorites().stream()
            .sorted(Comparator.comparingInt(Favorite::getPriority))
            .toList();
  }

  public void save() {
    for (int i = 0; i < favorites.size(); i++) {
      // todo: fix this by creating a converter
      //      System.out.println(favorites.get(i).getPriority());
      favorites.get(i).setPriority(i);
    }
    this.user.setFavorites(favorites);
    userxService.saveUser(this.user);
  }

  public List<Favorite> getFavorites() {
    return favorites;
  }

  public void setFavorites(List<Favorite> favorites) {
    this.favorites = favorites;
  }
}
