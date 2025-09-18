import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.sql.*;

public class AuthService {
    public String login(String username, String password) throws SQLException {
        String storedPassword = DatabaseService.getAdminPass(username);
        if (storedPassword != null && storedPassword.equals(password)) {
            String token = UUID.randomUUID().toString();
            DatabaseService.newActiveSession(token, username);
            return token;
        }
        return null;
    }

    public String getUserForToken(String token) throws SQLException {
        return token != null ? DatabaseService.getActiveUser(token): null;
    }
}
