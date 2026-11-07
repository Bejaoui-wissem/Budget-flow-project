package tn.enicarthage.budgetflow;

import com.sun.net.httpserver.HttpServer;
import tn.enicarthage.budgetflow.controller.AuthController;
import tn.enicarthage.budgetflow.controller.CategoryController;
import tn.enicarthage.budgetflow.controller.ExpenseController;
import tn.enicarthage.budgetflow.dao.CategoryDAO;
import tn.enicarthage.budgetflow.dao.ExpenseDAO;
import tn.enicarthage.budgetflow.dao.UserDAO;
import tn.enicarthage.budgetflow.router.Router;
import tn.enicarthage.budgetflow.router.StaticHandler;
import tn.enicarthage.budgetflow.service.AuthService;
import tn.enicarthage.budgetflow.service.CategoryService;
import tn.enicarthage.budgetflow.service.ExpenseService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/** Assemble les couches (DAO, services, contrôleurs, routeur) et démarre le serveur HTTP. */
public class ApiServer {

    private final HttpServer server;

    public ApiServer(int port) throws IOException {
        UserDAO userDAO = new UserDAO();
        CategoryDAO categoryDAO = new CategoryDAO();
        ExpenseDAO expenseDAO = new ExpenseDAO();

        Router router = new Router();
        new AuthController(new AuthService(userDAO)).register(router);
        new CategoryController(new CategoryService(categoryDAO)).register(router);
        new ExpenseController(new ExpenseService(expenseDAO, categoryDAO)).register(router);

        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api", router);
        server.createContext("/", new StaticHandler());
        server.setExecutor(Executors.newFixedThreadPool(8));
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }
}
