package at.qe.skeleton.internal.ui.beans;

import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import at.qe.skeleton.external.model.location.LocationAnswerDTO;
import at.qe.skeleton.external.services.GeocodingApiRequestService;
import at.qe.skeleton.external.services.WeatherApiRequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.annotation.ManagedBean;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

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
    private GeocodingApiRequestService geocodingApiRequestService;

    private List<LocationAnswerDTO> locationAnswerDTOList;
    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherApiDemoBean.class);

    private String currentWeather;

    //these were hard coded coordinates of innsbruck - i want to fill them now from the weather_api_demo.xhtml
    private double latitude;

    private double longitude;

    private String locationSearchInput = "Berlin";

    //hardcoded limit - i.e. the number of locations in the API response
    //TODO: Maybe make it possible to show more than one result from the api
    //          -> in that case the result should not be stored in single LocationAnswerDTO but multiple LocationAnswerDTOs
    private int limit = 1;


    public String getLocationSearchInput() {
        return locationSearchInput;
    }

    public void setLocationSearchInput(String locationSearchInput) {
        this.locationSearchInput = locationSearchInput;
    }

    public void performLocationSearch() {
        //String input= this.locationSearchInput;
        // lets start with a test call:
        //TODO: more than one LocationAnswer: how to display the list?
        String input = this.locationSearchInput;
        this.locationAnswerDTOList = this.geocodingApiRequestService.retrieveLocationLonLat(input, limit);
        Optional<LocationAnswerDTO> optionalLocation = locationAnswerDTOList.stream().findAny();
        if (optionalLocation.isPresent()) {
            this.latitude = optionalLocation.get().latitude();
            this.longitude = optionalLocation.get().longitude();
        }
        System.out.println("performing location Search for: " + input + " -> latitude: " + this.latitude + " longitude: " + this.longitude);
    }

    public void performWeatherApiRequest() {
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
            System.out.println("performing Weather api Request with latitude = " + getLatitude() + " and longitude = " + getLongitude());
        } catch (final Exception e) {
            LOGGER.error("error in request", e);
        }
    }

    public void performLocationSearchAndWeatherRequest() {
        this.performLocationSearch();
        this.performWeatherApiRequest();
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


//todo: introduce error handling
//todo: write tests
