package at.qe.skeleton.internal.ui.beans;

import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import at.qe.skeleton.internal.model.Favorite;
import at.qe.skeleton.internal.model.FavoriteDataConfig;
import at.qe.skeleton.internal.model.Userx;
import at.qe.skeleton.internal.services.CurrentAndForecastAnswerService;
import at.qe.skeleton.internal.services.FavoriteService;
import at.qe.skeleton.internal.services.UserxService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Bean for creating overview view for favorite locations for logged in user. */
@Component
@Scope("view")
public class FavoriteOverviewBean {

  @Autowired private UserxService userxService;

  @Autowired private FavoriteService favoriteService;

  @Autowired private CurrentAndForecastAnswerService currentAndForecastAnswerService;
  private List<Favorite> favorites;
  private FavoriteDataConfig favoriteDataConfig;
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

      favoriteDataConfig = user.getFavoriteDataConfig();

    } catch (Exception e) {
      this.addMessage(
          "An error occurred while loading the favorites or favorite-configurations",
          FacesMessage.SEVERITY_ERROR);
    }
  }

  public CurrentAndForecastAnswerDTO getCurrentWeather(Favorite favorite) {
    byte[] serializedWeatherData = favorite.getLocation().getWeather().getWeatherData();
    return currentAndForecastAnswerService.deserializeDTO(serializedWeatherData);
  }

  public String removeFavorite(Favorite favorite) {
    this.favoriteService.toggleFavorite(this.user, favorite.getLocation());
    return "/secured/favoritesOverview.xhtml?faces-redirect=true";
  }

  private void addMessage(String summary, FacesMessage.Severity severity) {
    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, null));
  }

  public FavoriteDataConfig getFavoriteDataConfig() {
    return favoriteDataConfig;
  }

  public List<Favorite> getFavorites() {
    if (favorites.isEmpty()) {
      this.addMessage("You haven´t added any favorites yet.", FacesMessage.SEVERITY_INFO);
    }
    return favorites;
  }
}
