import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.net.InetSocketAddress;

import java.io.IOException;
import java.io.OutputStream;

import org.json.JSONObject;

public class MainServer {
    public static void main(String[] args) {
        try {
            SSLContext sslContext = createSSLContext();

            HttpsServer server = HttpsServer.create(new InetSocketAddress(Config.SERVER_PORT), 0);


            server.setHttpsConfigurator(new HttpsConfigurator(sslContext));

            StatusService StatusService = new StatusService();
            AuthService authService = new AuthService();

            Logger authLogger = new Logger("AuthHandler");
            Logger staticLogger = new Logger("StaticHandler");
            Logger statusLogger = new Logger("StatusHandler");

            AuthHandler authHandler = new AuthHandler(authService, authLogger);

            StatusHandler statusHandler = new StatusHandler(StatusService, statusLogger);
            StaticHandler staticHandler = new StaticHandler(staticLogger, authHandler);

            server.createContext("/api/auth", authHandler);
            server.createContext("/api/status", statusHandler);
            server.createContext("/", staticHandler);

            Runtime.getRuntime().addShutdownHook(new Thread(Logger::shutdown));

            server.start();

            System.out.println("Server is running on " + Config.SERVER_HOST + ":" + Config.SERVER_PORT);
        } catch (Exception e) {
            System.err.println("!!! --- Server failed to start --- !!!");
            System.err.println("Error: " + e.getMessage());
            // e.printStackTrace(); // Uncomment for full stack trace during debugging
            System.exit(1);
        }
    }

    public static SSLContext createSSLContext() throws Exception {
        char[] keystorePassword = Config.KEYSTORE_PASSWORD.toCharArray();
        char[] keyPassword = Config.KEY_PASSWORD.toCharArray();

        
        KeyStore keyStore;
        try (FileInputStream fis = new FileInputStream(Config.KEYSTORE_PATH)) {
            keyStore = KeyStore.getInstance("JKS");
            keyStore.load(fis, keystorePassword);
        } catch (IOException e) {
            // A BadPaddingException here usually means the KEYSTORE_PASSWORD is wrong.
            throw new RuntimeException("Failed to load keystore. Check KEYSTORE_PATH and KEYSTORE_PASSWORD.", e);
        }
        
        KeyManagerFactory keyManagerFactory;
        try {
            keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, keyPassword);
        } catch (Exception e) {
            // A BadPaddingException here means the KEY_PASSWORD is wrong for the private key.
            throw new RuntimeException("Failed to initialize KeyManager. Check KEY_PASSWORD.", e);
        }

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
        trustManagerFactory.init(keyStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

        return sslContext;
    }
}