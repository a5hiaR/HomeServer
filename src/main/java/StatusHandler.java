import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;

import org.json.JSONObject;

import java.util.HashMap;

public class StatusHandler implements HttpHandler {

    private StatusService StatusService;
    private Logger logger;

    public StatusHandler(StatusService ss, Logger logger) {
        this.StatusService = ss;
        this.logger = logger;
    }

    public void handle(HttpExchange exchange) throws IOException {
        String clientIP = exchange.getRemoteAddress().getAddress().getHostAddress();
        HashMap<String, String> logData = new HashMap<>();
        logData.put("client_ip", clientIP);
        logger.log("INFO", logData);

        JSONObject data = this.StatusService.getStatusData(clientIP);
        String response = data.toString();

        exchange.getResponseHeaders().set("Content-Type","application/json");
        exchange.sendResponseHeaders(200, response.getBytes().length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}