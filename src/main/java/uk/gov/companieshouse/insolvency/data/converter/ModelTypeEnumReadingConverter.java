package uk.gov.companieshouse.insolvency.data.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.insolvency.ModelCase;

@Component
@ReadingConverter
public class ModelTypeEnumReadingConverter implements Converter<String, ModelCase.TypeEnum> {

    @Override
    public ModelCase.TypeEnum convert(String source) {
        return ModelCase.TypeEnum.fromValue(source);
    }
}
