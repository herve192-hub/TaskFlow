package com.taskflow.task_service.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class DateUtils {

    private DateUtils() {
    }

    public static final String DATE_FORMAT =
            "yyyy-MM-dd";

    public static final String DATE_TIME_FORMAT =
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter
                    .ofPattern(DATE_FORMAT)
                    .withZone(ZoneOffset.UTC);

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter
                    .ofPattern(DATE_TIME_FORMAT)
                    .withZone(ZoneOffset.UTC);

    public static Instant now() {
        return Instant.now();
    }

    public static LocalDate today() {
        return LocalDate.now(ZoneOffset.UTC);
    }

    public static String formatDate(
            Instant instant
    ) {

        if (instant == null) {
            return null;
        }

        return DATE_FORMATTER.format(instant);
    }

    public static String formatDateTime(
            Instant instant
    ) {

        if (instant == null) {
            return null;
        }

        return DATE_TIME_FORMATTER.format(instant);
    }

    public static Instant parseInstant(
            String value
    ) {

        if (value == null || value.isBlank()) {
            return null;
        }

        try {

            return Instant.parse(value);

        } catch (DateTimeParseException exception) {

            throw new IllegalArgumentException(
                    "Invalid date-time value: " + value,
                    exception
            );
        }
    }

    public static boolean isPast(
            Instant instant
    ) {

        return instant != null
                && instant.isBefore(now());
    }

    public static boolean isFuture(
            Instant instant
    ) {

        return instant != null
                && instant.isAfter(now());
    }
}