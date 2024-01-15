package at.qe.skeleton.internal.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.io.Serial;
import java.io.Serializable;
import org.springframework.data.domain.Persistable;

/**
 * Entity for storing which fields a users wants displayed in their favorite overview. Contains
 * booleans for all fields of CurrentWeatherDTO
 */
@Entity
public class FavoriteDataConfig implements Persistable<String>, Serializable {
  @Id @GeneratedValue private Long id;

  private Boolean showTimestamp;
  private Boolean showSunrise;
  private Boolean showSunset;
  private Boolean showTemperature = true;
  private Boolean showFeelsLikeTemperature = true;
  private Boolean showPressure;
  private Boolean showHumidity;
  private Boolean showDewPoint;
  private Boolean showClouds;
  private Boolean showUvi;
  private Boolean showVisibility;
  private Boolean showRain;
  private Boolean showSnow;
  private Boolean showWindSpeed;
  private Boolean showWindGust;
  private Boolean showWindDirection;

  /** Title is also called weather.main */
  private Boolean showTitle = true;

  private Boolean showIcon = true;

  public void setId(Long id) {
    this.id = id;
  }

  public String getId() {
    return String.valueOf(this.id);
  }

  @Override
  public boolean isNew() {
    return (null == id);
  }

  public Boolean getShowTimestamp() {
    return showTimestamp;
  }

  public void setShowTimestamp(Boolean showTimestamp) {
    this.showTimestamp = showTimestamp;
  }

  public Boolean getShowSunrise() {
    return showSunrise;
  }

  public void setShowSunrise(Boolean showSunrise) {
    this.showSunrise = showSunrise;
  }

  public Boolean getShowSunset() {
    return showSunset;
  }

  public void setShowSunset(Boolean showSunset) {
    this.showSunset = showSunset;
  }

  public Boolean getShowTemperature() {
    return showTemperature;
  }

  public void setShowTemperature(Boolean showTemperature) {
    this.showTemperature = showTemperature;
  }

  public Boolean getShowFeelsLikeTemperature() {
    return showFeelsLikeTemperature;
  }

  public void setShowFeelsLikeTemperature(Boolean showFeelsLikeTemperature) {
    this.showFeelsLikeTemperature = showFeelsLikeTemperature;
  }

  public Boolean getShowPressure() {
    return showPressure;
  }

  public void setShowPressure(Boolean showPressure) {
    this.showPressure = showPressure;
  }

  public Boolean getShowHumidity() {
    return showHumidity;
  }

  public void setShowHumidity(Boolean showHumidity) {
    this.showHumidity = showHumidity;
  }

  public Boolean getShowDewPoint() {
    return showDewPoint;
  }

  public void setShowDewPoint(Boolean showDewPoint) {
    this.showDewPoint = showDewPoint;
  }

  public Boolean getShowClouds() {
    return showClouds;
  }

  public void setShowClouds(Boolean showClouds) {
    this.showClouds = showClouds;
  }

  public Boolean getShowUvi() {
    return showUvi;
  }

  public void setShowUvi(Boolean showUvi) {
    this.showUvi = showUvi;
  }

  public Boolean getShowVisibility() {
    return showVisibility;
  }

  public void setShowVisibility(Boolean showVisibility) {
    this.showVisibility = showVisibility;
  }

  public Boolean getShowRain() {
    return showRain;
  }

  public void setShowRain(Boolean showRain) {
    this.showRain = showRain;
  }

  public Boolean getShowSnow() {
    return showSnow;
  }

  public void setShowSnow(Boolean showSnow) {
    this.showSnow = showSnow;
  }

  public Boolean getShowWindSpeed() {
    return showWindSpeed;
  }

  public void setShowWindSpeed(Boolean showWindSpeed) {
    this.showWindSpeed = showWindSpeed;
  }

  public Boolean getShowWindGust() {
    return showWindGust;
  }

  public void setShowWindGust(Boolean showWindGust) {
    this.showWindGust = showWindGust;
  }

  public Boolean getShowWindDirection() {
    return showWindDirection;
  }

  public void setShowWindDirection(Boolean showWindDirection) {
    this.showWindDirection = showWindDirection;
  }

  public Boolean getShowTitle() {
    return showTitle;
  }

  public void setShowTitle(Boolean showTitle) {
    this.showTitle = showTitle;
  }

  public Boolean getShowIcon() {
    return showIcon;
  }

  public void setShowIcon(Boolean showIcon) {
    this.showIcon = showIcon;
  }

  @Serial private static final long serialVersionUID = 1;
}
