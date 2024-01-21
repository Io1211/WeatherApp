package at.qe.skeleton.internal.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.Instant;
import org.springframework.data.domain.Persistable;


@Entity
public class DailyWeatherEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    public Instant sunrise;
    public Instant sunset;
    public Double dayTemperature;
    public Double minTemperature;
    public Double maxTemperature;
    public Double feelsLikeTemperature;
    public Double windSpeed;
    public Double windDeg;
    public Double windGust;
    public String summary;
    public Integer pop;
    public Double rain;
    public Double snow;


    public DailyWeatherEntry(
            Instant sunrise,
            Instant sunset,
            Double dayTemperature,
            Double minTemperature,
            Double maxTemperature,
            Double feelsLikeTemperature,
            Double windSpeed,
            Double windDeg,
            Double windGust,
            String summary,
            Integer pop,
            Double rain,
            Double snow) {

        this.sunrise = sunrise;
        this.sunset = sunset;
        this.dayTemperature = dayTemperature;
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        this.feelsLikeTemperature = feelsLikeTemperature;
        this.windSpeed = windSpeed;
        this.windDeg = windDeg;
        this.windGust = windGust;
        this.summary = summary;
        this.pop = pop;
        this.rain = rain;
        this.snow = snow;
    }

    public Instant getSunrise() {
        return sunrise;
    }

    public Instant getSunset() {
        return sunset;
    }

    public Double getDayTemperature() {
        return dayTemperature;
    }

    public Double getMinTemperature() {
        return minTemperature;
    }

    public Double getMaxTemperature() {
        return maxTemperature;
    }

    public Double getFeelsLikeTemperature() {
        return feelsLikeTemperature;
    }

    public Double getWindSpeed() {
        return windSpeed;
    }

    public Double getWindDeg() {
        return windDeg;
    }

    public Double getWindGust() {
        return windGust;
    }

    public String getSummary() {
        return summary;
    }

    public Integer getPop() {
        return pop;
    }

    public Double getRain() {
        return rain;
    }

    public Double getSnow() {
        return snow;
    }

}
