package org.example;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Tiny JSON helpers so the project needs no external dependencies. */
public final class Json {

    private Json() {}

    public static String escape(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    public static String string(String s) {
        return "\"" + escape(s) + "\"";
    }

    public static String array(List<String> items) {
        return items.stream().map(Json::string).collect(Collectors.joining(",", "[", "]"));
    }

    public static String object(Map<String, String> fields) {
        return fields.entrySet().stream()
                .map(e -> string(e.getKey()) + ":" + string(e.getValue()))
                .collect(Collectors.joining(",", "{", "}"));
    }
}
