// Date utility class for handling date and time operations

package com.taskflow.user_service.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
* Utility class for handling date and time operations in TaskFlow...
* <p>All application timestamps are handled in UTC to ensure
* consistent behavior across local development, Docker, AWS,
* and distributed microservices.</p>
*/

public final class DateUtils {

    private DateUtils() {
    // Utility class - prevent instantiation
    }

    public static final String DATE_FORMAT = "yyyy-MM-dd";

    public static final String DATE_TIME_FORMAT =
    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public static final String DATE_TIME_FORMAT_WITHOUT_MILLIS =
    "yyyy-MM-dd'T'HH:mm:ss'Z'";

    public static final String TIME_ZONE = "UTC";

    private static final DateTimeFormatter DATE_FORMATTER =
    DateTimeFormatter.ofPattern(DATE_FORMAT)
    .withZone(ZoneOffset.UTC);

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
    DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)
    .withZone(ZoneOffset.UTC);

    private static final DateTimeFormatter DATE_TIME_FORMATTER_WITHOUT_MILLIS =
    DateTimeFormatter.ofPattern(DATE_TIME_FORMAT_WITHOUT_MILLIS)
    .withZone(ZoneOffset.UTC);

    /**

    * Returns the current UTC timestamp...
    *
    * @return current UTC instant
    */
    public static Instant now() {
        return Instant.now();
    }

    /**

    * Returns the current UTC date...
    *
    * @return current UTC date
    */
    public static LocalDate today() {
        return LocalDate.now(ZoneOffset.UTC);
    }

    /**

    * Formats an Instant as yyyy-MM-dd...
    *
    * @param instant timestamp to format
    * @return formatted date
    */
    public static String formatDate(Instant instant) {
        if (instant == null) {
            return null;
        }
        return DATE_FORMATTER.format(instant);
    }

    /**

    * Formats an Instant as an ISO-8601 UTC timestamp
    * with milliseconds...
    *
    * @param instant timestamp to format
    * @return formatted UTC timestamp
    */
    public static String formatDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }

        return DATE_TIME_FORMATTER.format(instant);
    }

    /**
    * Formats an Instant as an ISO-8601 UTC timestamp
    * without milliseconds...
    *
    * @param instant timestamp to format
    * @return formatted UTC timestamp
    */
    public static String formatDateTimeWithoutMillis(Instant instant) {
        if (instant == null) {
            return null;
        }

        return DATE_TIME_FORMATTER_WITHOUT_MILLIS.format(instant);
    }

    /**
    * Parses a yyyy-MM-dd date into a LocalDate...
    *
    * @param dateString date string
    * @return parsed date
    * @throws IllegalArgumentException if the date cannot be parsed
    */
    public static LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse( dateString, DateTimeFormatter.ofPattern(DATE_FORMAT) );
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException( "Invalid date format. Expected: " + DATE_FORMAT, exception);
        }
    }

    /**
    * Parses an ISO-8601 UTC timestamp with milliseconds...
    *
    * @param dateTimeString timestamp string
    * @return parsed Instant
    * @throws IllegalArgumentException if the timestamp cannot be parsed
    */
    public static Instant parseDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isBlank()) {
            return null;
        }

        try {
            return Instant.parse(dateTimeString);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException( "Invalid date-time format. Expected: " + DATE_TIME_FORMAT, exception );
        }
    }
}
