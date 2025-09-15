import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class StaticHandler implements HttpHandler {
    private Logger logger;
    private AuthHandler authHandler;

    public StaticHandler(Logger logger, AuthHandler authHandler) {
        this.logger = logger;
        this.authHandler = authHandler;
    }

    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();

            if (path.endsWith("/")) {
                path += "index.html";
            }

            if (path.startsWith("/admin/")) {
                String user = authHandler.checkTokenFromCookie(exchange.getRequestHeaders().getFirst("Cookie"));
                if (user == null && !path.equals("/admin/login.html")) {
                    ServerUtils.sendRedirectResponse(exchange, "/admin/login.html", null, logger);
                    return;
                }
            }

            ServerUtils.sendFileResponse(exchange, 200, path, logger);
        } finally {
            exchange.close();
        }
    }
}