package tn.enicarthage.budgetflow.controller;

import tn.enicarthage.budgetflow.dto.ExpenseRequest;
import tn.enicarthage.budgetflow.router.Response;
import tn.enicarthage.budgetflow.router.Router;
import tn.enicarthage.budgetflow.service.ExpenseService;

/** CRUD des dépenses (toutes les routes exigent une authentification). */
public class ExpenseController {

    private final ExpenseService service;

    public ExpenseController(ExpenseService service) {
        this.service = service;
    }

    public void register(Router router) {
        // GET /api/expenses?categoryId=&from=&to=&q=&sort=date|montant&order=asc|desc
        router.get("/api/expenses", true, ctx -> Response.ok(service.list(
                ctx.userId(), ctx.queryInt("categoryId"), ctx.query("from"), ctx.query("to"),
                ctx.query("q"), ctx.query("sort"), ctx.query("order"))));
        router.get("/api/expenses/{id}", true,
                ctx -> Response.ok(service.get(ctx.pathInt("id"), ctx.userId())));
        router.post("/api/expenses", true,
                ctx -> Response.created(service.create(ctx.userId(), ctx.body(ExpenseRequest.class))));
        router.put("/api/expenses/{id}", true,
                ctx -> Response.ok(service.update(ctx.pathInt("id"), ctx.userId(), ctx.body(ExpenseRequest.class))));
        router.delete("/api/expenses/{id}", true, ctx -> {
            service.delete(ctx.pathInt("id"), ctx.userId());
            return Response.noContent();
        });
    }
}
