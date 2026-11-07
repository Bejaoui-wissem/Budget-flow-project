package tn.enicarthage.budgetflow.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import tn.enicarthage.budgetflow.exception.ApiException;

public final class Json {

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private Json() {
    }

    public static String toJson(Object value) {
        return GSON.toJson(value);
    }

    public static <T> T fromJson(String json, Class<T> type) {
        if (json == null || json.isBlank()) {
            throw ApiException.badRequest("Corps de la requête manquant");
        }
        try {
            T value = GSON.fromJson(json, type);
            if (value == null) {
                throw ApiException.badRequest("Corps de la requête invalide");
            }
            return value;
        } catch (JsonSyntaxException e) {
            throw ApiException.badRequest("JSON invalide");
        }
    }
}
