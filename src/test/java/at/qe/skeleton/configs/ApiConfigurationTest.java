package at.qe.skeleton.configs;

import at.qe.skeleton.external.model.location.LocationAnswerDTO;
import at.qe.skeleton.external.services.GeocodingApiRequestService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

// had to create seperate configs package inside test-package in order to have access to
// ApiConfiguration protected
// defaultRestClient methode.
@SpringBootTest
class ApiConfigurationTest {

  @Autowired RestClient restClient;
  static String apiResponseStringWoergl;
  static String apiResponseForInnsbruck;


  private static final ObjectMapper mapper = new ObjectMapper();

  @BeforeAll
  private static void initializeTestEnvironment() throws IOException {
    // writing the Wörgl json api response as String
    ClassPathResource classPathResourceUmlaut =
        new ClassPathResource("GeocodingResponseWörgl.json");
    JsonNode jsonNodeWoergl = mapper.readTree(classPathResourceUmlaut.getInputStream());
    apiResponseStringWoergl = mapper.writeValueAsString(jsonNodeWoergl);

    // writing the Innsbruck json api response as String
    ClassPathResource classPathResourceIbk =
        new ClassPathResource("GeocodingResponseInnsbruck.json");
    JsonNode jsonNodeIbk = mapper.readTree(classPathResourceIbk.getInputStream());
    apiResponseForInnsbruck = mapper.writeValueAsString(jsonNodeIbk);
  }


  private static MockWebServer mockWebServer;



  static String mockURL;
  static String mockPasswort;
  @BeforeAll
  private static void setApiParametersForFakeApi() throws Exception{
      mockWebServer = new MockWebServer();
      mockWebServer.start();
      mockURL = String.format("http://localhost:%s",
              mockWebServer.getPort());
      mockPasswort = "someRandomPasswort";
      System.setProperty("FAKE_API_KEY", mockPasswort);
      System.setProperty("FAKE_API_URL", mockURL);
  }

  @AfterAll
  private static void shutDownMockServer() throws Exception {
      mockWebServer.shutdown();
  }


  @Test
  public void correctGeoCodingApiRequestURLWhenUsingRealRestClient() throws Exception {
    GeocodingApiRequestService geocodingApiRequestService =
        new GeocodingApiRequestService(restClient);

    mockWebServer.enqueue(new MockResponse().setBody(apiResponseForInnsbruck)
            .addHeader("Content-Type", "application/json"));


    LocationAnswerDTO actualLocationAnswerDTO =
        geocodingApiRequestService.retrieveLocationLonLat("Innsbruck");

    RecordedRequest request= mockWebServer.takeRequest();
    String actualRequestPath = request.getPath();
    String expectedRequestPath = "/geo/1.0/direct?q=Innsbruck&limit=1&units=metric&appid=%s".formatted(mockPasswort);

    assertEquals(expectedRequestPath, actualRequestPath);
  }

  @Test
  public void correctGeoCodingApiURIEncodingWhenUsingRealRestClient() throws Exception {
    GeocodingApiRequestService geocodingApiRequestService =
        new GeocodingApiRequestService(restClient);

    mockWebServer.enqueue(new MockResponse().setBody(apiResponseStringWoergl)
            .addHeader("Content-Type", "application/json"));

    String Location = "Wörgl";

    LocationAnswerDTO actualLocationAnswerDTO =
        geocodingApiRequestService.retrieveLocationLonLat(Location);

    String encodedLocation = URLEncoder.encode(Location, StandardCharsets.UTF_8);
    RecordedRequest request= mockWebServer.takeRequest();
    String actualRequestPath = request.getPath();
    String expectedRequestPath = "/geo/1.0/direct?q=" + encodedLocation + "&limit=1&units=metric&appid=" + mockPasswort;

    assertEquals(expectedRequestPath, actualRequestPath);
  }
}
