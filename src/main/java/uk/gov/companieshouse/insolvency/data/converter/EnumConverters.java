package uk.gov.companieshouse.insolvency.data.converter;

import static uk.gov.companieshouse.insolvency.data.InsolvencyDataApiApplication.NAMESPACE;

import java.util.Set;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.lang.NonNull;
import uk.gov.companieshouse.insolvency.data.exceptions.InternalServerErrorException;
import uk.gov.companieshouse.insolvency.data.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

public class EnumConverters {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    private EnumConverters() {

    }

    @ReadingConverter
    public static class StringToEnum implements GenericConverter {

        @Override
        public Set<ConvertiblePair> getConvertibleTypes() {
            return Set.of(new ConvertiblePair(String.class, Enum.class));
        }

        @Override
        public Object convert(Object source, @NonNull TypeDescriptor sourceType, @NonNull TypeDescriptor targetType) {
            try {
                return targetType.getType().getDeclaredMethod("fromValue", String.class).invoke(null, source);
            } catch (Exception ex) {
                final String msg = "Unexpected Enum: %s".formatted(targetType);
                LOGGER.info(msg, DataMapHolder.getLogMap());
                throw new InternalServerErrorException(msg, ex);
            }
        }
    }

    @WritingConverter
    public static class EnumToString implements GenericConverter {

        @Override
        public Set<ConvertiblePair> getConvertibleTypes() {
            return Set.of(new ConvertiblePair(Enum.class, String.class));
        }

        @Override
        public Object convert(Object source, @NonNull TypeDescriptor sourceType, @NonNull TypeDescriptor targetType) {
            try {
                return sourceType.getType().getDeclaredMethod("getValue", null)
                        .invoke(source, null);
            } catch (Exception ex) {
                LOGGER.info("Exception in EnumConverter during write conversion", DataMapHolder.getLogMap());
                return ((Enum<?>) source).name();
            }
        }
    }
}
