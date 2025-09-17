import java.sql.*;
import org.sqlite.JDBC;
import org.sqlite.SQLiteConfig;

public class DatabaseService {
    private static DatabaseService instance;
    private Connection connection;
    private static Logger logger;

    public DatabaseService() throws Exception {
        connect(Config.DB_URL);
        initializeDatabase();
        logger = new Logger("DatabaseService");
    
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
    
    }

    public void connect(String dbPath) throws Exception{
        try {
            DriverManager.registerDriver(new JDBC());

            connection = DriverManager.getConnection(dbPath);

            ServerUtils.logEvent(logger, "DB-Service", "Database connected");          
        } catch(Exception e) {
            ServerUtils.logException(logger, "DB-Service", e);
            throw new RuntimeException("Database connect() failed", e);
        }
    }
}