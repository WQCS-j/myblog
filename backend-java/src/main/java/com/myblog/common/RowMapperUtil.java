package com.myblog.common;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public final class RowMapperUtil {
    private RowMapperUtil() {}

    public static Map<String, Object> camel(Map<String, Object> row) {
        Map<String, Object> result = new LinkedHashMap<>();
        row.forEach((key, value) -> result.put(toCamel(key), normalize(value)));
        return result;
    }

    private static String toCamel(String key) {
        StringBuilder result = new StringBuilder();
        boolean upper = false;
        for (char character : key.toCharArray()) {
            if (character == '_') { upper = true; continue; }
            result.append(upper ? Character.toUpperCase(character) : character);
            upper = false;
        }
        return result.toString();
    }

    private static Object normalize(Object value) {
        if (value instanceof Timestamp timestamp) return timestamp.toLocalDateTime();
        if (value instanceof java.sql.Date date) return date.toLocalDate();
        return value;
    }
}
