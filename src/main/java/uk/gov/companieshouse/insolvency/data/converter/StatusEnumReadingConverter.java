package uk.gov.companieshouse.insolvency.data.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.insolvency.CompanyInsolvency;
import uk.gov.companieshouse.api.insolvency.Practitioners;

@Component
@ReadingConverter
public class StatusEnumReadingConverter implements Converter<String, CompanyInsolvency.StatusEnum> {

    @Override
    public CompanyInsolvency.StatusEnum convert(String source) {
        return CompanyInsolvency.StatusEnum.fromValue(source);
    }
}
