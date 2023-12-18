package at.qe.skeleton.internal.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.domain.Persistable;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
public class CurrentAndForecastWeather implements Persistable<String>, Serializable {

    @Id
    @GeneratedValue
    private Long id;

    @CreationTimestamp
    private LocalDateTime createDate;
    @Lob
    private byte[] weatherData;


    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public byte[] getWeatherData() {
        return weatherData;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public void setWeatherData(byte[] weatherData) {
        this.weatherData = weatherData;
    }

    @Override
    public String getId() {
        return String.valueOf(this.id);
    }

    @Override
    public boolean isNew() {
        return (null == createDate);
    }
}
