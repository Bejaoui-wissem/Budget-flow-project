package tn.enicarthage.budgetflow.router;

@FunctionalInterface
public interface Handler {
    Response handle(RequestContext ctx) throws Exception;
}
