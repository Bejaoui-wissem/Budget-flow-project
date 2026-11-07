package tn.enicarthage.budgetflow.router;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tn.enicarthage.budgetflow.exception.ApiException;
import tn.enicarthage.budgetflow.util.Json;
import tn.enicarthage.budgetflow.util.JwtUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Associe (méthode HTTP, chemin) à un contrôleur. Les routes "secured" exigent
 * un jeton JWT valide dans l'en-tête Authorization: Bearer ...
 */
public class Router implements HttpHandler {

    private static final int MAX_BODY_BYTES = 1_000_000;

    private record Route(String method, Pattern pattern, List<String> paramNames,
                         boolean secured, Handler handler) {
    }

    private final List<Route> routes = new ArrayList<>();

    public void get(String path, boolean secured, Handler h) {
        add("GET", path, secured, h);
    }

    public void post(String path, boolean secured, Handler h) {
        add("POST", path, secured, h);
    }

    public void put(String path, boolean secured, Handler h) {
        add("PUT", path, secured, h);
    }

    public void delete(String path, boolean secured, Handler h) {
        add("DELETE", path, secured, h);
    }

    private void add(String method, String template, boolean secured, Handler h) {
        List<String> names = new ArrayList<>();
        Matcher m = Pattern.compile("\\{(\\w+)}").matcher(template);
        StringBuilder regex = new StringBuilder("^");
        int last = 0;
        while (m.find()) {
            regex.append(Pattern.quote(template.substring(last, m.start()))).append("([^/]+)");
            names.add(m.group(1));
            last = m.end();
        }
        regex.append(Pattern.quote(template.substring(last))).append("$");
        routes.add(new Route(method, Pattern.compile(regex.toString()), names, secured, h));
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            addCorsHeaders(ex);
            String method = ex.getRequestMethod();
            if ("OPTIONS".equals(method)) {
                send(ex, 204, null);
                return;
            }
            String path = ex.getRequestURI().getPath();
            if (path.length() > 1 && path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }

            boolean pathFound = false;
            for (Route route : routes) {
                Matcher m = route.pattern.matcher(path);
                if (!m.matches()) {
                    continue;
                }
                pathFound = true;
                if (!route.method.equals(method)) {
                    continue;
                }
                Map<String, String> params = new HashMap<>();
                for (int i = 0; i < route.paramNames.size(); i++) {
                    params.put(route.paramNames.get(i),
                            URLDecoder.decode(m.group(i + 1), StandardCharsets.UTF_8));
                }
                Integer userId = route.secured ? authenticate(ex) : null;
                RequestContext ctx = new RequestContext(params, parseQuery(ex.getRequestURI().getRawQuery()),
                        readBody(ex), userId);
                Response response = route.handler.handle(ctx);
                send(ex, response.status(), response.body() == null ? null : Json.toJson(response.body()));
                return;
            }
            throw pathFound ? new ApiException(405, "Méthode non autorisée")
                    : ApiException.notFound("Route introuvable");
        } catch (ApiException e) {
            send(ex, e.getStatus(), Json.toJson(Map.of("error", e.getMessage())));
        } catch (Exception e) {
            e.printStackTrace();
            send(ex, 500, Json.toJson(Map.of("error", "Erreur interne du serveur")));
        } finally {
            ex.close();
        }
    }

    private int authenticate(HttpExchange ex) {
        String header = ex.getRequestHeaders().getFirst("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw ApiException.unauthorized("Authentification requise");
        }
        return JwtUtil.verify(header.substring(7).trim());
    }

    private String readBody(HttpExchange ex) throws IOException {
        try (InputStream in = ex.getRequestBody()) {
            byte[] data = in.readNBytes(MAX_BODY_BYTES + 1);
            if (data.length > MAX_BODY_BYTES) {
                throw ApiException.badRequest("Requête trop volumineuse");
            }
            return new String(data, StandardCharsets.UTF_8);
        }
    }

    private Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> result = new HashMap<>();
        if (rawQuery == null || rawQuery.isEmpty()) {
            return result;
        }
        for (String pair : rawQuery.split("&")) {
            int eq = pair.indexOf('=');
            String key = URLDecoder.decode(eq < 0 ? pair : pair.substring(0, eq), StandardCharsets.UTF_8);
            String value = eq < 0 ? "" : URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8);
            result.put(key, value);
        }
        return result;
    }

    private void addCorsHeaders(HttpExchange ex) {
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
    }

    private void send(HttpExchange ex, int status, String json) throws IOException {
        if (json == null) {
            ex.sendResponseHeaders(status, -1);
            return;
        }
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = ex.getResponseBody()) {
            out.write(bytes);
        }
    }
}
