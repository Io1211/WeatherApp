package at.qe.skeleton.internal.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;
import org.springframework.data.domain.Persistable;

@Entity
@IdClass(LocationId.class) // necessary for entities with composite primary keys
public class Location implements Persistable<String>, Serializable {

  @Id private double latitude;

  @Id private double longitude;

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

  public double getLatitude() {
    return latitude;
  }

  public double getLongitude() {
    return longitude;
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
    this.latitude = lat;
    this.longitude = lon;
  }

  public void setCreationTimestamp(ZonedDateTime creationTimestamp) {
    this.creationTimestamp = creationTimestamp;
  }

  public String getId() {
    return "lat %s, lon %s".formatted(latitude, longitude);
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

// necessary for entities with composite primary keys
// see https://www.baeldung.com/jpa-composite-primary-keys
class LocationId implements Serializable {
  private double latitude;

  private double longitude;

  public LocationId(double latitude, double longitude) {
    this.latitude = latitude;
    this.longitude = longitude;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    LocationId that = (LocationId) o;
    return Double.compare(latitude, that.latitude) == 0
        && Double.compare(longitude, that.longitude) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(latitude, longitude);
  }
}
