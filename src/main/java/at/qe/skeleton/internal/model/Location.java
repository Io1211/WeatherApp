package at.qe.skeleton.internal.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import org.springframework.data.domain.Persistable;

@Entity
public class Location implements Persistable<String>, Serializable {

  @Id private double lat;

  @Id private double lon;

  private ZonedDateTime creationTimestamp;

  private String city;

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public double getLat() {
    return lat;
  }

  public void setLat(double lat) {
    this.lat = lat;
  }

  public double getLon() {
    return lon;
  }

  public void setLon(double lon) {
    this.lon = lon;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public CurrentAndForecastAnswer getWeather() {
    return weather;
  }

  public void setWeather(CurrentAndForecastAnswer weather) {
    this.weather = weather;
  }

  private String country;

  private String state;

  @OneToOne(optional = false)
  private CurrentAndForecastAnswer weather;

  public void setId(double lat, double lon) {
    this.lat = lat;
    this.lon = lon;
  }

  public void setCreationTimestamp(ZonedDateTime creationTimestamp) {
    this.creationTimestamp = creationTimestamp;
  }

  public String getId() {
    return "lat %s, lon %s".formatted(lat, lon);
  }

  public ZonedDateTime getCreationTimestamp() {
    return creationTimestamp;
  }

  @PrePersist
  public void onCreate() {
    this.creationTimestamp = ZonedDateTime.now();
  }

  @Override
  public boolean isNew() {
    return (null == creationTimestamp);
  }
}
