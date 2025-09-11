import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;

import org.json.JSONObject;

public class StatusHandler implements HttpHandler {

    private StatusService StatusService;

    public StatusHandler(StatusService ss) {
        this.StatusService = ss;
    }

    public void handle(HttpExchange exchange) throws IOException {
        String clientIP = exchange.getRemoteAddress().getAddress().getHostAddress();
        System.out.println("Status Handler Activated by "+clientIP);

        JSONObject data = this.StatusService.getStatusData(clientIP);
        String response = data.toString();

        exchange.getResponseHeaders().set("Content-Type","application/json");
        exchange.sendResponseHeaders(200, response.getBytes().length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}