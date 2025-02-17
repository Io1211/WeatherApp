package at.qe.skeleton.external.model.location;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * This class is part of the Weather App project by Group g8t2 in Software Architecture. <br>
 * <br>
 * This class is used to model the answer of the Geocoding API Call
 *
 * @param name of the location
 * @param latitude in deg
 * @param longitude in deg
 * @param country in which the location is located
 * @param state in which the location is located
 * @see <a href="https://openweathermap.org/api/geocoding-api">API Documentation</a>
 */
public record LocationAnswerDTO(
    @JsonProperty("name") String name,
    @JsonIgnore @JsonProperty("local_names") Map<String, String> localNames,
    @JsonProperty("lat") double latitude,
    @JsonProperty("lon") double longitude,
    @JsonProperty("country") String country,
    @JsonProperty("state") String state)
    implements Serializable {

  @Serial private static final long serialVersionUID = 1;
}
