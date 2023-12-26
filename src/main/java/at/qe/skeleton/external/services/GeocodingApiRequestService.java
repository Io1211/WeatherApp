package at.qe.skeleton.external.services;


import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import at.qe.skeleton.external.model.location.LocationAnswerDTO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;



import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
    private static final int LIMIT_VALUE = 1;



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
    public LocationAnswerDTO retrieveLocationLonLat(String locationName) throws RuntimeException {

        ResponseEntity<List<LocationAnswerDTO>> responseEntity=  this.restClient.get()
                .uri(UriComponentsBuilder.fromPath(GEOCODING_URI)
                        .queryParam(LOCATION_NAME, locationName)
                        .queryParam(LIMIT_OF_RESULTS, String.valueOf(LIMIT_VALUE))
                        .build().toUriString())
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<LocationAnswerDTO>>() {});



        HttpStatusCode statusCode = responseEntity.getStatusCode();

        if (statusCode.is4xxClientError()) {
            throw new HttpClientErrorException(responseEntity.getStatusCode(), "Geocoding Client error for searchstring: %s".formatted(locationName));
        }
        if (statusCode.is5xxServerError()) {
            throw new HttpServerErrorException(responseEntity.getStatusCode(), "Geocoding Client error for searchstring: %s".formatted(locationName));
        }

        //since the API returns a json List we need to take the first element out of the list, which is the DTO
        List<LocationAnswerDTO> locationAnswerList = responseEntity.getBody();
        if (locationAnswerList != null && !locationAnswerList.isEmpty()) {
            return locationAnswerList.get(0);
        } else {
            throw new RuntimeException("GeocodingApiRequest returned no LocationAnswerDTO");
        }



        // todo introduce error handling using responseEntity.getStatusCode.isXXXError
    }


}
