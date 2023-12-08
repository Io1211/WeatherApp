package at.qe.skeleton.external.model.currentandforecast.misc;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

/**
 * This class is part of the skeleton project provided for students of the
 * course "Software Architecture" offered by Innsbruck University.
 * <br><br>
 * This class is used to model the answer of an API call
 *
 * @param senderName  the publisher of the event
 * @param event       alert event name
 * @param start       timestamp: start of the event
 * @param end         timestamp: end of the event
 * @param description description of the event
 * @param tags        type of severe weather
 * @see <a href="https://openweathermap.org/api/one-call-3#current">API Documentation</a>
 */
public record AlertDTO(@JsonProperty("sender_name") String senderName,
                       String event,
                       Instant start,
                       Instant end,
                       String description,
                       List<String> tags
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1;

}
