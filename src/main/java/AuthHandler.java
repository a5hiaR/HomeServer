import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import java.sql.*;

public class AuthHandler implements HttpHandler {
    private final AuthService authService;
    private final Logger logger;

    public AuthHandler(AuthService authService, Logger logger) {
        this.authService = authService;
        this.logger = logger;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException{
        try {
            String clientIP = exchange.getRemoteAddress().getAddress().getHostAddress();
            String path = exchange.getRequestURI().getPath();
            
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                if (path.equals("/api/auth/admin")) {
                    try {
                    handleAdminLogin(exchange, clientIP);
                    } catch (SQLException e) {
                        ServerUtils.logException(logger, e);
                    }
                } else {
                    ServerUtils.sendErrorResponse(exchange, 404, "Not Found", logger);
                }
            } else {
                ServerUtils.sendErrorResponse(exchange, 405, "Method Not Allowed", logger);
            }
        } finally {
            exchange.close();
        }
    }

    private void handleAdminLogin(HttpExchange exchange, String clientIP) throws IOException, SQLException {
        String body = new String(exchange.getRequestBody().readAllBytes());
        Map<String, String> loginData = ServerUtils.parseUrlEncoded(body);
        String usr = loginData.get("usr");
        String pswd = loginData.get("pswd");

        String token = authService.login(usr, pswd);

        if (token != null) {
            HashMap<String, String> logData = new HashMap<>();
            logData.put("client_ip", clientIP);
            logData.put("user", usr);
            logData.put("message", "Successful Admin Login");
            logger.log("INFO", logData);

            String cookie = "sessionToken=" + token + "; Path=/admin; HttpOnly; SameSite=Strict";
            ServerUtils.sendRedirectResponse(exchange, "/admin/index.html", cookie, logger);
        } else {
            String jsonResponse = "{\"login\":0, \"message\":\"Invalid credentials\"}";
            ServerUtils.sendJsonResponse(exchange, 401, jsonResponse, logger);
        }
    }

    public String checkTokenFromCookie(String cookieHeader) throws SQLException {
        if (cookieHeader == null || cookieHeader.isEmpty()) {
            return null;
        }
        Map<String, String> cookies = ServerUtils.parseCookie(cookieHeader);
        String token = cookies.get("sessionToken");
        return authService.getUserForToken(token);
    }
}