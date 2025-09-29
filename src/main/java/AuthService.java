import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.sql.*;

import java.io.IOException;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

public class AuthService {
    public String login(String username, String password) throws SQLException {
        String storedPassword = DatabaseService.getAdminPass(username);
        if (storedPassword != null && storedPassword.equals(password)) {
            String token = DatabaseService.getActiveSession(username);
            if (token == null) {
                token = UUID.randomUUID().toString();
                DatabaseService.newActiveSession(token, username);
            }
            return token;
        }
        return null;
    }

    public String getUserForToken(String token) throws SQLException {
        return token != null ? DatabaseService.getActiveUser(token): null;
    }

    public static String checkTokenFromCookie(String cookieHeader) throws SQLException {
        if (cookieHeader == null || cookieHeader.isEmpty()) {
            return null;
        }
        Map<String, String> cookies = ServerUtils.parseCookie(cookieHeader);
        String token = cookies.get("sessionToken");
        return AuthService.getUserForToken(token);
    }

    public static void handleAdminLogin(HttpExchange exchange, String clientIP) throws IOException, SQLException {
        String body = new String(exchange.getRequestBody().readAllBytes());
        Map<String, String> loginData = ServerUtils.parseUrlEncoded(body);
        String usr = loginData.get("usr");
        String pswd = loginData.get("pswd");

        String token = AuthService.login(usr, pswd);

        if (token != null) {
            HashMap<String, String> logData = new HashMap<>();
            logData.put("client_ip", clientIP);
            logData.put("user", usr);
            logData.put("message", "Successful Admin Login");
            logger.log("INFO", logData);

            String cookie = "sessionToken=" + token + "; Path=/; HttpOnly; SameSite=Strict";
            ServerUtils.sendRedirectResponse(exchange, "/admin/index.html", cookie, logger);
        } else {
            String jsonResponse = "{\"login\":0, \"message\":\"Invalid credentials\"}";
            ServerUtils.sendJsonResponse(exchange, 401, jsonResponse, logger);
        }
    }

    public static void handleAdminLogout(String token, HttpExchange exchange, String clientIP) throws IOException, SQLException {
        System.out.println("Called admin logout with token: " + token + ".");
        try {
            if (token != null) {
                DatabaseService.deleteActiveSession(token);
            }
        } catch (Exception e) {
            ServerUtils.logException(logger, e);
        } finally {
            String cookie = "sessionToken=; Expires=Thu, 01 Jan 1970 00:00:00 GMT; Path=/; HttpOnly; SameSite=Strict";
            ServerUtils.sendRedirectResponse(exchange, "/admin/login.html", cookie, logger);
        }
    }

    public static void handleAddAdmin(HttpExchange exchange, String clientIP) throws IOException, SQLException{
        String user = DatabaseService.getActiveUser(getTokenFromExchange(exchange));
        if(user != null) {
            String body = new String(exchange.getRequestBody().readAllBytes());
            Map<String, String> loginData = ServerUtils.parseUrlEncoded(body);
            String usr = loginData.get("usr");
            String pswd = loginData.get("pswd");
            DatabaseService.addAdmin(usr, pswd);

            HashMap<String, String> logData = new HashMap<>();
            logData.put("client_ip", clientIP);
            logData.put("from", usr);
            logData.put("message", "New Admin Added");
            logData.put("new-user", usr);
            logger.log("INFO", logData);

            ServerUtils.sendRedirectResponse(exchange, "/admin/index.html", null, logger);
        } else {
            System.out.println(user);
            ServerUtils.sendErrorResponse(exchange, 401, "Unauthorized", logger);
        }
    }

    public static void handleRemoveAdmin(HttpExchange exchange, String clientIP) throws IOException, SQLException {
        String user = DatabaseService.getActiveUser(getTokenFromExchange(exchange));
        if (user != null) {
            String userDel = ServerUtils.parseUrlEncoded(new String(exchange.getRequestBody().readAllBytes())).get("usr");
            DatabaseService.deleteAdmin(userDel);
            DatabaseService.deleteActiveSession(DatabaseService.getActiveSession(userDel));
        } else {
            ServerUtils.sendErrorResponse(exchange, 401, "Unauthorized", logger);
        }
    }
}
