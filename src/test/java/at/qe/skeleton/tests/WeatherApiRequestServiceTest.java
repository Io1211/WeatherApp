package at.qe.skeleton.tests;

import at.qe.skeleton.configs.ApiConfiguration;
import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import at.qe.skeleton.external.model.currentandforecast.misc.CurrentWeatherDTO;
import at.qe.skeleton.external.services.WeatherApiRequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.Validator;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Before;
import org.junit.jupiter.api.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.ExecutingResponseCreator;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;


class WeatherApiRequestServiceTest {


    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;


    //todo: @before und beforeEach und so weiter refactoren.
    @BeforeEach
    public void setup() {
        this.restTemplate = new RestTemplate();
        this.mockServer = MockRestServiceServer.bindTo(restTemplate).build();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void initialize() throws IOException{
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
        mockURL = String.format("http://localhost:%s",
                mockBackEnd.getPort());
        this.restClient = RestClient.builder()
                .baseUrl(mockURL)
                .build();
        this.weatherApiRequestService = new WeatherApiRequestService(restClient);
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


    //todo: positive Test Case
    @Test
    public void testDtoCreation() {
        String responseBody = loadMockResponseFromFile("WeatherApiResponse.json");

        this.mockServer.expect(MockRestRequestMatchers.requestTo("/data/3.0/onecall?lat=37.7749&lon=-122.4194"))
                .andRespond(MockRestResponseCreators.withSuccess(responseBody, MediaType.APPLICATION_JSON));

        CurrentAndForecastAnswerDTO actualDTOCreationResult =
                this.restTemplate.getForObject("/data/3.0/onecall?lat={lat}&lon={lon}", CurrentAndForecastAnswerDTO.class, 37.7749, -122.4194);


        this.mockServer.verify();
        Assertions.assertEquals("America/Los_Angeles", actualDTOCreationResult.timezone());
        Assertions.assertEquals(287.29, actualDTOCreationResult.currentWeather().temperature());
        Assertions.assertEquals("Rain", actualDTOCreationResult.hourlyWeather().get(0).weather().title());
    }


    // Es folgen einige Setup Schritte zur Erstellung und Aufruf eines Mock-Servers
    // Der Mock-Server wird tatsächlich angesteuert. Library ist: okhttp3
    public static MockWebServer mockBackEnd;


    @AfterEach
    void tearDownWebServer() throws IOException {
        mockBackEnd.shutdown();
    }

    private RestClient restClient;
    private WeatherApiRequestService weatherApiRequestService;
    private String mockURL;



    // Tests the retrieveCurrentAndForecastWeather Method with a call to the mockBackEnd Server, which answers with
    // WeatherApiResponse.json as Body.
    @Test
    public void retrieveCurrentAndForecastWeatherTest() throws Exception {
        mockBackEnd.enqueue(new MockResponse().setBody(loadMockResponseFromFile("WeatherApiResponse.json"))
                .addHeader("Content-Type", "application/json"));

        CurrentAndForecastAnswerDTO mockedAnswerDto = weatherApiRequestService
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
                () -> weatherApiRequestService.retrieveCurrentAndForecastWeather(99, 99));
    }

    @Test
    public void apiRequestShouldThrowError4xx() {
        mockBackEnd.enqueue(new MockResponse().setResponseCode(404));
        assertThrows(HttpStatusCodeException.class,
                () -> weatherApiRequestService.retrieveCurrentAndForecastWeather(99, 99));
    }


    private Validator validator;
    @Test
    public void inputValidationTest() throws Exception {
        mockBackEnd.enqueue(new MockResponse().setBody(loadMockResponseFromFile("WeatherApiResponse.json"))
                .addHeader("Content-Type", "application/json"));

        Method retrieveCurrentAndForecastWeather = WeatherApiRequestService.class.getMethod("retrieveCurrentAndForecastWeather", double.class, double.class);

        Double[] parameterValues = {-91.0, 0.0};

        Set<ConstraintViolation<WeatherApiRequestService>> violations = validator.forExecutables().validateParameters(
                weatherApiRequestService, retrieveCurrentAndForecastWeather, parameterValues);

        assertFalse(violations.isEmpty());

    }


    //todo: Network/Connection Error Test Case
    //Simulate a network or connection error (e.g., using a mock that throws an exception).
    // This verifies that your method handles network-related issues appropriately.


}