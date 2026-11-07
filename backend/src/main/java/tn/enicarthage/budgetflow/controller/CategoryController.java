package tn.enicarthage.budgetflow.controller;

import tn.enicarthage.budgetflow.dto.CategoryRequest;
import tn.enicarthage.budgetflow.router.Response;
import tn.enicarthage.budgetflow.router.Router;
import tn.enicarthage.budgetflow.service.CategoryService;

/** CRUD des catégories (toutes les routes exigent une authentification). */
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    public void register(Router router) {
        router.get("/api/categories", true,
                ctx -> Response.ok(service.list(ctx.userId())));
        router.get("/api/categories/{id}", true,
                ctx -> Response.ok(service.get(ctx.pathInt("id"), ctx.userId())));
        router.post("/api/categories", true,
                ctx -> Response.created(service.create(ctx.userId(), ctx.body(CategoryRequest.class))));
        router.put("/api/categories/{id}", true,
                ctx -> Response.ok(service.update(ctx.pathInt("id"), ctx.userId(), ctx.body(CategoryRequest.class))));
        router.delete("/api/categories/{id}", true, ctx -> {
            service.delete(ctx.pathInt("id"), ctx.userId());
            return Response.noContent();
        });
    }
}
