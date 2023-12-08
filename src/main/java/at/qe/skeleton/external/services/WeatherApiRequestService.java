package at.qe.skeleton.external.services;

import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;


/**
 * This class is part of the skeleton project provided for students of the
 * course "Software Architecture" offered by Innsbruck University.
 */
@Scope("application")
@Component
@Validated // makes sure the parameter validation annotations are checked during runtime
public class WeatherApiRequestService {

    private static final String CURRENT_AND_FORECAST_URI = "/data/3.0/onecall";

    private static final String LONGITUDE_PARAMETER = "lon";

    private static final String LATITUDE_PARAMETER = "lat";

    @Autowired
    private RestClient restClient;

    /**
     * Makes an API call to get the current and a weather forecast for a specified location
     * <br><br>
     * If you are unaware of lat/lon of the location use the geocoding api to determine those parameters
     * @param latitude of the location
     * @param longitude of the location
     * @return the current and forecast weather
     */
    public CurrentAndForecastAnswerDTO retrieveCurrentAndForecastWeather(@Min(-90) @Max(90) double latitude,
                                                                         @Min(-180) @Max(180) double longitude) {

        ResponseEntity<CurrentAndForecastAnswerDTO> responseEntity = this.restClient.get()
                .uri(UriComponentsBuilder.fromPath(CURRENT_AND_FORECAST_URI)
                        .queryParam(LATITUDE_PARAMETER, String.valueOf(latitude))
                        .queryParam(LONGITUDE_PARAMETER, String.valueOf(longitude))
                        .build().toUriString())
                .retrieve()
                .toEntity(CurrentAndForecastAnswerDTO.class);

        // todo introduce error handling using responseEntity.getStatusCode.isXXXError
        return responseEntity.getBody();
    }

}
