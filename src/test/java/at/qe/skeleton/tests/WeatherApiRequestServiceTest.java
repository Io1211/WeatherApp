package at.qe.skeleton.tests;

import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import at.qe.skeleton.external.model.currentandforecast.misc.CurrentWeatherDTO;
import at.qe.skeleton.external.services.WeatherApiRequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@Component
@SpringBootTest
class WeatherApiRequestServiceTest {

    @Autowired
    private WeatherApiRequestService weatherApiRequestService;

    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @BeforeEach
    public void setup(){
        this.restTemplate = new RestTemplate();
        this.mockServer  = MockRestServiceServer.bindTo(restTemplate).build();
    }


    @Autowired
    private ObjectMapper objectMapper;


    // Uses Jackson ObjectMapper to deserialize the JSON-file into a Java object
    private CurrentAndForecastAnswerDTO loadExpectedResponseFromFile(String filePath) {
        try {
            return objectMapper.readValue(loadMockResponseFromFile(filePath), CurrentAndForecastAnswerDTO.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
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
    public void ApiMethodShouldDeliverCorrectDTO() {
        //classpath is Spring Boot prefix to indicate that the file should be loaded from the classpath.
        String responseBody = loadMockResponseFromFile("WeatherApiResponse.json");

        this.mockServer.expect(MockRestRequestMatchers.requestTo("/data/3.0/onecall?lat=37.7749&lon=-122.4194"))
                .andRespond(MockRestResponseCreators.withSuccess(responseBody, MediaType.APPLICATION_JSON));

        CurrentAndForecastAnswerDTO actualDTOCreationResult =
                this.restTemplate.getForObject("/data/3.0/onecall?lat={lat}&lon={lon}", CurrentAndForecastAnswerDTO.class,37.7749, -122.4194);



        this.mockServer.verify();
        Assertions.assertEquals("America/Los_Angeles", actualDTOCreationResult.timezone());
        // noch mehr in die Richtung...
    }

    @Test
    public void apiRequestShouldThrowError4xx() {
        //todo: implement logic
    }

    //todo: positive Test Case

    //todo: Server Error Test Case

    //todo: Network/Connection Error Test Case
    //Simulate a network or connection error (e.g., using a mock that throws an exception).
    // This verifies that your method handles network-related issues appropriately.


    //todo: Input Validation Test Case
    //Test the method with valid and invalid input values to ensure it performs input validation correctly.

}