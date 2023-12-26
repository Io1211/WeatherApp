package at.qe.skeleton.tests;

import at.qe.skeleton.external.model.location.LocationAnswerDTO;
import at.qe.skeleton.external.services.GeocodingApiRequestService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;


import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GeocodingApi_LocationDTO_Test {
    //todo: write tests

    private static RestClient testRestClient;

    private static MockRestServiceServer mockServer;

    private static final ObjectMapper mapper = new ObjectMapper();

    private static String apiResponseStringUmlaut;

    private static String apiResponseStringIbk;


    @BeforeAll
    static void prepareApiTestEnvironment() throws Exception{
        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
        testRestClient = RestClient.create(restTemplate);

        ClassPathResource classPathResourceUmlaut = new ClassPathResource("GeocodingResponseWörgl.json");
        JsonNode jsonNodeUmlaut = mapper.readTree(classPathResourceUmlaut.getInputStream());
        apiResponseStringUmlaut = mapper.writeValueAsString(jsonNodeUmlaut);

        ClassPathResource classPathResourceIbk = new ClassPathResource("GeocodingResponseInnsbruck.json");
        JsonNode jsonNodeIbk = mapper.readTree(classPathResourceIbk.getInputStream());
        apiResponseStringIbk = mapper.writeValueAsString(jsonNodeIbk);

    }

    @BeforeEach
    void clearMockServerExpectations () {
        mockServer.reset();
    }

    @Test
    public void testApiCallWithoutUmlaut() throws IOException {

        mockServer.expect(requestTo("/geo/1.0/direct?q=Innsbruck&limit=1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(apiResponseStringIbk, MediaType.APPLICATION_JSON));

        // need to initialize the geocodingApiRequestService with the testRestClient
        GeocodingApiRequestService geocodingApiRequestService = new GeocodingApiRequestService();
        ReflectionTestUtils.setField(geocodingApiRequestService, "restClient", testRestClient);
        LocationAnswerDTO actualLocationAnswerDTO =
                geocodingApiRequestService.retrieveLocationLonLat("Innsbruck");

        mockServer.verify();
        Assertions.assertEquals(actualLocationAnswerDTO.name(), "Innsbruck");
        Assertions.assertEquals(actualLocationAnswerDTO.latitude(), 47.2654296);
        Assertions.assertEquals(actualLocationAnswerDTO.longitude(), 11.3927685);
        Assertions.assertEquals(actualLocationAnswerDTO.country(), "AT");
        Assertions.assertEquals(actualLocationAnswerDTO.state(), "Tyrol");
    }


    //Umlaute machen Probleme. Hier wiki Artikel: https://de.wikipedia.org/wiki/URL-Encoding
    // -> siehe im Artikel: Nicht-Ascii-Zeichen
    @Test
    public void testApiCallWithUmlaut() throws IOException {
        //Wörgl is being encoded to URL compatible message with UTF-8 as Base to W%C3%B6rgl
        mockServer.expect(requestTo("/geo/1.0/direct?q=W%C3%B6rgl&limit=1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(apiResponseStringUmlaut, MediaType.APPLICATION_JSON));

        // need to initialize the geocodingApiRequestService with the testRestClient
        GeocodingApiRequestService geocodingApiRequestService = new GeocodingApiRequestService();
        ReflectionTestUtils.setField(geocodingApiRequestService, "restClient", testRestClient);
        LocationAnswerDTO actualLocationAnswerDTO =
                geocodingApiRequestService.retrieveLocationLonLat("Wörgl");

        mockServer.verify();
        Assertions.assertEquals(actualLocationAnswerDTO.name(), "Stadt Wörgl");
        Assertions.assertEquals(actualLocationAnswerDTO.latitude(), 47.48033265);
        Assertions.assertEquals(actualLocationAnswerDTO.longitude(), 12.07928067668956);
        Assertions.assertEquals(actualLocationAnswerDTO.country(), "AT");
        Assertions.assertEquals(actualLocationAnswerDTO.state(), "Tyrol");
    }
}