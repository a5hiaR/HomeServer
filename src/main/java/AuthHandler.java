import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.json.JSONObject;

import java.util.HashMap;

public class AuthHandler() implements HttpHandler{
    private String clientIP;
    private HttpExchange exchange;

    public void handle(HttpExchange exchange) {
        this.exchange = exchange;

        if ("POST".equalsIgnoreCase(this.exchange.getRequestMethod())) {
            String body = new String(this.exchange.getRequestBody.readAllBytes());
            this.clientIP = this.exchange.getRemoteAddress().getAddress().getHostAddress();

            String usr = parseLogin(body).get("usr");
            String pswd = parseLogin(body).get("pswd");

            if ("admin".equals(usr)&&"test".equals(pswd)) {
                System.out.println("Auth handle got sucessful login from "+ this.clientIP)
            }
        }
    }

    private Map<String, String> parseLogin(String str) {
        Map<String, String> parsedLogin = new HashMap<>();

        String[] pairs = formData.split("&");
    
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            String key = keyValue[0];
            String value = keyValue.length > 1 ? keyValue[1] : "";
            parsedData.put(key, value);
        }

        return parsedLogin;
    }
}