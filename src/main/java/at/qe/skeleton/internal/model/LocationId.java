package at.qe.skeleton.internal.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

// necessary for entities with composite primary keys
// see https://www.baeldung.com/jpa-composite-primary-keys
@Embeddable
public class LocationId implements Serializable {

  private double latitude;

  private double longitude;

  public LocationId(@NotNull double latitude, @NotNull double longitude) {
    this.latitude = latitude;
    this.longitude = longitude;
  }

  public LocationId() {}

  public double getLatitude() {
    return latitude;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public void setLongitude(double longitude) {
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
