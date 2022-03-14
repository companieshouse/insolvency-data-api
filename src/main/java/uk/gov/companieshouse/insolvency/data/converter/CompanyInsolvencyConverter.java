package uk.gov.companieshouse.insolvency.data.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.insolvency.CompanyInsolvency;

@WritingConverter
@Component
public class CompanyInsolvencyConverter implements Converter<CompanyInsolvency, DBObject> {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public DBObject convert(CompanyInsolvency source) {
        try {
            return BasicDBObject.parse(objectMapper.writeValueAsString(source));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
