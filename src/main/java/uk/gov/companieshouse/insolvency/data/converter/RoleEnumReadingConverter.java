package uk.gov.companieshouse.insolvency.data.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.insolvency.CaseDates;
import uk.gov.companieshouse.api.insolvency.Practitioners;

@Component
@ReadingConverter
public class RoleEnumReadingConverter implements Converter<String, Practitioners.RoleEnum> {

    @Override
    public Practitioners.RoleEnum convert(String source) {
        return Practitioners.RoleEnum.fromValue(source);
    }
}
