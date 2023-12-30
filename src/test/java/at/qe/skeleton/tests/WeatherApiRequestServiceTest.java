package at.qe.skeleton.tests;

import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import at.qe.skeleton.external.services.WeatherApiRequestService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.Validator;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import org.junit.jupiter.api.*;

import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;

import org.springframework.test.web.client.MockRestServiceServer;

import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import java.io.*;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class WeatherApiRequestServiceTest {

  @BeforeAll
  public static void Setup() throws IOException {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();

    // writing weather api response .json to string
    ClassPathResource classPathResourceUmlaut = new ClassPathResource("WeatherApiResponse.json");
    JsonNode jsonNodeWoergl = mapper.readTree(classPathResourceUmlaut.getInputStream());
    weatherApiResponseString = mapper.writeValueAsString(jsonNodeWoergl);

    // writing the Innsbruck json api response as String
    ClassPathResource classPathResourceIbk =
        new ClassPathResource("GeocodingResponseInnsbruck.json");
    JsonNode jsonNodeIbk = mapper.readTree(classPathResourceIbk.getInputStream());
    apiResponseForInnsbruck = mapper.writeValueAsString(jsonNodeIbk);

    // We are using a MockRestServiceServer which can mock responses to the api calls. the api calls
    // dont really call
    // the real api since we bind the MockRestServer to them via the RestTemplate defined here.
    // since WeatherApiRequestService needs a RestClient and MockRestServiceServer needs to be bound
    // to RestTemplate,
    // we need to create a RestClient out of a restTemplate.

    // stub RestTemplate and bind it to Mockserver for mock-api-requests
    RestTemplate stubRestTemplate = new RestTemplate();
    mockRestServiceServer = MockRestServiceServer.bindTo(stubRestTemplate).build();
    // we can now create a restClient (which we need for WeatherApiRequestService) with the
    // stubRestTemplate
    // which sends all his requests to the MockRestServiceServer
    RestClient testRestClient = RestClient.create(stubRestTemplate);
    weatherApiRequestService = new WeatherApiRequestService(testRestClient);
  }

  private static final ObjectMapper mapper = new ObjectMapper();
  private static String weatherApiResponseString;
  static MockRestServiceServer mockRestServiceServer;
  static String apiResponseForInnsbruck;

  private static WeatherApiRequestService weatherApiRequestService;

  @BeforeEach
  void resetMockServer() {
    mockRestServiceServer.reset();
  }

  @Test
  public void correctUrlOfWeatherApiCall() {
    double latitude = 42.0;
    double longitude = -42.0;
    String expectedUri = "/data/3.0/onecall?lat=" + latitude + "&lon=" + longitude;

    // Set up expectations for the MockRestServiceServer
    mockRestServiceServer
        .expect(requestTo(expectedUri))
        .andRespond(withSuccess(weatherApiResponseString, MediaType.APPLICATION_JSON));

    weatherApiRequestService.retrieveCurrentAndForecastWeather(latitude, longitude);
    mockRestServiceServer.verify();
  }

  @Test
  public void testDtoCreationFromApiResponse() {
    mockRestServiceServer
        .expect(requestTo("/data/3.0/onecall?lat=37.7749&lon=-122.4194"))
        .andRespond(withSuccess(weatherApiResponseString, MediaType.APPLICATION_JSON));

    CurrentAndForecastAnswerDTO actualDTOCreationResult =
        weatherApiRequestService.retrieveCurrentAndForecastWeather(37.7749, -122.4194);

    mockRestServiceServer.verify();
    Assertions.assertEquals("America/Los_Angeles", actualDTOCreationResult.timezone());
    Assertions.assertEquals(287.29, actualDTOCreationResult.currentWeather().temperature());
    Assertions.assertEquals(
        "Rain", actualDTOCreationResult.hourlyWeather().get(0).weather().title());
  }

  @Test
  public void simulateConnectionError() {
    RestClient mockedRestClient = Mockito.mock(RestClient.class);
    WeatherApiRequestService disconnectedApiRequestService =
        new WeatherApiRequestService(mockedRestClient);
    when(mockedRestClient.get()).thenThrow(RuntimeException.class);

    Assertions.assertThrows(
        Exception.class,
        () -> disconnectedApiRequestService.retrieveCurrentAndForecastWeather(0, 0));
  }



  // the following represents another approach for testing api calls with a MockWebServer from
  // okhttp3 library.
  // that is a "fake" server, which is actually located at the mockURL (see below) and handles the
  // calls as you tell
  // it to, with the enqueue Method.

  @BeforeEach
  void initializeMockApi() throws IOException {

    mockBackEnd = new MockWebServer();
    mockBackEnd.start();
    String mockURL = String.format("http://localhost:%s", mockBackEnd.getPort());
    RestClient restClient = RestClient.builder().baseUrl(mockURL).build();
    this.modifiedWeatherAPIRequestService = new WeatherApiRequestService(restClient);
  }

  // Der Mock-Server wird tatsächlich angesteuert. Library ist: okhttp3
  public static MockWebServer mockBackEnd;
  @AfterEach
  void tearDownWebServer() throws IOException {
    mockBackEnd.shutdown();
  }
  private WeatherApiRequestService modifiedWeatherAPIRequestService;

  // tests if error 504 is recognized by call-Method
  @Test
  public void apiRequestShouldThrowError5xx() {
    mockBackEnd.enqueue(new MockResponse().setResponseCode(504));
    assertThrows(
        HttpStatusCodeException.class,
        () -> modifiedWeatherAPIRequestService.retrieveCurrentAndForecastWeather(99, 99));
  }

  @Test
  public void apiRequestShouldThrowError4xx() {
    mockBackEnd.enqueue(new MockResponse().setResponseCode(404));
    assertThrows(
        HttpStatusCodeException.class,
        () -> modifiedWeatherAPIRequestService.retrieveCurrentAndForecastWeather(99, 99));
  }

  @Test
  public void inputValidationTest() {
    mockBackEnd.enqueue(
        new MockResponse()
            .setBody(weatherApiResponseString)
            .addHeader("Content-Type", "application/json"));

    double outOfBoundsLon = -91.0;
    double lat = 0.0;

    Assertions.assertThrows(AssertionError.class, () -> weatherApiRequestService.retrieveCurrentAndForecastWeather(outOfBoundsLon, lat));
  }
}
