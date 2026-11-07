package tn.enicarthage.budgetflow.dao;

import tn.enicarthage.budgetflow.config.DatabaseConnection;
import tn.enicarthage.budgetflow.model.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Une catégorie est visible par un utilisateur si elle est par défaut (user_id NULL)
 * ou si elle lui appartient. Seules les catégories personnelles sont modifiables.
 */
public class CategoryDAO {

    public List<Category> findAllForUser(int userId) throws SQLException {
        String sql = "SELECT id, nom, description, user_id FROM categories "
                + "WHERE user_id IS NULL OR user_id = ? ORDER BY nom";
        List<Category> result = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        }
        return result;
    }

    public Optional<Category> findAccessible(int id, int userId) throws SQLException {
        String sql = "SELECT id, nom, description, user_id FROM categories "
                + "WHERE id = ? AND (user_id IS NULL OR user_id = ?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public int insert(int userId, String nom, String description) throws SQLException {
        String sql = "INSERT INTO categories (nom, description, user_id) VALUES (?, ?, ?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nom);
            ps.setString(2, description);
            ps.setInt(3, userId);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    /** Retourne true si une catégorie personnelle a été modifiée. */
    public boolean update(int id, int userId, String nom, String description) throws SQLException {
        String sql = "UPDATE categories SET nom = ?, description = ? WHERE id = ? AND user_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nom);
            ps.setString(2, description);
            ps.setInt(3, id);
            ps.setInt(4, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id, int userId) throws SQLException {
        String sql = "DELETE FROM categories WHERE id = ? AND user_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    /** Nombre de dépenses rattachées à la catégorie (pour refuser sa suppression). */
    public int countExpenses(int categoryId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM expenses WHERE category_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private Category map(ResultSet rs) throws SQLException {
        rs.getInt("user_id");
        boolean personal = !rs.wasNull();
        return new Category(rs.getInt("id"), rs.getString("nom"), rs.getString("description"), personal);
    }
}
