package at.qe.skeleton.external.model.deserialisation;

import at.qe.skeleton.external.model.shared.WeatherDTO;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

/**
 * This class is part of the skeleton project provided for students of the
 * course "Software Architecture" offered by Innsbruck University.
 * <br><br>
 * Strangely the weather structure is always wrapped in a collection in the result json.
 * This deserializer takes care of that and extracts the value of the collection into a single object
 */
public class WeatherDeserializer extends JsonDeserializer<WeatherDTO> {

    @Override
    public WeatherDTO deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode node = mapper.readTree(p);
        if (node instanceof ArrayNode arrayNode && !arrayNode.isEmpty()) {
            return mapper.treeToValue(arrayNode.get(0), WeatherDTO.class);
        } else if (node instanceof ObjectNode objectNode && !objectNode.isEmpty()) {
            return mapper.treeToValue(objectNode, WeatherDTO.class);
        }
        return null;
    }
}
