package uk.gov.companieshouse.insolvency.data.converter;

import java.util.Set;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.lang.NonNull;

public class EnumConverters {

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
                throw new IllegalArgumentException("Unexpected Enum " + targetType);
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
                return ((Enum<?>) source).name();
            }
        }
    }
}
