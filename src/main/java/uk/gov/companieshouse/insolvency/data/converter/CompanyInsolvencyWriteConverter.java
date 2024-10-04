package uk.gov.companieshouse.insolvency.data.converter;

import static uk.gov.companieshouse.insolvency.data.InsolvencyDataApiApplication.NAMESPACE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.lang.NonNull;
import uk.gov.companieshouse.api.insolvency.CompanyInsolvency;
import uk.gov.companieshouse.insolvency.data.exceptions.InternalServerErrorException;
import uk.gov.companieshouse.insolvency.data.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@WritingConverter
public class CompanyInsolvencyWriteConverter implements Converter<CompanyInsolvency, DBObject> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    private final ObjectMapper objectMapper;

    public CompanyInsolvencyWriteConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public DBObject convert(@NonNull CompanyInsolvency source) {
        try {
            return BasicDBObject.parse(objectMapper.writeValueAsString(source));
        } catch (JsonProcessingException ex) {
            LOGGER.info("Failed to convert CompanyInsolvency into DB object", DataMapHolder.getLogMap());
            throw new InternalServerErrorException("Failed to convert CompanyInsolvency into DB object", ex);
        }
    }

}
