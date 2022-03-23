package uk.gov.companieshouse.insolvency.data.serialization;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class LocalDateDeSerializer extends JsonDeserializer<LocalDate> {

    @Override
    public LocalDate deserialize(
            JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        JsonNode jsonNode = jsonParser.readValueAsTree();
        try {
            return LocalDate.parse(jsonNode.get("$date").textValue(), formatter);
        } catch (Exception ex) {
            //TODO: handle exception
            return null;
        }
    }
}