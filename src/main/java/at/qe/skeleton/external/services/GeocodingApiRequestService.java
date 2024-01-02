package at.qe.skeleton.external.services;

import at.qe.skeleton.external.model.location.LocationAnswerDTO;

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

import java.util.List;

@Scope("application")
@Component
@Validated // makes sure the parameter validation annotations are checked during runtime
public class GeocodingApiRequestService {

  /**
   * Beispiel API call: http://api.openweathermap.org/geo/1.0/direct?q={city name},{state
   * code},{country code}&limit={limit}&appid={API key}
   *
   * <p>Fragen: müssen wir möglicherweise mehr als einen Ort als DTO objekt speichern?
   */
  private static final String GEOCODING_URI = "/geo/1.0/direct";

  private static final String LOCATION_NAME = "q";
  private static final String LIMIT_OF_RESULTS = "limit";
  private static final int LIMIT_VALUE = 1;

  private final RestClient restClient;

  @Autowired
  public GeocodingApiRequestService(RestClient restClient) {
    this.restClient = restClient;
  }

  /**
   * Makes an API call to get the exact geographical coordinates to the given name of a location or
   * zip/post code <br>
   * <br>
   *
   * @param locationName that is coming from UI. Can include country code and state after comma.
   * @return langitude & latitude of the location
   */
  public LocationAnswerDTO retrieveLocationLonLat(String locationName) throws RuntimeException {

    // todo: should we include country code or something in that method?
    ResponseEntity<List<LocationAnswerDTO>> responseEntity =
        this.restClient
            .get()
            .uri(
                UriComponentsBuilder.fromPath(GEOCODING_URI)
                    .queryParam(LOCATION_NAME, locationName)
                    .queryParam(LIMIT_OF_RESULTS, String.valueOf(LIMIT_VALUE))
                    .build()
                    .toUriString())
            .retrieve()
            .toEntity(new ParameterizedTypeReference<>() {});

    HttpStatusCode statusCode = responseEntity.getStatusCode();

    if (statusCode.is4xxClientError()) {
      throw new HttpClientErrorException(
          responseEntity.getStatusCode(),
          "Geocoding Client error for searchstring: %s".formatted(locationName));
    }
    if (statusCode.is5xxServerError()) {
      throw new HttpServerErrorException(
          responseEntity.getStatusCode(),
          "Geocoding Server error for searchstring: %s".formatted(locationName));
    }

    // since the API returns a json List we need to take the first element out of the list, which is
    // the DTO
    List<LocationAnswerDTO> locationAnswerList = responseEntity.getBody();
    if (locationAnswerList != null && !locationAnswerList.isEmpty()) {
      return locationAnswerList.get(0);
    } else {
      // todo: think about error handling...
      //  what if no search results from api? show error in frontend...
      throw new RuntimeException("GeocodingApiRequest returned no LocationAnswerDTO");
    }
  }
}
