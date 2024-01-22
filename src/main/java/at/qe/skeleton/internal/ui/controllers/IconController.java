package at.qe.skeleton.internal.ui.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("view")
public class IconController {
  private static final Logger LOGGER = LoggerFactory.getLogger(IconController.class);
  private static final String URI = "https://openweathermap.org/img/wn/";

  public String getIcon(String iconId) {

    String uriResult = URI.concat(iconId).concat("@2x.png");
    LOGGER.info("retrieving icon from: {}", uriResult);
    return uriResult;
  }
}
