package at.qe.skeleton.internal.ui.beans;

import at.qe.skeleton.internal.model.Favorite;
import at.qe.skeleton.internal.model.Userx;
import at.qe.skeleton.internal.services.UserxService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
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
    try {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      this.user = userxService.loadUser(auth.getName());

      favorites =
          user.getFavorites().stream()
              .sorted(Comparator.comparingInt(Favorite::getPriority))
              .toList();
    } catch (Exception e) {
      this.addMessage("An error occurred while loading the favorites", FacesMessage.SEVERITY_ERROR);
    }
  }

  public void save() {
    try {
      for (int i = 0; i < favorites.size(); i++) {
        favorites.get(i).setPriority(i);
      }
      this.user.setFavorites(favorites);
      userxService.saveUser(this.user);
    } catch (Exception e) {
      this.addMessage("An error occurred while saving the changes", FacesMessage.SEVERITY_ERROR);
      return;
    }

    this.addMessage("Changes saved", FacesMessage.SEVERITY_INFO);
  }

  private void addMessage(String summary, FacesMessage.Severity severity) {
    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, null));
  }

  public List<Favorite> getFavorites() {
    return favorites;
  }

  public void setFavorites(List<Favorite> favorites) {
    this.favorites = favorites;
  }
}
