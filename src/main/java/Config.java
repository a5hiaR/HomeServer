import io.github.cdimascio.dotenv.Dotenv;

public final class Config {
    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    public static final String SERVER_HOST = getEnv("SERVER_HOST", "0.0.0.0");
    public static final int SERVER_PORT = Integer.parseInt(getEnv("SERVER_PORT", "8433"));

    public static final String KEYSTORE_PATH = getRequired("KEYSTORE_PATH");
    public static final String KEYSTORE_PASSWORD = getRequired("KEYSTORE_PASSWORD");
    public static final String KEY_PASSWORD = getEnv("KEY_PASSWORD", KEYSTORE_PASSWORD);

    public static final String LOG_FILE_PATH = getRequired("LOG_FILE_PATH");

    public static final String DB_URL = getRequired("DB_URL");

    private static String getEnv(String key, String defaultValue) {
        String value = dotenv.get(key);
        return (value != null && !value.isBlank()) ? value : defaultValue;
    }

    private static String getRequired(String key) {
        String value = dotenv.get(key);
        if (value == null || value.isBlank()) {
            throw new RuntimeException("Required environment variable '" + key + "' is not set");
        }
        return value;
    }
}