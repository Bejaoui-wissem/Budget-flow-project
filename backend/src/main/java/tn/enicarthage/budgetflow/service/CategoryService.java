package tn.enicarthage.budgetflow.service;

import tn.enicarthage.budgetflow.dao.CategoryDAO;
import tn.enicarthage.budgetflow.dto.CategoryRequest;
import tn.enicarthage.budgetflow.exception.ApiException;
import tn.enicarthage.budgetflow.model.Category;
import tn.enicarthage.budgetflow.util.Validator;

import java.sql.SQLException;
import java.util.List;

public class CategoryService {

    private final CategoryDAO dao;

    public CategoryService(CategoryDAO dao) {
        this.dao = dao;
    }

    public List<Category> list(int userId) throws SQLException {
        return dao.findAllForUser(userId);
    }

    public Category get(int id, int userId) throws SQLException {
        return dao.findAccessible(id, userId)
                .orElseThrow(() -> ApiException.notFound("Catégorie introuvable"));
    }

    public Category create(int userId, CategoryRequest r) throws SQLException {
        String nom = Validator.required(r.nom(), "Nom", 50);
        String description = Validator.optional(r.description(), "Description", 255);
        int id = dao.insert(userId, nom, description);
        return get(id, userId);
    }

    public Category update(int id, int userId, CategoryRequest r) throws SQLException {
        Category existing = get(id, userId);
        if (!existing.personnalisee()) {
            throw ApiException.badRequest("Les catégories par défaut ne sont pas modifiables");
        }
        String nom = Validator.required(r.nom(), "Nom", 50);
        String description = Validator.optional(r.description(), "Description", 255);
        dao.update(id, userId, nom, description);
        return get(id, userId);
    }

    public void delete(int id, int userId) throws SQLException {
        Category existing = get(id, userId);
        if (!existing.personnalisee()) {
            throw ApiException.badRequest("Les catégories par défaut ne sont pas supprimables");
        }
        if (dao.countExpenses(id) > 0) {
            throw ApiException.conflict("Cette catégorie contient des dépenses : supprimez-les d'abord");
        }
        dao.delete(id, userId);
    }
}
