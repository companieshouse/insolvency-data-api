package uk.gov.companieshouse.insolvency.data.converter;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import org.springframework.core.convert.converter.Converter;

public class OffsetDateTimeReader implements Converter<Date, OffsetDateTime> {

    @Override
    public OffsetDateTime convert(final Date date) {
        OffsetDateTime offsetDateTime = date.toInstant()
                .atOffset(ZoneOffset.UTC);
        return offsetDateTime;
    }

}
