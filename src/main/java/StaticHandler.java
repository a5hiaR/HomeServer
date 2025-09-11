import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.json.JSONObject;

public class StaticHandler implements HttpHandler {
    private String clientIP;
    private HttpExchange exchange;

    public void handle(HttpExchange exchange) throws IOException {
        this.exchange = exchange;
        this.clientIP = this.exchange.getRemoteAddress().getAddress().getHostAddress();
        
        String path = this.exchange.getRequestURI().getPath();

        if (path.endsWith("/")) {path += "index.html";}

        InputStream resourceStream = getClass().getClassLoader().getResourceAsStream("webroot"+path);

        if (resourceStream == null) {
            send404();
            return;
        }

        exchange.getResponseHeaders().set("Content-Type", getContentType(path));

        try (OutputStream os = exchange.getResponseBody();
            InputStream is = resourceStream;) {
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            this.exchange.sendResponseHeaders(200, 0);

            while((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }

            System.out.println("Sent "+path+" to "+this.clientIP);
        } catch (Exception exception) {
            System.out.println(exception+" sending "+path+" to "+this.clientIP);
        }
    }

    public void send404() throws IOException {

        String response = "This is a mediocre 404";
        

        try(OutputStream os = this.exchange.getResponseBody()) {
            this.exchange.getResponseHeaders().set("Content-Type","text/plain");
            this.exchange.sendResponseHeaders(404, response.getBytes().length);

            os.write(response.getBytes());
            System.out.println("Sent 404 to "+clientIP);
        } catch (Exception exception) {
            System.out.println(exception+" sending 404 to "+this.clientIP);
        }
    }

    public void sendErr(int err) throws IOException {
        String response = err+" Error!?";

        try(OutputStream os = this.exchange.getResponseBody()) {
        this.exchange.getResponseHeaders().set("Content-Tyoe", "text/plain");
        this.exchange.sendResponseHeaders(err, response.getBytes().length);

            os.write(response.getBytes()):
            System.out.println("Sent "+err+" error to "+clientIP);
        } catch (Exception exception) {
            System.out.println(exception+" sending "+err+" to "+ clientIP);
        }
    }

    public String getContentType(String p) {
        if (p.endsWith(".html")) return "text/html";
        if (p.endsWith(".css")) return "text/css";
        if (p.endsWith(".js")) return "application/javascript";
        if (p.endsWith(".png")) return "image/png";
        if (p.endsWith(".jpg") || p.endsWith(".jpeg")) return "image/jpeg";
        return "application/octet-stream";
    }
}