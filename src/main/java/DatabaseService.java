import java.sql.*;
import org.sqlite.JDBC;
import org.sqlite.SQLiteConfig;

public class DatabaseService {
    private static DatabaseService instance;
    private Connection connection;
    private static Logger logger;

    public DatabaseService() {
        connect();
        initializeDatabase();
        logger = new Logger("DatabaseService");
    
    }

    public static synchonized DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public void connect(String dbPath) throws SQLException{
        try {
            
        } catch(SQLException e) {
            ServerUtils.logException(logger, "DB-Service", e);
            throw RuntimeException("Database connect() failed", e);
        }
    }
}