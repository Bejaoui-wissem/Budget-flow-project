package tn.enicarthage.budgetflow.router;

/** Réponse HTTP : code + corps (sérialisé en JSON, ou vide si null). */
public record Response(int status, Object body) {

    public static Response ok(Object body) {
        return new Response(200, body);
    }

    public static Response created(Object body) {
        return new Response(201, body);
    }

    public static Response noContent() {
        return new Response(204, null);
    }
}
