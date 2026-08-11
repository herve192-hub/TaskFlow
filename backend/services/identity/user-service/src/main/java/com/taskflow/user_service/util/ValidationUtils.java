// 
// 
package com.taskflow.user_service.util;

import java.util.Collection;
import java.util.regex.Pattern;

public final class ValidationUtils {

    private ValidationUtils() {
        // Utility class — prevent instantiation ...
    }

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+?[0-9]{7,15}$"
    );

    /**
     * Checks whether a string is null, empty, or contains only whitespace ...
     */
    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Checks whether a string contains a meaningful value...
     */
    public static boolean isNotBlank(String value) {
        return !isBlank(value);
    }

    /**
     * Validates an email address...
     */
    public static boolean isValidEmail(String email) {
        return isNotBlank(email) && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validates a phone number.
     *
     * Accepts international numbers such as:
     * +14155552671
     * +33612345678
     *
     * Also accepts numbers without a leading +.
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        return isNotBlank(phoneNumber)
                && PHONE_PATTERN.matcher(phoneNumber.trim()).matches();
    }

    /**
     * Checks whether a string has a length within the specified range...
     */
    public static boolean isLengthBetween(
            String value,
            int minLength,
            int maxLength) {

        if (value == null) {
            return false;
        }

        int length = value.trim().length();

        return length >= minLength && length <= maxLength;
    }

    /**
     * Checks whether a collection is null or empty...
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Checks whether a collection contains at least one element...
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    /**
     * Validates that an ID is a positive number...
     */
    public static boolean isValidId(Long id) {
        return id != null && id > 0;
    }

    /**
     * Validates a password using basic security requirements...
     *
     * Requirements:
     * - At least 8 characters
     * - At least one uppercase letter
     * - At least one lowercase letter
     * - At least one digit
     */
    public static boolean isValidPassword(String password) {

        if (isBlank(password)) {
            return false;
        }

        return password.length() >= 8
                && password.matches(".*[A-Z].*")
                && password.matches(".*[a-z].*")
                && password.matches(".*\\d.*");
    }
}