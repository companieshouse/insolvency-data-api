package uk.gov.companieshouse.insolvency.data.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.insolvency.CompanyInsolvency;

@Component
@WritingConverter
public class CompanyInsolvencyConverter implements Converter<CompanyInsolvency, BasicDBObject> {

    private final ObjectMapper objectMapper;

    public CompanyInsolvencyConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public BasicDBObject convert(CompanyInsolvency source) {
        try {
            return BasicDBObject.parse(objectMapper.writeValueAsString(source));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
