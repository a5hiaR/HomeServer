import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

import org.json.JSONObject;

public class StatusHandler implements HttpHandler {

    private StatusService StatusService;
    private Logger logger;

    public StatusHandler(StatusService ss, Logger logger) {
        this.StatusService = ss;
        this.logger = logger;
    }

    public void handle(HttpExchange exchange) throws IOException {
        try {
            String clientIP = exchange.getRemoteAddress().getAddress().getHostAddress();

            JSONObject data = this.StatusService.getStatusData(clientIP);
            String response = data.toString();
            ServerUtils.sendJsonResponse(exchange, 200, response, logger);
        } finally {
            exchange.close();
        }
    }
}