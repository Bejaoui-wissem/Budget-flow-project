package tn.enicarthage.budgetflow;

import tn.enicarthage.budgetflow.config.DatabaseConnection;

import java.sql.Connection;

public class Main {

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));

        // Vérifie la base de données dès le démarrage pour un message d'erreur clair
        try (Connection c = DatabaseConnection.getConnection()) {
            System.out.println("Connexion a la base de donnees : OK");
        } catch (Exception e) {
            System.err.println("Impossible de se connecter a la base de donnees : " + e.getMessage());
            System.err.println("Verifiez que MySQL est demarre, que database.sql a ete execute "
                    + "et les variables DB_URL, DB_USER, DB_PASSWORD.");
            System.exit(1);
        }

        ApiServer server = new ApiServer(port);
        server.start();
        System.out.println("BudgetFlow demarre : http://localhost:" + port);
    }
}
