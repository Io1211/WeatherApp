package at.qe.skeleton.internal.model;

import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;

import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * Entity to persist current and forecast weather data
 */
@Entity
public class CurrentAndForecastAnswer implements Persistable<String>, Serializable {

    @Id
    @GeneratedValue
    private Long id;

    private ZonedDateTime timestampLastCall;
    @Lob
    private byte[] weatherData;

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
    public String getId() {
        return String.valueOf(this.id);
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean isNew() {
        return (null == timestampLastCall);
    }
}
