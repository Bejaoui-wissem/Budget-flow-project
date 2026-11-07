package tn.enicarthage.budgetflow.controller;

import tn.enicarthage.budgetflow.dto.LoginRequest;
import tn.enicarthage.budgetflow.dto.RegisterRequest;
import tn.enicarthage.budgetflow.router.Response;
import tn.enicarthage.budgetflow.router.Router;
import tn.enicarthage.budgetflow.service.AuthService;

/** Routes d'authentification : inscription, connexion, utilisateur courant. */
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    public void register(Router router) {
        router.post("/api/auth/register", false,
                ctx -> Response.created(service.register(ctx.body(RegisterRequest.class))));
        router.post("/api/auth/login", false,
                ctx -> Response.ok(service.login(ctx.body(LoginRequest.class))));
        router.get("/api/auth/me", true,
                ctx -> Response.ok(service.me(ctx.userId())));
    }
}
