package tn.enicarthage.budgetflow.dao;

import tn.enicarthage.budgetflow.config.DatabaseConnection;
import tn.enicarthage.budgetflow.model.Expense;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** CRUD des dépenses ; toutes les requêtes sont filtrées sur l'utilisateur connecté. */
public class ExpenseDAO {

    private static final String SELECT =
            "SELECT e.id, e.montant, e.date_depense, e.description, e.mode_paiement, "
                    + "e.category_id, c.nom AS category_nom "
                    + "FROM expenses e JOIN categories c ON c.id = e.category_id ";

    /** Liste filtrée. sort : "date" ou "montant" ; order : "asc" ou "desc" (valeurs déjà validées). */
    public List<Expense> findAll(int userId, Integer categoryId, LocalDate from, LocalDate to,
                                 String search, String sort, String order) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT).append("WHERE e.user_id = ? ");
        List<Object> params = new ArrayList<>();
        params.add(userId);
        if (categoryId != null) {
            sql.append("AND e.category_id = ? ");
            params.add(categoryId);
        }
        if (from != null) {
            sql.append("AND e.date_depense >= ? ");
            params.add(Date.valueOf(from));
        }
        if (to != null) {
            sql.append("AND e.date_depense <= ? ");
            params.add(Date.valueOf(to));
        }
        if (search != null && !search.isBlank()) {
            sql.append("AND e.description LIKE ? ");
            params.add("%" + search.trim() + "%");
        }
        String column = "montant".equals(sort) ? "e.montant" : "e.date_depense";
        String direction = "asc".equalsIgnoreCase(order) ? "ASC" : "DESC";
        sql.append("ORDER BY ").append(column).append(' ').append(direction).append(", e.id DESC");

        List<Expense> result = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        }
        return result;
    }

    public Optional<Expense> findById(int id, int userId) throws SQLException {
        String sql = SELECT + "WHERE e.id = ? AND e.user_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public int insert(int userId, BigDecimal montant, LocalDate date, String description,
                      String modePaiement, int categoryId) throws SQLException {
        String sql = "INSERT INTO expenses (montant, date_depense, description, mode_paiement, user_id, category_id) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setBigDecimal(1, montant);
            ps.setDate(2, Date.valueOf(date));
            ps.setString(3, description);
            ps.setString(4, modePaiement);
            ps.setInt(5, userId);
            ps.setInt(6, categoryId);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    public boolean update(int id, int userId, BigDecimal montant, LocalDate date, String description,
                          String modePaiement, int categoryId) throws SQLException {
        String sql = "UPDATE expenses SET montant = ?, date_depense = ?, description = ?, "
                + "mode_paiement = ?, category_id = ? WHERE id = ? AND user_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBigDecimal(1, montant);
            ps.setDate(2, Date.valueOf(date));
            ps.setString(3, description);
            ps.setString(4, modePaiement);
            ps.setInt(5, categoryId);
            ps.setInt(6, id);
            ps.setInt(7, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id, int userId) throws SQLException {
        String sql = "DELETE FROM expenses WHERE id = ? AND user_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    private Expense map(ResultSet rs) throws SQLException {
        return new Expense(rs.getInt("id"), rs.getBigDecimal("montant"),
                rs.getDate("date_depense").toLocalDate().toString(),
                rs.getString("description"), rs.getString("mode_paiement"),
                rs.getInt("category_id"), rs.getString("category_nom"));
    }
}
