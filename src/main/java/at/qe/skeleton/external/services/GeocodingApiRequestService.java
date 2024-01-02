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
  private static final int LIMIT_VALUE = 5;

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
   * @param locationName to search for - it is coming from UI
   * @param limit limit of locations that should be returned from ui
   * @return A list of LocationAnswerDTO objects containing the longitude and latitude, as well as
   *     name, country code and state(maybe null) of the location.
   */
  public List<LocationAnswerDTO> retrieveLocationsLonLat(String locationName, int limit)
      throws RuntimeException {

    // todo: should we include country code or something in that method?
    ResponseEntity<List<LocationAnswerDTO>> responseEntity =
        this.restClient
            .get()
            .uri(
                UriComponentsBuilder.fromPath(GEOCODING_URI)
                    .queryParam(LOCATION_NAME, locationName)
                    .queryParam(LIMIT_OF_RESULTS, String.valueOf(limit))
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
      return locationAnswerList;
    } else {
      // man könnte in UI dann an den user ein fenster öffnen mit
      // "we couldnt find any Locations with the name "#{WeatherApiDemoBean.locationSearchInput}""
      throw new RuntimeException("GeocodingApiRequest returned no LocationAnswerDTO");
    }

    // todo introduce error handling using responseEntity.getStatusCode.isXXXError
  }
}
