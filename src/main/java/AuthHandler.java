import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import java.util.HashMap;
import java.util.Map;

public class AuthHandler implements HttpHandler {
    private final AuthService authService;
    private final Logger logger;

    public AuthHandler(AuthService authService, Logger logger) {
        this.authService = authService;
        this.logger = logger;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String clientIP = exchange.getRemoteAddress().getAddress().getHostAddress();
            String path = exchange.getRequestURI().getPath();
            
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                if (path.equals("/api/auth/admin")) {
                    handleAdminLogin(exchange, clientIP);
                } else {
                    sendResponse(exchange, 404, "{\"error\":\"Not Found\"}", clientIP);
                }
            } else {
                sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}", clientIP);
            }
        } finally {
            exchange.close();
        }
    }

    private void handleAdminLogin(HttpExchange exchange, String clientIP) throws IOException {
        HashMap<String, String> logData = new HashMap<>();
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> loginData = parseUrlEncoded(body);
        String usr = loginData.get("usr");
        String pswd = loginData.get("pswd");

        String token = authService.login(usr, pswd);

        if (token != null) {
            logData.put("client_ip", clientIP);
            logData.put("user", usr);
            logData.put("message", "Successful Admin Login");
            logger.log("INFO", logData);

            exchange.getResponseHeaders().add("Set-Cookie", "sessionToken=" + token + "; Path=/admin; HttpOnly; SameSite=Strict");
            exchange.getResponseHeaders().add("Location", "/admin/index.html");
            exchange.sendResponseHeaders(303, -1);
        } else {
            logData.put("client_ip", clientIP);
            logData.put("user", usr);
            logData.put("message", "Failed admin login");
            logger.log("WARN", logData); // Use WARN for failed security events

            // 401 Unauthorized is more appropriate for failed login
            sendResponse(exchange, 401, "{\"login\":0, \"message\":\"Invalid credentials\"}", clientIP);
        }
    }

    public String checkTokenFromCookie(String cookieHeader) {
        if (cookieHeader == null || cookieHeader.isEmpty()) {
            return null;
        }
        Map<String, String> cookies = parseCookie(cookieHeader);
        String token = cookies.get("sessionToken");
        return authService.getUserForToken(token);
    }

     private Map<String, String> parseCookie(String str) {
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

    private Map<String, String> parseUrlEncoded(String str) {
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

    private void sendResponse(HttpExchange exchange, int status, String json, String clientIP) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] response = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, response.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        } catch (Exception exception) {
            HashMap<String, String> logData = new HashMap<>();
            logData.put("client_ip", clientIP);
            logData.put("exception", exception.toString());
            logger.log("ERROR", logData);
        }
        
        // Log the response that was sent
        HashMap<String, String> logData = new HashMap<>();
        logData.put("client_ip", clientIP);
        logData.put("status", String.valueOf(status));
        logData.put("message", json);
        logger.log("INFO", logData);
    }
}