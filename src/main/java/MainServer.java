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

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 5);

        StatusHandler StatusHandler = new StatusHandler(StatusService);
        StaticHandler StaticHandler = new StaticHandler();

        server.createContext("/api/status", StatusHandler);
        server.createContext("/", StaticHandler);

        server.start();

        System.out.println("Server is running");
    }
}