package at.qe.skeleton.internal.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import org.springframework.data.domain.Persistable;

@Entity
public class Location implements Persistable<LocationId>, Serializable {

  // Composite primary key (beautifully explained here
  // https://www.baeldung.com/jpa-composite-primary-keys)
  @EmbeddedId private LocationId locationId;

  private ZonedDateTime creationTimestamp;

  private String city;

  private String country;

  private String state;

  @OneToOne(optional = false)
  private CurrentAndForecastAnswer weather;

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
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

  public void setId(double lat, double lon) {
    this.locationId = new LocationId(lat, lon);
  }

  public void setCreationTimestamp(ZonedDateTime creationTimestamp) {
    this.creationTimestamp = creationTimestamp;
  }

  public LocationId getId() {
    return this.locationId;
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
