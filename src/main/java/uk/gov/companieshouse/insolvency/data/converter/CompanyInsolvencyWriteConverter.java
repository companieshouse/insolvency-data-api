package uk.gov.companieshouse.insolvency.data.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.lang.NonNull;
import uk.gov.companieshouse.api.insolvency.CompanyInsolvency;

@WritingConverter
public class CompanyInsolvencyWriteConverter implements Converter<CompanyInsolvency, DBObject> {

    private final ObjectMapper objectMapper;

    public CompanyInsolvencyWriteConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public DBObject convert(@NonNull CompanyInsolvency source) {
        try {
            return BasicDBObject.parse(objectMapper.writeValueAsString(source));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
