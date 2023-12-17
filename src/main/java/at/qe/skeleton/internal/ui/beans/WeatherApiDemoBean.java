package at.qe.skeleton.internal.ui.beans;

import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import at.qe.skeleton.external.model.currentandforecast.misc.CurrentWeatherDTO;
import at.qe.skeleton.external.model.currentandforecast.misc.HourlyWeatherDTO;
import at.qe.skeleton.external.services.WeatherApiRequestService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

/**
 * Demonstrates the working api and what the raw request data would look like
 * <br><br>
 * This class is part of the skeleton project provided for students of the
 * course "Software Architecture" offered by Innsbruck University.
 */
@Component
@Scope("view")
public class WeatherApiDemoBean {

    @Autowired
    private WeatherApiRequestService weatherApiRequestService;

    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherApiDemoBean.class);


    // derzeit nur currentWeather -> wir holen uns von der API aber auch Forecast Weather.
    // wie können wir dieses abfangen und abspeichern?
    private CurrentWeatherDTO currentWeather;

    //hard coded coordinates of innsbruck
    private double latitude = 47.2692;

    private double longitude = 11.4041;

    //now add the other values from the CurrentAndForecastAnswerDTO (and CurrentWeatherDTO and so on...)
    // muss jetzt rausfinden wie ich die befülle.
    private String timezone;
    private double temperature;
    private double feelsLikeTemperature;
    private Collection<HourlyWeatherDTO> hourlyWeather;


    public Collection<HourlyWeatherDTO> getHourlyWeather() {
        return hourlyWeather;
    }

    public void setHourlyWeather(List<HourlyWeatherDTO> hourlyWeather){
        this.hourlyWeather = hourlyWeather;
    }

    @PostConstruct
    public void init() {
        try {
            CurrentAndForecastAnswerDTO answer = this.weatherApiRequestService.retrieveCurrentAndForecastWeather(getLatitude(), getLongitude());
            //ObjectMapper mapper = new ObjectMapper()
            //        .findAndRegisterModules()
            //        .enable(SerializationFeature.INDENT_OUTPUT);
            //String plainTextAnswer = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(answer);
            //String escapedHtmlAnswer = StringEscapeUtils.escapeHtml4(plainTextAnswer);
            //String escapedHtmlAnswerWithLineBreaks = escapedHtmlAnswer.replace("\n", "<br>")
            //        .replace(" ", "&nbsp;");
            //this.setCurrentWeather(escapedHtmlAnswerWithLineBreaks);
            this.timezone = answer.timezone();
            this.currentWeather = answer.currentWeather();
            this.temperature = currentWeather.temperature();
            this.feelsLikeTemperature = currentWeather.feelsLikeTemperature();
            this.hourlyWeather = answer.hourlyWeather();



        } catch (final Exception e) {
            LOGGER.error("error in request", e);
        }
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public void setFeelsLikeTemperature(double feelsLikeTemperature) {
        this.feelsLikeTemperature = feelsLikeTemperature;
    }

    public String getTimezone() {
        return timezone;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getFeelsLikeTemperature() {
        return feelsLikeTemperature;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
