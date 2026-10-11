package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public final class Handlers {

    private Handlers() {}

    /** GET /health -> {"status":"ok"} */
    public static void health(HttpExchange ex) throws IOException {
        if (!"GET".equals(ex.getRequestMethod())) {
            sendJson(ex, 405, Json.object(Map.of("error", "Method not allowed")));
            return;
        }
        sendJson(ex, 200, Json.object(Map.of("status", "ok")));
    }

    /** GET /api/hello?name=Sam -> {"message":"Hello, Sam!"} */
    public static void hello(HttpExchange ex) throws IOException {
        if (!"GET".equals(ex.getRequestMethod())) {
            sendJson(ex, 405, Json.object(Map.of("error", "Method not allowed")));
            return;
        }
        String name = queryParam(ex.getRequestURI().getRawQuery(), "name");
        if (name == null || name.isBlank()) name = "World";
        sendJson(ex, 200, Json.object(Map.of("message", "Hello, " + name + "!")));
    }

    /**
     * GET  /api/items -> JSON array of items
     * POST /api/items -> add the plain-text request body as a new item
     * (stored in memory, so items reset when the server restarts)
     */
    public static class ItemsHandler implements HttpHandler {
        private final List<String> items = new CopyOnWriteArrayList<>();

        @Override
        public void handle(HttpExchange ex) throws IOException {
            switch (ex.getRequestMethod()) {
                case "GET" -> sendJson(ex, 200, Json.array(items));
                case "POST" -> {
                    String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8).trim();
                    if (body.isEmpty()) {
                        sendJson(ex, 400, Json.object(Map.of("error", "Request body is empty")));
                        return;
                    }
                    items.add(body);
                    Map<String, String> res = new LinkedHashMap<>();
                    res.put("added", body);
                    res.put("total", String.valueOf(items.size()));
                    sendJson(ex, 201, Json.object(res));
                }
                default -> sendJson(ex, 405, Json.object(Map.of("error", "Method not allowed")));
            }
        }
    }

    // ---- helpers ----

    private static String queryParam(String rawQuery, String key) {
        if (rawQuery == null) return null;
        for (String pair : rawQuery.split("&")) {
            int idx = pair.indexOf('=');
            String k = idx >= 0 ? pair.substring(0, idx) : pair;
            if (k.equals(key)) {
                String v = idx >= 0 ? pair.substring(idx + 1) : "";
                return URLDecoder.decode(v, StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    private static void sendJson(HttpExchange ex, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }
}
