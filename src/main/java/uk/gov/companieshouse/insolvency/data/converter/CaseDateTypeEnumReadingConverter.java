package uk.gov.companieshouse.insolvency.data.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.insolvency.CaseDates;
import uk.gov.companieshouse.api.insolvency.ModelCase;

@Component
@ReadingConverter
public class CaseDateTypeEnumReadingConverter implements Converter<String, CaseDates.TypeEnum> {

    @Override
    public CaseDates.TypeEnum convert(String source) {
        return CaseDates.TypeEnum.fromValue(source);
    }
}
