package at.qe.skeleton.tests;

import at.qe.skeleton.external.model.location.LocationAnswerDTO;
import at.qe.skeleton.external.services.GeocodingApiRequestService;
import at.qe.skeleton.internal.services.exceptions.GeocodingApiReturnedEmptyListException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GeocodingApiLocationDtoTest {

  private static RestClient testRestClient;
  private static MockRestServiceServer mockServer;
  private static final ObjectMapper mapper = new ObjectMapper();
  private static String apiResponseStringWoergl;

  private static String emptyApiResponseString;
  private static String apiResponseStringIbk;
  private static GeocodingApiRequestService geocodingApiRequestService;
  private static final int LIMIT = 1;

  @BeforeAll
  static void prepareApiTestEnvironment() throws Exception {

    // stub RestTemplate and Mockserver for mock-api-requests
    RestTemplate restTemplate = new RestTemplate();
    mockServer = MockRestServiceServer.bindTo(restTemplate).build();
    testRestClient = RestClient.create(restTemplate);

    // writing the Wörgl json api response as String
    ClassPathResource classPathResourceUmlaut =
        new ClassPathResource("GeocodingResponseWörgl.json");
    JsonNode jsonNodeUmlaut = mapper.readTree(classPathResourceUmlaut.getInputStream());
    apiResponseStringWoergl = mapper.writeValueAsString(jsonNodeUmlaut);

    ClassPathResource classPathResourceEmpty =
        new ClassPathResource("emptyGeocodingApiResponse.json");
    JsonNode jsonNodeEmptyResponse = mapper.readTree(classPathResourceEmpty.getInputStream());
    emptyApiResponseString = mapper.writeValueAsString(jsonNodeEmptyResponse);

    // writing the Innsbruck json api response as String
    ClassPathResource classPathResourceIbk =
        new ClassPathResource("GeocodingResponseInnsbruck.json");
    JsonNode jsonNodeIbk = mapper.readTree(classPathResourceIbk.getInputStream());
    apiResponseStringIbk = mapper.writeValueAsString(jsonNodeIbk);

    // need to initialize the geocodingApiRequestService with the testRestClient
    // testRestClient is bound to the mock Server so calls dont really go out to the web.
    geocodingApiRequestService = new GeocodingApiRequestService(testRestClient);
  }

  @BeforeEach
  void clearMockServerExpectations() {
    mockServer.reset();
  }

  @Test
  public void GeocodingApiServiceBuildsAndCallsCorrectURI()
      throws GeocodingApiReturnedEmptyListException {

    // setting which call should be expected by Mockserver and how he responds.
    mockServer
        .expect(requestTo("/geo/1.0/direct?q=Innsbruck&limit=" + LIMIT))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(apiResponseStringIbk, MediaType.APPLICATION_JSON));
    // naking the api-request
    geocodingApiRequestService.retrieveLocationsLonLat("Innsbruck", LIMIT);

    // the actual test: verifying if request reached the expected URI
    mockServer.verify();
  }

  @Test
  public void geocodingApiServiceBuildsCorrectDtoObjectFromApiResponse()
      throws GeocodingApiReturnedEmptyListException {
    mockServer
        .expect(requestTo("/geo/1.0/direct?q=Innsbruck&limit=" + LIMIT))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(apiResponseStringIbk, MediaType.APPLICATION_JSON));

    GeocodingApiRequestService geocodingApiRequestService =
        new GeocodingApiRequestService(testRestClient);

    LocationAnswerDTO actualLocationAnswerDTO =
        geocodingApiRequestService.retrieveLocationsLonLat("Innsbruck", LIMIT).get(0);

    mockServer.verify();
    Assertions.assertEquals("Innsbruck", actualLocationAnswerDTO.name());
    Assertions.assertEquals(47.2654296, actualLocationAnswerDTO.latitude());
    Assertions.assertEquals(11.3927685, actualLocationAnswerDTO.longitude());
    Assertions.assertEquals("AT", actualLocationAnswerDTO.country());
    Assertions.assertEquals("Tyrol", actualLocationAnswerDTO.state());
  }

  // Durch den RequestInterceptor wurden special Characters doppelt enkodiert. Den Request
  // Interceptor testen wir hier
  // allerdings nicht, weil wir unseren eigenen RestTemplate einfügen und nicht den restClient aus
  // der apiConfiguration
  // verwenden.
  @Test
  public void geocodingApiServiceBuildsCorrectUrlAndDtoWithEncoding()
      throws GeocodingApiReturnedEmptyListException {
    // Wörgl is being encoded to URL compatible message with UTF-8 as Base to W%C3%B6rgl
    String locationName = "Wörgl";

    String locationNameEncoded = URLEncoder.encode(locationName, StandardCharsets.UTF_8);
    mockServer
        .expect(requestTo("/geo/1.0/direct?q=" + locationNameEncoded + "&limit=" + LIMIT))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(apiResponseStringWoergl, MediaType.APPLICATION_JSON));

    // need to initialize the geocodingApiRequestService with the testRestClient
    GeocodingApiRequestService geocodingApiRequestService =
        new GeocodingApiRequestService(testRestClient);

    LocationAnswerDTO actualLocationAnswerDTO =
        geocodingApiRequestService.retrieveLocationsLonLat(locationName, LIMIT).get(0);

    mockServer.verify();
    Assertions.assertEquals("Stadt Wörgl", actualLocationAnswerDTO.name());
    Assertions.assertEquals(47.48033265, actualLocationAnswerDTO.latitude());
    Assertions.assertEquals(12.07928067668956, actualLocationAnswerDTO.longitude());
    Assertions.assertEquals("AT", actualLocationAnswerDTO.country());
    Assertions.assertEquals("Tyrol", actualLocationAnswerDTO.state());
  }

  // todo: add test for empty answer from api
  @Test
  public void shouldThrowEmptyListException() {
    String locationName = "thisIsNotARealLocationName";

    mockServer
        .expect(requestTo("/geo/1.0/direct?q=" + locationName + "&limit=" + LIMIT))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(emptyApiResponseString, MediaType.APPLICATION_JSON));

    GeocodingApiRequestService geocodingApiRequestService =
        new GeocodingApiRequestService(testRestClient);

    Assertions.assertThrows(
        GeocodingApiReturnedEmptyListException.class,
        () -> geocodingApiRequestService.retrieveLocationsLonLat(locationName, LIMIT));

    mockServer.verify();
  }

  @Test
  void clientError() {
    String locationName = "locationName";
    HttpStatus status = HttpStatus.valueOf(400);

    mockServer
        .expect(requestTo("/geo/1.0/direct?q=" + locationName + "&limit=" + LIMIT))
        .andRespond(withStatus(status));

    Assertions.assertThrows(
        HttpClientErrorException.class,
        () -> geocodingApiRequestService.retrieveLocationsLonLat(locationName, LIMIT));
  }

  @Test
  void serverError() {
    String locationName = "locationName";
    HttpStatus status = HttpStatus.valueOf(501);
    mockServer
        .expect(requestTo("/geo/1.0/direct?q=" + locationName + "&limit=" + LIMIT))
        .andRespond(withStatus(status));

    Assertions.assertThrows(
        HttpServerErrorException.class,
        () -> geocodingApiRequestService.retrieveLocationsLonLat(locationName, LIMIT));
  }
}
