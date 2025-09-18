import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ServerUtils {

    public static Map<String, String> parseUrlEncoded(String str) {
        Map<String, String> parsed = new HashMap<>();
        if (str == null || str.isEmpty()) return parsed;

        String[] pairs = str.split("&");

        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                parsed.put(key, value);
            } else if (keyValue.length == 1) {
                String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                parsed.put(key, "");
            }
        }
        return parsed;
    }

    public static Map<String, String> parseCookie(String str) {
        Map<String, String> parsedCookie = new HashMap<>();
        if (str == null) return parsedCookie;

        String[] pairs = str.split("; ");

        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                parsedCookie.put(keyValue[0], keyValue[1]);
            } else if (keyValue.length == 1) {
                parsedCookie.put(keyValue[0], "");
            }
        }
        return parsedCookie;
    }

    public static String getContentType(String path) {
        if (path.endsWith(".html")) return "text/html";
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".js")) return "application/javascript";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        return "application/octet-stream";
    }

    public static void sendJsonResponse(HttpExchange exchange, int status, String json, Logger logger) throws IOException {
        String clientIP = exchange.getRemoteAddress().getAddress().getHostAddress();
        try {
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            byte[] response = json.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, response.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        } catch (Exception exception) {
            logException(logger, exception);
        } finally {
            logResponse(logger, clientIP, status, exchange.getRequestURI().getPath(), json);
        }
    }

    public static void sendFileResponse(HttpExchange exchange, int status, String path, Logger logger) throws IOException {
        String clientIP = exchange.getRemoteAddress().getAddress().getHostAddress();
        String resourcePath = "webroot" + path;
        try (InputStream resourceStream = ServerUtils.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (resourceStream == null) {
                sendErrorResponse(exchange, 404, "Not Found", logger);
                return;
            }

            exchange.getResponseHeaders().set("Content-Type", getContentType(path));
            exchange.sendResponseHeaders(status, 0);

            try (OutputStream os = exchange.getResponseBody()) {
                resourceStream.transferTo(os);
            }
            logResponse(logger, clientIP, status, path, "File sent: " + path);
        } catch (Exception exception) {
            logException(logger, exception);
        }
    }

    public static void sendErrorResponse(HttpExchange exchange, int errorCode, String message, Logger logger) throws IOException {
        String json = String.format("{\"error\":\"%s\", \"code\":%d}", message, errorCode);
        sendJsonResponse(exchange, errorCode, json, logger);
    }

    public static void sendRedirectResponse(HttpExchange exchange, String location, String cookie, Logger logger) throws IOException {
        exchange.getResponseHeaders().add("Location", location);
        if (cookie != null) {
            exchange.getResponseHeaders().add("Set-Cookie", cookie);
        }
        exchange.sendResponseHeaders(303, -1);
        logResponse(logger, exchange.getRemoteAddress().getAddress().getHostAddress(), 303, exchange.getRequestURI().getPath(), "Redirect to " + location);
    }

    public static void logResponse(Logger logger, String clientIP, int status, String path, String message) {
        if (logger == null) return;
        HashMap<String, String> logData = new HashMap<>();
        logData.put("client_ip", clientIP);
        logData.put("status", String.valueOf(status));
        logData.put("path", path);
        logData.put("message", message);
        String level = (status >= 400) ? "WARN" : "INFO";
        logger.log(level, logData);
    }

    public static void logException(Logger logger, Exception exception) {
        if (logger == null) return;
        HashMap<String, String> logData = new HashMap<>();
        logData.put("exception", exception.toString());
        logger.log("ERROR", logData);
    }

    public static void logEvent(Logger logger, String clientIP, String event) {
        if (logger == null) return;
        HashMap<String, String> logData = new HashMap<>();
        if(clientIP!=null) logData.put("client_ip", clientIP);
        logData.put("message", event);
        logger.log("INFO", logData);
    }
}
