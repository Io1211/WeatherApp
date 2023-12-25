package at.qe.skeleton.tests;

import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import at.qe.skeleton.external.services.WeatherApiRequestService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.Validator;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import org.junit.jupiter.api.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;

import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.client.MockRestServiceServer;

import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.lang.reflect.Method;
import java.net.SocketException;
import java.net.http.HttpConnectTimeoutException;
import java.nio.charset.StandardCharsets;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;


class WeatherApiRequestServiceTest {

    @BeforeAll
    public static void validatorSetup() {

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

    }

    //reads the corresponding (json) file into a string.
    private String loadMockResponseFromFile(String filePath) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            // Read the file content
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load JSON file: " + filePath, e);
        }
    }


    @Test
    public void correctUrlOfWeatherApiCall() throws Exception {
        double latitude = 42.0;
        double longitude = -42.0;

        // We are using a MockRestServiceServer which can mock responses to the api calls. the api calls dont really call
        // the real api since we bind the MockRestServer to them via the RestTemplate defined here.
        // since WeatherApiRequestService needs a RestClient and MockRestServiceServer needs to be bound to RestTemplate,
        // we need to create a RestClient out of a restTemplate.
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(restTemplate).build();
        RestClient testRestClient = RestClient.create(restTemplate);

        WeatherApiRequestService testApiService = new WeatherApiRequestService(testRestClient);

        // Set up expectations for the MockRestServiceServer
        mockServer.expect(requestTo("/data/3.0/onecall?lat=42.0&lon=-42.0"))
                .andRespond(withSuccess(loadMockResponseFromFile("WeatherApiResponse.json"), MediaType.APPLICATION_JSON));

        testApiService.retrieveCurrentAndForecastWeather(latitude, longitude);
        mockServer.verify();
    }

    @Test
    public void testDtoCreation() {
        String responseBody = loadMockResponseFromFile("WeatherApiResponse.json");

        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(restTemplate).build();
        //since WeatherApiRequestService needs a RestClient and MockRestServiceServer needs to be bound to RestTemplate,
        // we need to create a RestClient out of a restTemplate.
        RestClient testRestClient = RestClient.create(restTemplate);

        WeatherApiRequestService testApiService = new WeatherApiRequestService(testRestClient);

        mockServer.expect(requestTo("/data/3.0/onecall?lat=37.7749&lon=-122.4194"))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        CurrentAndForecastAnswerDTO actualDTOCreationResult =
                testApiService.retrieveCurrentAndForecastWeather(37.7749, -122.4194);

        mockServer.verify();
        Assertions.assertEquals("America/Los_Angeles", actualDTOCreationResult.timezone());
        Assertions.assertEquals(287.29, actualDTOCreationResult.currentWeather().temperature());
        Assertions.assertEquals("Rain", actualDTOCreationResult.hourlyWeather().get(0).weather().title());
    }



    @Test
    public void simulateConnectionError(){

        RestClient mockedRestClient = Mockito.mock(RestClient.class);
        WeatherApiRequestService disconnectedApiRequestService = new WeatherApiRequestService(mockedRestClient);
        when(mockedRestClient.get()).thenThrow(RuntimeException.class);

        Assertions.assertThrows(Exception.class,
                () -> disconnectedApiRequestService.retrieveCurrentAndForecastWeather(0,0));
    }





    // the following represents another approach for testing api calls with a MockWebServer from okhttp3 library.
    // that is a "fake" server, which is actually located at the mockURL (see below) and handles the calls as you tell
    // it to, with the enqueue Method.

    @BeforeEach
    void initializeMockApi() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
        String mockURL = String.format("http://localhost:%s",
                mockBackEnd.getPort());
        RestClient restClient = RestClient.builder()
                .baseUrl(mockURL)
                .build();
        this.modifiedWeatherAPIRequestService = new WeatherApiRequestService(restClient);
    }

    // Der Mock-Server wird tatsächlich angesteuert. Library ist: okhttp3
    public static MockWebServer mockBackEnd;
    @AfterEach
    void tearDownWebServer() throws IOException {
        mockBackEnd.shutdown();
    }

    private WeatherApiRequestService modifiedWeatherAPIRequestService;


    // Tests the retrieveCurrentAndForecastWeather Method with a call to the mockBackEnd Server, which answers with
    // WeatherApiResponse.json as Body.
    @Test
    public void retrieveCurrentAndForecastWeatherTest() throws Exception {
        mockBackEnd.enqueue(new MockResponse().setBody(loadMockResponseFromFile("WeatherApiResponse.json"))
                .addHeader("Content-Type", "application/json"));

        CurrentAndForecastAnswerDTO mockedAnswerDto = modifiedWeatherAPIRequestService
                .retrieveCurrentAndForecastWeather(37.7749, -122.4194);

        Assertions.assertEquals("America/Los_Angeles", mockedAnswerDto.timezone());
        Assertions.assertEquals(287.29, mockedAnswerDto.currentWeather().temperature());
        Assertions.assertEquals("Rain", mockedAnswerDto.hourlyWeather().get(0).weather().title());
    }


    //tests if error 504 is recognized by call-Method
    @Test
    public void apiRequestShouldThrowError5xx() {
        mockBackEnd.enqueue(new MockResponse().setResponseCode(504));
        assertThrows(HttpStatusCodeException.class,
                () -> modifiedWeatherAPIRequestService.retrieveCurrentAndForecastWeather(99, 99));
    }

    @Test
    public void apiRequestShouldThrowError4xx() {
        mockBackEnd.enqueue(new MockResponse().setResponseCode(404));
        assertThrows(HttpStatusCodeException.class,
                () -> modifiedWeatherAPIRequestService.retrieveCurrentAndForecastWeather(99, 99));
    }


    private static Validator validator;

    @Test
    public void inputValidationTest() throws Exception {
        mockBackEnd.enqueue(new MockResponse().setBody(loadMockResponseFromFile("WeatherApiResponse.json"))
                .addHeader("Content-Type", "application/json"));

        Method retrieveCurrentAndForecastWeather = WeatherApiRequestService.class.getMethod("retrieveCurrentAndForecastWeather", double.class, double.class);

        Double[] parameterValues = {-91.0, 0.0};

        Set<ConstraintViolation<WeatherApiRequestService>> violations = validator.forExecutables().validateParameters(
                modifiedWeatherAPIRequestService, retrieveCurrentAndForecastWeather, parameterValues);

        assertFalse(violations.isEmpty());

    }

}




