package uk.gov.companieshouse.insolvency.data.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DateTimeFormatterTest {

    @Test
    void shouldParseAndFormatGivenDateString() {
        LocalDate parsedValue = DateTimeFormatter.parse("2015-06-26T08:31:35.058Z");
        assertThat(parsedValue).isNotNull();
        assertThat(parsedValue.toString()).hasToString("2015-06-26");
    }

    @Test
    void throwExceptionWhenGivenWrongDate() {
        assertThrows(IllegalStateException.class, () -> DateTimeFormatter.parse("2015 08:31:35.058Z"));
    }

    @Test
    void shouldFormatGivenDateString() {
        String formattedDate = DateTimeFormatter.format(LocalDate.of(2015, 06, 26));
        assertThat(formattedDate).isNotNull();
        assertThat(formattedDate).isEqualTo("2015-06-26T00:00:00Z");
    }
}
