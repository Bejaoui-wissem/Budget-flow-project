package tn.enicarthage.budgetflow.model;

/** Utilisateur (le hash du mot de passe n'est jamais renvoyé par l'API). */
public record User(int id, String nom, String prenom, String email, String motDePasse) {
}
