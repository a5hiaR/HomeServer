import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthHandler implements HttpHandler {
    private String clientIP;
    private HttpExchange exchange;
    private static Map<String, String> sessions = new HashMap<>();
    private Logger logger;

    public AuthHandler(Logger logger) {
        this.logger = logger;
    }

    public void handle(HttpExchange exchange) throws IOException {
        HashMap<String, String> logData = new HashMap<>();
        this.exchange = exchange;
        this.clientIP = this.exchange.getRemoteAddress().getAddress().getHostAddress();

        String body = new String(this.exchange.getRequestBody().readAllBytes());
        String path = this.exchange.getRequestURI().getPath();
        
        if ("POST".equalsIgnoreCase(this.exchange.getRequestMethod())) {
            if (path.equals("/api/auth/admin")) {
                String usr = parseLogin(body).get("usr");
                String pswd = parseLogin(body).get("pswd");
            

                if ("admin".equals(usr)&&"test".equals(pswd)) {
                    String tkn = UUID.randomUUID().toString();
                    sessions.put(tkn, usr);

                    logData.clear();
                    logData.put("client_ip", this.clientIP);
                    logData.put("user", usr);
                    logData.put("message", "Successful Admin Login");
                    logger.log("INFO", logData);

                    exchange.getResponseHeaders().add("Set-Cookie", "sessionToken=" + tkn + "; Path=/admin; HttpOnly; SameSite=Strict");
                    exchange.getResponseHeaders().add("Location", "/admin/index.html");
                    exchange.sendResponseHeaders(303, -1);
                } else {
                    logData.clear();
                    logData.put("client_ip", this.clientIP);
                    logData.put("user", usr);
                    logData.put("message", "Failed admin login");
                    logger.log("INFO", logData);

                    sendResponse(503, "{\"login\":0}");
                }
            }
        }
    }

    public String checkTokenFromCookie(String cookie) throws IOException {
        return checkToken(parseCookie(cookie).get("sessionToken"));
    }

    public String checkToken(String tkn) {
        if (sessions.containsKey(tkn)) {
            return sessions.get(tkn);
        } else {
            return null;
        }
    }

     private Map<String, String> parseCookie(String str) throws IOException {
        Map<String, String> parsedCookie = new HashMap<>();

        String[] pairs = str.split("; ");
    
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            String key = keyValue[0];
            String value = keyValue.length > 1 ? keyValue[1] : "";
            parsedCookie.put(key, value);
        }

        return parsedCookie;
    }

    private Map<String, String> parseLogin(String str) throws IOException {
        Map<String, String> parsedLogin = new HashMap<>();

        String[] pairs = str.split("&");
    
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            String key = keyValue[0];
            String value = keyValue.length > 1 ? keyValue[1] : "";
            parsedLogin.put(key, value);
        }

        return parsedLogin;
    }

    private void sendResponse(int status, String json) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] response = json.getBytes();
        exchange.sendResponseHeaders(status, response.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
            HashMap<String, String> logData = new HashMap<>();

            logData.clear();
            logData.put("client_ip", clientIP);
            logData.put("status", String.valueOf(status));
            logData.put("message", json);
            logger.log("INFO", logData);
        } catch (Exception exception) {
            HashMap<String, String> logData = new HashMap<>();

            logData.clear();
            logData.put("client_ip", clientIP);
            logData.put("exception", exception.toString());
            logger.log("ERROR", logData);
        }
    }
}