package at.qe.skeleton.internal.model;

import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entity to persist current and forecast weather data
 */
@Entity
public class CurrentAndForecastWeather implements Persistable<String>, Serializable {

    @Id
    @GeneratedValue
    private Long id;

    private LocalDateTime lastModified;
    @Lob
    private byte[] weatherData;

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public byte[] getWeatherData() {
        return weatherData;
    }

    public void setWeatherData(byte[] weatherData) {
        this.weatherData = weatherData;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    @PrePersist
    public void onCreate() {
        this.lastModified = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.lastModified = LocalDateTime.now();
    }

    @Override
    public String getId() {
        return String.valueOf(this.id);
    }

    @Override
    public boolean isNew() {
        return (null == lastModified);
    }
}
