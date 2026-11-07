package tn.enicarthage.budgetflow.dto;

public record RegisterRequest(String nom, String prenom, String email, String motDePasse,
                              String confirmationMotDePasse) {
}
