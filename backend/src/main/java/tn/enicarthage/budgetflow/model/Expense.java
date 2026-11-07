package tn.enicarthage.budgetflow.model;

import java.math.BigDecimal;

/** Dépense ; la date est au format ISO (yyyy-MM-dd). */
public record Expense(int id, BigDecimal montant, String date, String description,
                      String modePaiement, int categoryId, String categoryName) {
}
