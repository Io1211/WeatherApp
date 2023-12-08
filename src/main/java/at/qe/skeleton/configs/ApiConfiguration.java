package at.qe.skeleton.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;

/**
 * Configuration for the weather api used in this years software architecture course.
 * <p>
 * This allows the injection of {@link RestClient} pointing to the used weather api config
 * <p>
 * This class is part of the skeleton project provided for students of the
 * course "Software Architecture" offered by Innsbruck University.
 */
@Configuration
public class ApiConfiguration {

    private static final String APIKEY_PARAMETER = "appid";
    private static final String UNITS_PARAMETER = "units";
    private static final String DEFAULT_UNITS = "metric";

    @Value("${api.key}")
    private String apiKey;

    @Value("${api.url}")
    private String baseUrl;

    private class QueryAddingRequestInterceptor implements ClientHttpRequestInterceptor {
        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
            URI uri = UriComponentsBuilder.fromUri(request.getURI())
                    .queryParam(UNITS_PARAMETER, DEFAULT_UNITS)
                    .queryParam(APIKEY_PARAMETER, apiKey)
                    .build().toUri();

            HttpRequest modifiedRequest = new HttpRequestWrapper(request) {
                @Override
                public URI getURI() {
                    return uri;
                }
            };

            return execution.execute(modifiedRequest, body);
        }
    }

    @Bean
    protected RestClient defaultRestClient() {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestInterceptor(new QueryAddingRequestInterceptor())
                .build();
    }
}
