package at.qe.skeleton.external.services;


import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import at.qe.skeleton.external.model.location.LocationAnswerDTO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

/**
 * Makes an API call to get the exact geographical coordinates to the given name of a location or zip/post code
 * <br><br>
 *
 * @param locationName
 * @param countryCode
 * @return langitude & latitude of the location
 */


@Scope("application")
@Component
@Validated // makes sure the parameter validation annotations are checked during runtime
public class GeocodingApiRequestService {

    /**
     * Beispiel API call:
     * http://api.openweathermap.org/geo/1.0/direct?q={city name},{state code},{country code}&limit={limit}&appid={API key}
     * <p>
     * Fragen: müssen wir möglicherweise mehr als einen Ort als DTO objekt speichern?
     */


    private static final String GEOCODING_URI = "/geo/1.0/direct";

    private static final String LOCATION_NAME = "q";
    private static final String LIMIT_OF_RESULTS = "limit";



    @Autowired
    private RestClient restClient;

    /**
     * Makes an API call to get the current and a weather forecast for a specified location
     * <br><br>
     * If you are unaware of lat/lon of the location use the geocoding api to determine those parameters
     *
     * @param latitude  of the location
     * @param longitude of the location
     * @return the current and forecast weather
     */
    public List<LocationAnswerDTO> retrieveLocationLonLat(String locationName, int limit) {

        return this.restClient.get()
                .uri(UriComponentsBuilder.fromPath(GEOCODING_URI)
                        .queryParam(LOCATION_NAME, locationName)
                        .queryParam(LIMIT_OF_RESULTS, String.valueOf(limit))
                        .build().toUriString())
                .retrieve()
                .body(new ParameterizedTypeReference<List<LocationAnswerDTO>>() {});
        // todo introduce error handling using responseEntity.getStatusCode.isXXXError

        //todo: write tests

    }
}
