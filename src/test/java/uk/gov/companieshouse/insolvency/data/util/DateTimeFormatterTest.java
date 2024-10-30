package uk.gov.companieshouse.insolvency.data.util;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

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
        String formattedDate = DateTimeFormatter.format(LocalDate.of(2015, 6, 26));
        assertThat(formattedDate).isNotNull();
        assertThat(formattedDate).isEqualTo("2015-06-26T00:00:00Z");
    }

    @Test
    void shouldFormatPublishedAtDate() {
        // given
        Instant now = Instant.parse("2024-09-04T10:52:22.235486Z");
        final String expected = "2024-09-04T10:52:22";

        // when
        final String actual = DateTimeFormatter.formatPublishedAt(now);

        // then
        assertEquals(expected, actual);
    }
}
