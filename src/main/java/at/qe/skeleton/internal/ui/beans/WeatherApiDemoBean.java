package at.qe.skeleton.internal.ui.beans;

import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import at.qe.skeleton.external.services.WeatherApiRequestService;
import at.qe.skeleton.internal.services.CurrentAndForecastWeatherService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.annotation.PostConstruct;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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

    @Autowired
    private CurrentAndForecastWeatherService currentAndForecastWeatherService;

    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherApiDemoBean.class);

    private String currentWeather;

    private Long searchId;

    private String searchedWeather;
    //hard coded coordinates of innsbruck
    private double latitude = 47.2692;

    private double longitude = 11.4041;

    @PostConstruct
    public void init() {
        try {
            CurrentAndForecastAnswerDTO answer = this.weatherApiRequestService.retrieveCurrentAndForecastWeather(getLatitude(), getLongitude());
            ObjectMapper mapper = new ObjectMapper()
                    .findAndRegisterModules()
                    .enable(SerializationFeature.INDENT_OUTPUT);
            String plainTextAnswer = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(answer);
            String escapedHtmlAnswer = StringEscapeUtils.escapeHtml4(plainTextAnswer);
            String escapedHtmlAnswerWithLineBreaks = escapedHtmlAnswer.replace("\n", "<br>")
                    .replace(" ", "&nbsp;");
            this.setCurrentWeather(escapedHtmlAnswerWithLineBreaks);
            currentAndForecastWeatherService.saveWeather(escapedHtmlAnswerWithLineBreaks);
        } catch (final Exception e) {
            LOGGER.error("error in request", e);
        }
    }

    public void weatherSearch() {
        try {
            this.searchedWeather = currentAndForecastWeatherService.findWeather(this.getSearchID());
        } catch (Exception e) {
            LOGGER.warn("error in fetching weather data", e);
            this.searchedWeather = "No weather data with the provided id was found.\n" +
                    "Please provide a valid search id";
        }
    }

    public String getSearchedWeather() {
        return searchedWeather;
    }

    public Long getSearchID() {
        return searchId;
    }

    public void setSearchID(Long searchID) {
        this.searchId = searchID;
    }

    public void setSearchedWeather(String searchedWeather) {
        this.searchedWeather = searchedWeather;
    }

    public String getCurrentWeather() {
        return currentWeather;
    }

    public void setCurrentWeather(String currentWeather) {
        this.currentWeather = currentWeather;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
