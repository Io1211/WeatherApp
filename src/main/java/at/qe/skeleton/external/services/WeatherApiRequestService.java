package at.qe.skeleton.external.services;

import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.*;
import org.springframework.web.util.UriComponentsBuilder;


/**
 * This class is part of the skeleton project provided for students of the course "Software
 * Architecture" offered by Innsbruck University.
 */

// The "application" scope creates the bean instance for the lifecycle of a ServletContext.
@Scope("application")
@Component
@Validated // makes sure the parameter validation annotations are checked during runtime
public class WeatherApiRequestService {

  private static final String CURRENT_AND_FORECAST_URI = "/data/3.0/onecall";

  private static final String LONGITUDE_PARAMETER = "lon";

  private static final String LATITUDE_PARAMETER = "lat";

  private final RestClient restClient;

  @Autowired
  public WeatherApiRequestService(RestClient restClient) {
    this.restClient = restClient;
  }

  /**
   * Makes an API call to get the current and a weather forecast for a specified location <br>
   * <br>
   * If you are unaware of lat/lon of the location use the geocoding api to determine those
   * parameters
   *
   * @param latitude of the location
   * @param longitude of the locationx
   * @return the current and forecast weather
   * @throws HttpStatusCodeException when 4xx or 5xx Status Code is returned
   */

  // todo: decide what happens when 4xx or 5xx status code is retrieved from API. where do we handle
  // HttpStatusCodeException?
  public CurrentAndForecastAnswerDTO retrieveCurrentAndForecastWeather(
      @Min(-90) @Max(90) double latitude, @Min(-180) @Max(180) double longitude) {

    ResponseEntity<CurrentAndForecastAnswerDTO> responseEntity =
        this.restClient
            .get()
            .uri(
                UriComponentsBuilder.fromPath(CURRENT_AND_FORECAST_URI)
                    .queryParam(LATITUDE_PARAMETER, String.valueOf(latitude))
                    .queryParam(LONGITUDE_PARAMETER, String.valueOf(longitude))
                    .build()
                    .toUriString())
            .retrieve()
            .toEntity(CurrentAndForecastAnswerDTO.class);

    HttpStatusCode statusCode = responseEntity.getStatusCode();

    if (statusCode.is4xxClientError()) {
      throw new HttpClientErrorException(
          responseEntity.getStatusCode(),
          "Client error for values: lat=%f, lon=%f".formatted(latitude, longitude));
    }
    if (statusCode.is5xxServerError()) {
      throw new HttpServerErrorException(
          responseEntity.getStatusCode(),
          "Server error for values: lat=%f, lon=%f".formatted(latitude, longitude));
    }

    return responseEntity.getBody();
  }
}
