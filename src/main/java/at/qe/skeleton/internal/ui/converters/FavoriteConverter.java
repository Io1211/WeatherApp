package at.qe.skeleton.internal.ui.converters;

import at.qe.skeleton.internal.model.Favorite;
import at.qe.skeleton.internal.services.FavoriteService;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
public class FavoriteConverter implements Converter<Favorite> {

  @Autowired private FavoriteService favoriteService;

  @Override
  public Favorite getAsObject(FacesContext facesContext, UIComponent uiComponent, String value) {
    if (value != null && !value.trim().isEmpty()) {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      return favoriteService.loadFavorite(value, auth.getName());
    } else {
      return null;
    }
  }

  @Override
  public String getAsString(FacesContext facesContext, UIComponent uiComponent, Favorite favorite) {
    if (favorite != null) {
      return favorite.getLocation().getCity();
    } else {
      return null;
    }
  }
}
