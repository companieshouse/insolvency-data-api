package uk.gov.companieshouse.insolvency.data.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import uk.gov.companieshouse.api.insolvency.CompanyInsolvency;

@ReadingConverter
public class CompanyInsolvencyReadConverter implements Converter<Document, CompanyInsolvency> {

    private final ObjectMapper objectMapper;

    public CompanyInsolvencyReadConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public CompanyInsolvency convert(Document source) {
        try {
            return objectMapper.readValue(source.toJson(), CompanyInsolvency.class);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
