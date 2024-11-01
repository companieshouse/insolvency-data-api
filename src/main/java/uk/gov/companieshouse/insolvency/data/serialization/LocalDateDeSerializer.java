package uk.gov.companieshouse.insolvency.data.serialization;

import static uk.gov.companieshouse.insolvency.data.InsolvencyDataApiApplication.NAMESPACE;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import uk.gov.companieshouse.insolvency.data.exceptions.InternalServerErrorException;
import uk.gov.companieshouse.insolvency.data.logging.DataMapHolder;
import uk.gov.companieshouse.insolvency.data.util.DateTimeFormatter;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

public class LocalDateDeSerializer extends JsonDeserializer<LocalDate> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    @Override
    public LocalDate deserialize(JsonParser jsonParser,
                                 DeserializationContext deserializationContext) throws IOException {
        JsonNode jsonNode = jsonParser.readValueAsTree();
        try {
            JsonNode dateJsonNode = jsonNode.get("$date");
            if (dateJsonNode == null) {
                return DateTimeFormatter.parse(jsonNode.textValue());
            } else if (dateJsonNode.isTextual()) {
                var dateStr = dateJsonNode.textValue();
                return DateTimeFormatter.parse(dateStr);
            } else {
                var longDate = dateJsonNode.get("$numberLong").asLong();
                var dateStr = Instant.ofEpochMilli(new Date(longDate).getTime()).toString();
                return DateTimeFormatter.parse(dateStr);
            }
        } catch (Exception ex) {
            final String msg = "Failed to deserialise LocalDate from JSON node: %s".formatted(jsonNode);
            LOGGER.info(msg, DataMapHolder.getLogMap());
            throw new InternalServerErrorException(msg, ex);
        }
    }
}
