package uk.gov.companieshouse.insolvency.data.converter;

import static uk.gov.companieshouse.insolvency.data.InsolvencyDataApiApplication.NAMESPACE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.lang.NonNull;
import uk.gov.companieshouse.api.insolvency.CompanyInsolvency;
import uk.gov.companieshouse.insolvency.data.exceptions.InternalServerErrorException;
import uk.gov.companieshouse.insolvency.data.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@ReadingConverter
public class CompanyInsolvencyReadConverter implements Converter<Document, CompanyInsolvency> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    private final ObjectMapper objectMapper;

    public CompanyInsolvencyReadConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public CompanyInsolvency convert(@NonNull Document source) {
        try {
            return objectMapper.readValue(source.toJson(), CompanyInsolvency.class);
        } catch (JsonProcessingException ex) {
            LOGGER.info("Failed to convert document source to CompanyInsolvency", DataMapHolder.getLogMap());
            throw new InternalServerErrorException("Failed to convert document source to CompanyInsolvency", ex);
        }
    }

}
