package tn.enicarthage.budgetflow.dao;

import tn.enicarthage.budgetflow.config.DatabaseConnection;
import tn.enicarthage.budgetflow.exception.ApiException;
import tn.enicarthage.budgetflow.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class UserDAO {

    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = "SELECT id, nom, prenom, email, mot_de_passe FROM users WHERE email = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public Optional<User> findById(int id) throws SQLException {
        String sql = "SELECT id, nom, prenom, email, mot_de_passe FROM users WHERE id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    /** Insère l'utilisateur et retourne son identifiant. */
    public int insert(String nom, String prenom, String email, String passwordHash) throws SQLException {
        String sql = "INSERT INTO users (nom, prenom, email, mot_de_passe) VALUES (?, ?, ?, ?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nom);
            ps.setString(2, prenom);
            ps.setString(3, email);
            ps.setString(4, passwordHash);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            if ("23000".equals(e.getSQLState())) {
                throw ApiException.conflict("Un compte existe déjà avec cet e-mail");
            }
            throw e;
        }
    }

    private User map(ResultSet rs) throws SQLException {
        return new User(rs.getInt("id"), rs.getString("nom"), rs.getString("prenom"),
                rs.getString("email"), rs.getString("mot_de_passe"));
    }
}
