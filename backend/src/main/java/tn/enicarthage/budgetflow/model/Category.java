package tn.enicarthage.budgetflow.model;

/** Catégorie de dépenses. "personnalisee" vaut false pour les catégories par défaut. */
public record Category(int id, String nom, String description, boolean personnalisee) {
}
