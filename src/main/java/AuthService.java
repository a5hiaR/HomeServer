import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AuthService {
    private final Map<String, String> sessions = new ConcurrentHashMap<>();
    
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "test";

    public String login(String username, String password) {
        if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
            String token = UUID.randomUUID().toString();
            sessions.put(token, username);
            return token;
        }
        return null;
    }

    public String getUserForToken(String token) {
        return token != null ? sessions.get(token) : null;
    }
}
