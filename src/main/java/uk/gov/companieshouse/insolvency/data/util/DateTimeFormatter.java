package uk.gov.companieshouse.insolvency.data.util;

import static java.time.ZoneOffset.UTC;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateTimeFormatter {

    static final String STR_PATTERN = "\\d{4}-\\d{2}-\\d{2}";
    static final Pattern pattern = Pattern.compile(STR_PATTERN);

    static java.time.format.DateTimeFormatter writeDateTimeFormatter =
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    static java.time.format.DateTimeFormatter readDateTimeFormatter =
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");

    static java.time.format.DateTimeFormatter publishedAtDateTimeFormatter =
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final java.time.format.DateTimeFormatter deltaAtFormatter =
            java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS").withZone(UTC);

    private DateTimeFormatter() {

    }

    /**
     * Parse date string to LocalDate.
     *
     * @param dateStr date as string.
     * @return parsed date.
     */
    public static LocalDate parse(String dateStr) {
        Matcher matcher = pattern.matcher(dateStr);
        matcher.find();
        return LocalDate.parse(matcher.group(), readDateTimeFormatter);
    }

    /**
     * Format date.
     *
     * @param localDate date to format.
     * @return formatted date as string.
     */
    public static String format(LocalDate localDate) {
        return localDate.atStartOfDay().format(writeDateTimeFormatter);
    }

    /**
     * Format publishedAt date
     *
     * @param now current time as Instant
     * @return UTC time as string rounded to seconds
     */
    public static String formatPublishedAt(Instant now) {
        return publishedAtDateTimeFormatter.format(now.atZone(ZoneOffset.UTC));
    }

    public static boolean isDeltaStale(final String requestDeltaAt, final OffsetDateTime existingDeltaAt) {
        return existingDeltaAt != null &&
                OffsetDateTime.parse(requestDeltaAt, deltaAtFormatter).isBefore(existingDeltaAt);
    }
}
