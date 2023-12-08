package at.qe.skeleton.external.model.currentandforecast.misc;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * This class is part of the skeleton project provided for students of the
 * course "Software Architecture" offered by Innsbruck University.
 * <br><br>
 * This class is used to model the answer of an API call
 *
 * @param timestamp     UNIX timestamp in UTC, use timezone/timezone offset create {@link java.time.ZonedDateTime}
 * @param precipitation in mm/h
 * @see <a href="https://openweathermap.org/api/one-call-3#current">API Documentation</a>
 */
public record PrecipitationByTimestampDTO(@JsonProperty("dt") Instant timestamp,
                                          double precipitation
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1;

}
