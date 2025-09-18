import java.sql.*;
import org.sqlite.JDBC;
import org.sqlite.SQLiteConfig;


public class DatabaseService {
    private static DatabaseService instance;
    private Connection connection;
    private static final Logger logger = new Logger("DatabaseService");

    private DatabaseService() throws Exception {
        connect(Config.DB_URL);
        initializeDatabase();
    }

    public static synchronized DatabaseService getInstance() throws Exception{
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public void initializeDatabase() {
        String createActiveSessionsTable = """
        CREATE TABLE IF NOT EXISTS activeSessions (
            token TEXT PRIMARY KEY,
            user TEXT NOT NULL,
            created TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );
        """;

        String  createAdminTable = """
        CREATE TABLE IF NOT EXISTS admins (
            username TEXT PRIMARY KEY,
            password TEXT NOT NULL
        );
        """;

        try (Statement statement = connection.createStatement()) {
            statement.execute(createActiveSessionsTable);
            statement.execute(createAdminTable);

            String insertAdminSQL = "INSERT OR IGNORE INTO admins (username, password) VALUES (?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(insertAdminSQL)) {
                ps.setString(1, "admin");
                ps.setString(2, "test");
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            ServerUtils.logException(logger, e);
            throw new RuntimeException("Database initializeDatabase() failed", e);
        }
    }

    public void connect(String dbPath) throws Exception{
        try {
            DriverManager.registerDriver(new JDBC());

            connection = DriverManager.getConnection(dbPath);

            ServerUtils.logEvent(logger, null, "Database connected");      
        } catch(Exception e) {
            ServerUtils.logException(logger, e);
            throw new RuntimeException("Database connect() failed", e);
        }
    }

    public static void newActiveSession(String token, String user) throws SQLException {
        String insertQuery = "INSERT INTO activeSessions (token, user) VALUES (?, ?)";
        try (PreparedStatement statement = getInstance().getConnection().prepareStatement(insertQuery)) {
            statement.setString(1, token);
            statement.setString(2, user);
            statement.executeUpdate();
        } catch (Exception e) {
            ServerUtils.logException(logger, e);
            throw new SQLException("Failed to create new active session.", e);
        }
    }

    public static void deleteActiveSession(String token) throws SQLException {
        String deleteQuery = "DELETE FROM activeSessions WHERE token = ?";
        try(PreparedStatement statement = getInstance().getConnection().prepareStatement(deleteQuery)) {
            statement.setString(1, token);
            statement.executeUpdate();
        } catch (Exception e) {
            ServerUtils.logException(logger, e);
            throw new SQLException("Failed to delete active session.", e);
        }
    }

    public static String getActiveUser(String token) throws SQLException {
        String selectQuery = "SELECT user FROM activeSessions WHERE token = ?";
        try(PreparedStatement statement = getInstance().getConnection().prepareStatement(selectQuery)) {
            statement.setString(1, token);
            try(ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("user");
                }
            }
        } catch (Exception e) {
            ServerUtils.logException(logger, e);
            throw new SQLException("Failed to get active user.", e);
        }
        return null;
    }

    public static void addAdmin(String user, String pass) throws SQLException {
        String insertQuery = "INSERT INTO admins (username, password) VALUES (?, ?)";
        try(PreparedStatement statement = getInstance().getConnection().prepareStatement(insertQuery)) {
            statement.setString(1, user);
            statement.setString(2, pass);
            statement.executeUpdate();
        } catch (Exception e) {
            ServerUtils.logException(logger, e);
            throw new SQLException("Failed to add admin.", e);
        }
    }

    public static void deleteAdmin(String user) throws SQLException {
        String deleteQuery = "DELETE FROM admins WHERE username = ?";
        try(PreparedStatement statement = getInstance().getConnection().prepareStatement(deleteQuery)) {
            statement.setString(1, user);
            statement.executeUpdate();
        } catch (Exception e) {
            ServerUtils.logException(logger, e);
            throw new SQLException("Failed to delete admin.", e);
        }
    }
    
    public static String getAdminPass(String user) throws SQLException {
        String selectQuery = "SELECT password FROM admins WHERE username = ?";
        try(PreparedStatement statement = getInstance().getConnection().prepareStatement(selectQuery)) {
            statement.setString(1, user);
            try(ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("password");
                }
            }
        } catch (Exception e) { 
            ServerUtils.logException(logger, e);
            throw new SQLException("Failed to get admin password.", e);
        }
        return null;
    }

}