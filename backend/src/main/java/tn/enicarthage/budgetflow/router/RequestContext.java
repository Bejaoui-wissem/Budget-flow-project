package tn.enicarthage.budgetflow.router;

import tn.enicarthage.budgetflow.exception.ApiException;
import tn.enicarthage.budgetflow.util.Json;

import java.util.Map;

/** Informations d'une requête : paramètres de chemin, query string, corps et utilisateur connecté. */
public class RequestContext {

    private final Map<String, String> pathParams;
    private final Map<String, String> query;
    private final String body;
    private final Integer userId;

    public RequestContext(Map<String, String> pathParams, Map<String, String> query,
                          String body, Integer userId) {
        this.pathParams = pathParams;
        this.query = query;
        this.body = body;
        this.userId = userId;
    }

    public int userId() {
        if (userId == null) {
            throw ApiException.unauthorized("Authentification requise");
        }
        return userId;
    }

    public int pathInt(String name) {
        try {
            return Integer.parseInt(pathParams.get(name));
        } catch (NumberFormatException e) {
            throw ApiException.badRequest("Identifiant invalide");
        }
    }

    public String query(String name) {
        return query.get(name);
    }

    public Integer queryInt(String name) {
        String value = query.get(name);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            throw ApiException.badRequest("Paramètre " + name + " invalide");
        }
    }

    public <T> T body(Class<T> type) {
        return Json.fromJson(body, type);
    }
}
