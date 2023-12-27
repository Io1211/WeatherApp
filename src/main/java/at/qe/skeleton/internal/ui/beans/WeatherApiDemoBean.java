package at.qe.skeleton.internal.ui.beans;

import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import at.qe.skeleton.external.services.WeatherApiRequestService;
import at.qe.skeleton.internal.services.CurrentAndForecastAnswerService;
import at.qe.skeleton.internal.services.FailedJsonToDtoMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * Demonstrates the working api and what the raw request data would look like <br>
 * <br>
 * This class is part of the skeleton project provided for students of the course "Software
 * Architecture" offered by Innsbruck University.
 */
@Component
@Scope("view")
public class WeatherApiDemoBean {

    @Autowired
    private WeatherApiRequestService weatherApiRequestService;

    @Autowired
    private CurrentAndForecastAnswerService currentAndForecastAnswerService;

    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherApiDemoBean.class);

    private String currentWeather;

    private Long searchId;

    private String searchedWeather;
    // hard coded coordinates of innsbruck
//    private double latitude = 47.2692;
//    private double longitude = 11.4041;
    // hard coded coordinates of milan
    private double latitude = 45.4642;
    private double longitude = 9.1900;

    public void callApi() {
        try {
            CurrentAndForecastAnswerDTO answer =
                    this.weatherApiRequestService.retrieveCurrentAndForecastWeather(
                            getLatitude(), getLongitude());
            currentAndForecastAnswerService.saveWeather(answer);
            ObjectMapper mapper =
                    new ObjectMapper().findAndRegisterModules().enable(SerializationFeature.INDENT_OUTPUT);
            String plainTextAnswer = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(answer);
            String escapedHtmlAnswer = StringEscapeUtils.escapeHtml4(plainTextAnswer);
            String escapedHtmlAnswerWithLineBreaks =
                    escapedHtmlAnswer.replace("\n", "<br>").replace(" ", "&nbsp;");
            this.setCurrentWeather(escapedHtmlAnswerWithLineBreaks);
        } catch (final Exception e) {
            LOGGER.error("error in request", e);
        }
    }

    public void findAll() throws FailedJsonToDtoMappingException {
        Collection<CurrentAndForecastAnswerDTO> currentAndForecastAnswerDTOS =
                currentAndForecastAnswerService.getAllCurrentAndForecastWeather();
        if (currentAndForecastAnswerDTOS.isEmpty()) {
            this.searchedWeather = "There are no weather entries present in the database at this moment";
            return;
        }
        StringBuilder renderedWeather = new StringBuilder();
        for (CurrentAndForecastAnswerDTO currentAndForecastAnswerDTO : currentAndForecastAnswerDTOS) {
            renderedWeather.append(
                    "Weather id : %s<br>"
                            .formatted(currentAndForecastAnswerDTO.currentWeather().weather().id()));
            renderedWeather.append(
                    "Description: %s<br>"
                            .formatted(currentAndForecastAnswerDTO.currentWeather().weather().description()));
            renderedWeather.append(
                    "Title      : %s<br><br>"
                            .formatted(currentAndForecastAnswerDTO.currentWeather().weather().title()));
        }
        this.searchedWeather = renderedWeather.toString();
    }

    public void findLastHour() throws FailedJsonToDtoMappingException {
        Collection<CurrentAndForecastAnswerDTO> weather =
                currentAndForecastAnswerService.getLastHourCurrentAndForecastWeather();
        if (weather == null) {
            this.searchedWeather = "No weather entry was found by that id";
            return;
        }
        StringBuilder renderedWeather = new StringBuilder();
        for (CurrentAndForecastAnswerDTO currentAndForecastAnswerDTO : weather) {
            renderedWeather.append(
                    "Weather id : %s<br>"
                            .formatted(currentAndForecastAnswerDTO.currentWeather().weather().id()));
            renderedWeather.append(
                    "Description: %s<br>"
                            .formatted(currentAndForecastAnswerDTO.currentWeather().weather().description()));
            renderedWeather.append(
                    "Title      : %s<br><br>"
                            .formatted(currentAndForecastAnswerDTO.currentWeather().weather().title()));
        }
        this.searchedWeather = renderedWeather.toString();
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
