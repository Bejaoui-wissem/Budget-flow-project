package tn.enicarthage.budgetflow.dto;

import java.math.BigDecimal;

public record ExpenseRequest(BigDecimal montant, String date, String description,
                             String modePaiement, Integer categoryId) {
}
