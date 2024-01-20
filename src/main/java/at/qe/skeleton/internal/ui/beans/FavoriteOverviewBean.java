package at.qe.skeleton.internal.ui.beans;

import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import at.qe.skeleton.external.model.shared.WeatherDTO;
import at.qe.skeleton.internal.model.CurrentAndForecastAnswer;
import at.qe.skeleton.internal.model.Favorite;
import at.qe.skeleton.internal.model.FavoriteDataConfig;
import at.qe.skeleton.internal.model.Userx;
import at.qe.skeleton.internal.services.CurrentAndForecastAnswerService;
import at.qe.skeleton.internal.services.FavoriteService;
import at.qe.skeleton.internal.services.LocationService;
import at.qe.skeleton.internal.services.UserxService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

/** Bean for creating overview view for favorite locations for logged in user. */
@Component
@Scope("view")
public class FavoriteOverviewBean {

  @Autowired private UserxService userxService;

  @Autowired private LocationService locationService;

  @Autowired private CurrentAndForecastAnswerService currentAndForecastAnswerService;
  private List<Favorite> favorites;
  private FavoriteDataConfig favoriteDataConfig;
  private List<CurrentAndForecastAnswerDTO> currentAndForecastAnswerDTOS;

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

      currentAndForecastAnswerDTOS =
          favorites.stream()
              .map(
                  favorite ->
                      currentAndForecastAnswerService.deserializeDTO(
                          favorite.getLocation().getWeather().getWeatherData()))
              .toList();

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

  private void addMessage(String summary, FacesMessage.Severity severity) {
    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, null));
  }

  public FavoriteDataConfig getFavoriteDataConfig() {
    return favoriteDataConfig;
  }

  public List<Favorite> getFavorites() {
    return favorites;
  }

  public List<CurrentAndForecastAnswerDTO> getCurrentAndForecastAnswerDTOS() {
    return currentAndForecastAnswerDTOS;
  }
}
