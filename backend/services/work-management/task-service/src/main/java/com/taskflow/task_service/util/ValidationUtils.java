package com.taskflow.task_service.util;

import java.util.Collection;

public final class ValidationUtils {

    private ValidationUtils() {
    }

    public static boolean isBlank(
            String value
    ) {

        return value == null
                || value.trim().isEmpty();
    }

    public static boolean isNotBlank(
            String value
    ) {

        return !isBlank(value);
    }

    public static boolean hasValidLength(
            String value,
            int min,
            int max
    ) {

        if (value == null) {
            return false;
        }

        int length = value.trim().length();

        return length >= min
                && length <= max;
    }

    public static boolean isEmpty(
            Collection<?> collection
    ) {

        return collection == null
                || collection.isEmpty();
    }

    public static boolean isNotEmpty(
            Collection<?> collection
    ) {

        return !isEmpty(collection);
    }

    public static boolean isPositive(
            Double value
    ) {

        return value != null
                && value > 0;
    }

    public static boolean isNonNegative(
            Double value
    ) {

        return value != null
                && value >= 0;
    }

    public static boolean isSame(
            String first,
            String second
    ) {

        return first != null
                && first.equals(second);
    }
}