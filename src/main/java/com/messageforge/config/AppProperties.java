package com.messageforge.config;

import java.util.Arrays;

final class AppProperties {

    private AppProperties() {
    }

    static String get(String defaultValue, String... names) {
        return Arrays.stream(names)
                .map(AppProperties::lookup)
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElse(defaultValue);
    }

    static String getRequired(String description, String... names) {
        return Arrays.stream(names)
                .map(AppProperties::lookup)
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(description + " must be set using one of: "
                        + String.join(", ", names)));
    }

    static int getInt(int defaultValue, String... names) {
        return Integer.parseInt(get(String.valueOf(defaultValue), names));
    }

    static long getLong(long defaultValue, String... names) {
        return Long.parseLong(get(String.valueOf(defaultValue), names));
    }

    private static String lookup(String name) {
        String systemProperty = System.getProperty(name);
        if (systemProperty != null && !systemProperty.isBlank()) {
            return systemProperty;
        }
        return System.getenv(name);
    }
}
