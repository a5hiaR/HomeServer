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
                try {
                    String token = AuthService.getTokenFromExchange(exchange);
                    String user = AuthService.getUserForToken(token);
                    System.out.println("/admin/* accesed by token: "+ AuthService.getTokenFromExchange(exchange) + ".");
                    if (user == null && !path.equals("/admin/login.html")) {
                        ServerUtils.sendRedirectResponse(exchange, "/admin/login.html", null, logger);
                        return;
                    } else if (token != null && user == null) {
                        String cookie = "sessionToken=; Expires=Thu, 01 Jan 1970 00:00:00 GMT; Path=/; HttpOnly; SameSite=Strict";
                        ServerUtils.sendRedirectResponse(exchange, "/admin/login.html", cookie, logger);
                        return;
                    } else if (user != null && path.equals("/admin/login.html")) {ServerUtils.sendRedirectResponse(exchange, "/admin/index.html", null, logger); return;}
                } catch (Exception e) {
                    ServerUtils.logException(logger, e);
                }
            }

            ServerUtils.sendFileResponse(exchange, 200, path, logger);
        } finally {
            exchange.close();
        }
    }
}