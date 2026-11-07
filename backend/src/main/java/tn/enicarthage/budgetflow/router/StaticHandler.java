package tn.enicarthage.budgetflow.router;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** Sert la page de test (resources/static) à l'adresse http://localhost:8080/ */
public class StaticHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            String path = ex.getRequestURI().getPath();
            String name = "/".equals(path) ? "index.html" : path.substring(1);
            if (!"GET".equals(ex.getRequestMethod()) || !name.matches("[A-Za-z0-9._-]+")) {
                ex.sendResponseHeaders(404, -1);
                return;
            }
            try (InputStream in = StaticHandler.class.getResourceAsStream("/static/" + name)) {
                if (in == null) {
                    ex.sendResponseHeaders(404, -1);
                    return;
                }
                byte[] data = in.readAllBytes();
                ex.getResponseHeaders().set("Content-Type", contentType(name));
                ex.sendResponseHeaders(200, data.length);
                try (OutputStream out = ex.getResponseBody()) {
                    out.write(data);
                }
            }
        } finally {
            ex.close();
        }
    }

    private String contentType(String name) {
        if (name.endsWith(".html")) return "text/html; charset=utf-8";
        if (name.endsWith(".js")) return "text/javascript; charset=utf-8";
        if (name.endsWith(".css")) return "text/css; charset=utf-8";
        return "application/octet-stream";
    }
}
