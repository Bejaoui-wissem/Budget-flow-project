package tn.enicarthage.budgetflow.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Connexion JDBC à MySQL. Les paramètres peuvent être modifiés avec des
 * variables d'environnement : DB_URL, DB_USER, DB_PASSWORD.
 */
public final class DatabaseConnection {

    private static final String URL = env("DB_URL",
            "jdbc:mysql://localhost:3306/budgetflow?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
    private static final String USER = env("DB_USER", "root");
    private static final String PASSWORD = env("DB_PASSWORD", "");

    private DatabaseConnection() {
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
