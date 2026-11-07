package tn.enicarthage.budgetflow.service;

import tn.enicarthage.budgetflow.dao.CategoryDAO;
import tn.enicarthage.budgetflow.dao.ExpenseDAO;
import tn.enicarthage.budgetflow.dto.ExpenseRequest;
import tn.enicarthage.budgetflow.exception.ApiException;
import tn.enicarthage.budgetflow.model.Expense;
import tn.enicarthage.budgetflow.util.Validator;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** Règles métier des dépenses : montant positif, catégorie obligatoire et accessible. */
public class ExpenseService {

    private final ExpenseDAO expenseDAO;
    private final CategoryDAO categoryDAO;

    public ExpenseService(ExpenseDAO expenseDAO, CategoryDAO categoryDAO) {
        this.expenseDAO = expenseDAO;
        this.categoryDAO = categoryDAO;
    }

    public List<Expense> list(int userId, Integer categoryId, String from, String to,
                              String search, String sort, String order) throws SQLException {
        LocalDate fromDate = (from == null || from.isBlank()) ? null : Validator.date(from, "Date de début");
        LocalDate toDate = (to == null || to.isBlank()) ? null : Validator.date(to, "Date de fin");
        if (sort != null && !sort.isBlank() && !sort.equals("date") && !sort.equals("montant")) {
            throw ApiException.badRequest("Tri invalide (date ou montant)");
        }
        if (order != null && !order.isBlank() && !order.equalsIgnoreCase("asc") && !order.equalsIgnoreCase("desc")) {
            throw ApiException.badRequest("Ordre invalide (asc ou desc)");
        }
        return expenseDAO.findAll(userId, categoryId, fromDate, toDate, search, sort, order);
    }

    public Expense get(int id, int userId) throws SQLException {
        return expenseDAO.findById(id, userId)
                .orElseThrow(() -> ApiException.notFound("Dépense introuvable"));
    }

    public Expense create(int userId, ExpenseRequest r) throws SQLException {
        Validated v = validate(userId, r);
        int id = expenseDAO.insert(userId, v.montant, v.date, v.description, v.modePaiement, v.categoryId);
        return get(id, userId);
    }

    public Expense update(int id, int userId, ExpenseRequest r) throws SQLException {
        get(id, userId); // 404 si la dépense n'existe pas ou n'appartient pas à l'utilisateur
        Validated v = validate(userId, r);
        expenseDAO.update(id, userId, v.montant, v.date, v.description, v.modePaiement, v.categoryId);
        return get(id, userId);
    }

    public void delete(int id, int userId) throws SQLException {
        if (!expenseDAO.delete(id, userId)) {
            throw ApiException.notFound("Dépense introuvable");
        }
    }

    private Validated validate(int userId, ExpenseRequest r) throws SQLException {
        BigDecimal montant = Validator.positiveAmount(r.montant());
        LocalDate date = Validator.date(r.date(), "Date");
        String description = Validator.optional(r.description(), "Description", 255);
        String modePaiement = Validator.optional(r.modePaiement(), "Mode de paiement", 30);
        if (r.categoryId() == null) {
            throw ApiException.badRequest("Catégorie obligatoire");
        }
        if (categoryDAO.findAccessible(r.categoryId(), userId).isEmpty()) {
            throw ApiException.badRequest("Catégorie invalide");
        }
        return new Validated(montant, date, description, modePaiement, r.categoryId());
    }

    private record Validated(BigDecimal montant, LocalDate date, String description,
                             String modePaiement, int categoryId) {
    }
}
