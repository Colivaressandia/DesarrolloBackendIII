package com.bancoxyz.batch.support;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;

public final class LegacyDates {

    private static final List<DateTimeFormatter> FORMATS = List.of(
            formatter("uuuu-MM-dd"),
            formatter("d-M-uuuu"),
            formatter("d/M/uuuu"),
            formatter("uuuu/M/d"));

    private LegacyDates() {
    }

    public static LocalDate parse(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("fecha vacía");
        }
        for (DateTimeFormatter format : FORMATS) {
            try {
                return LocalDate.parse(value.trim(), format);
            } catch (DateTimeParseException ignored) {
                // Try the next legacy format.
            }
        }
        throw new IllegalArgumentException("formato de fecha legacy no reconocido");
    }

    private static DateTimeFormatter formatter(String pattern) {
        return new DateTimeFormatterBuilder()
                .appendPattern(pattern)
                .toFormatter()
                .withResolverStyle(ResolverStyle.STRICT);
    }
}
