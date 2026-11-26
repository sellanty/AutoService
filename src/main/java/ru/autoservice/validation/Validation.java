package ru.autoservice.validation;

public final class Validation {
    private Validation() {}

    public static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + ": значение не может быть пустым");
        }
        return value.trim();
    }
}
