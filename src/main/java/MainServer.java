import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.net.InetSocketAddress;

import java.io.IOException;
import java.io.OutputStream;

import org.json.JSONObject;

public class MainServer {
    public static void main(String[] args) throws IOException {
        StatusService StatusService = new StatusService();
        AuthService authService = new AuthService();

        Logger authLogger = new Logger("AuthHandler");
        Logger staticLogger = new Logger("StaticHandler");
        Logger statusLogger = new Logger("StatusHandler");

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 5);

        AuthHandler authHandler = new AuthHandler(authService, authLogger);

        StatusHandler statusHandler = new StatusHandler(StatusService, statusLogger);
        StaticHandler staticHandler = new StaticHandler(staticLogger, authHandler);

        server.createContext("/api/auth", authHandler);
        server.createContext("/api/status", statusHandler);
        server.createContext("/", staticHandler);

        Runtime.getRuntime().addShutdownHook(new Thread(Logger::shutdown));

        server.start();

        System.out.println("Server is running");
    }
}