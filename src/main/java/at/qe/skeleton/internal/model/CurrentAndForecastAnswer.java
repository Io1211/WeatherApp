package at.qe.skeleton.internal.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import org.springframework.data.domain.Persistable;

/** Entity to persist current and forecast weather data */
@Entity
public class CurrentAndForecastAnswer implements Persistable<Long>, Serializable {

  @Id @GeneratedValue private Long id;

  private ZonedDateTime timestampLastCall;

  @Lob private byte[] weatherData;

  @OneToOne(mappedBy = "weather", cascade = CascadeType.ALL)
  private Location location;

  public Location getLocation() {
    return location;
  }

  public void setLocation(Location location) {
    this.location = location;
  }

  public ZonedDateTime getTimestampLastCall() {
    return timestampLastCall;
  }

  public byte[] getWeatherData() {
    return weatherData;
  }

  public void setWeatherData(byte[] weatherData) {
    this.weatherData = weatherData;
  }

  public void setTimestampLastCall(ZonedDateTime timestampLastCall) {
    this.timestampLastCall = timestampLastCall;
  }

  @PrePersist
  public void onCreate() {
    this.timestampLastCall = ZonedDateTime.now();
  }

  @Override
  public Long getId() {
    return this.id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  @Override
  public boolean isNew() {
    return (null == timestampLastCall);
  }
}
