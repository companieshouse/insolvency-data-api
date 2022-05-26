package uk.gov.companieshouse.insolvency.data.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import org.apache.commons.lang.StringUtils;

public class CustomStringSerializer extends JsonSerializer<String> {

    @Override
    public void serialize(String string, JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        if (StringUtils.isBlank(string)) {
            jsonGenerator.writeNull();
        } else {
            jsonGenerator.writeString(string);
        }
    }

    @Override
    public boolean isEmpty(SerializerProvider provider, String value) {
        if (value != null) {
            String finalValue = value.trim();
            if (finalValue.isEmpty()) {
                return true;
            }
        }

        return false;
    }
}
