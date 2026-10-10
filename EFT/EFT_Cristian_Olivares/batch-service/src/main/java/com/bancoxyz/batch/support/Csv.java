package com.bancoxyz.batch.support;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class Csv {

    private Csv() {
    }

    public static String line(Object... values) {
        return Arrays.stream(values)
                .map(value -> escape(value == null ? "" : value.toString()))
                .collect(Collectors.joining(","));
    }

    private static String escape(String value) {
        String escaped = value.replace("\"", "\"\"");
        return escaped.contains(",") || escaped.contains("\"")
                || escaped.contains("\n") || escaped.contains("\r")
                ? "\"" + escaped + "\"" : escaped;
    }
}
