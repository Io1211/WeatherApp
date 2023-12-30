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

/**
 * Makes an API call to get the exact geographical coordinates to the given name of a location or
 * zip/post code <br>
 * <br>
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
   * Makes an API call to get the current and a weather forecast for a specified location <br>
   * <br>
   * If you are unaware of lat/lon of the location use the geocoding api to determine those
   * parameters
   *
   * @param latitude of the location
   * @param longitude of the location
   * @return the current and forecast weather
   */

  // Umlaut-Locations werden hier doppelt kodiert vom uricomponentsbuilder... Warum? keine ahnung...
  // siehe hier:
  // https://stackoverflow.com/questions/34321361/avoid-double-encoding-of-url-query-param-with-springs-resttemplate
  // https://stackoverflow.com/questions/60835309/how-to-avoid-double-encoding-of-when-using-spring-resttemplate
  // URLs werden mithilfe von %... codiert.
  // komisch ist dass der test eine richtige kodierung vorgibt. aber der test verwendet auch einen
  // fake restClient, vllt liegt da das problem?

  public LocationAnswerDTO retrieveLocationLonLat(String locationName) throws RuntimeException {

    ResponseEntity<List<LocationAnswerDTO>> responseEntity =
        this.restClient
            .get()
            .uri(
                UriComponentsBuilder.fromPath(GEOCODING_URI)
                    .queryParam(LOCATION_NAME, locationName)
                    .queryParam(LIMIT_OF_RESULTS, String.valueOf(LIMIT_VALUE))
                    // if i set build(encoded: true) then it says: invalid character "ö" but if i
                    // leave it false or default then it gets double encoded...?
                    .build()
                    .toUriString())
            .retrieve()
            .toEntity(new ParameterizedTypeReference<List<LocationAnswerDTO>>() {});

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
      // man könnte in UI dann an den user ein fenster öffnen mit
      // "we couldnt find any Locations with the name "#{WeatherApiDemoBean.locationSearchInput}""
      throw new RuntimeException("GeocodingApiRequest returned no LocationAnswerDTO");
    }

    // todo introduce error handling using responseEntity.getStatusCode.isXXXError
  }
}
