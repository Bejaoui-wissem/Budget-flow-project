package tn.enicarthage.budgetflow.util;

import tn.enicarthage.budgetflow.exception.ApiException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/** Contrôles de saisie côté serveur. */
public final class Validator {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private Validator() {
    }

    public static String required(String value, String label, int maxLength) {
        if (value == null || value.isBlank()) {
            throw ApiException.badRequest(label + " obligatoire");
        }
        String trimmed = value.trim();
        if (trimmed.length() > maxLength) {
            throw ApiException.badRequest(label + " trop long (max " + maxLength + " caractères)");
        }
        return trimmed;
    }

    public static String optional(String value, String label, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > maxLength) {
            throw ApiException.badRequest(label + " trop long (max " + maxLength + " caractères)");
        }
        return trimmed;
    }

    public static String email(String value) {
        String email = required(value, "E-mail", 120).toLowerCase();
        if (!EMAIL.matcher(email).matches()) {
            throw ApiException.badRequest("Adresse e-mail invalide");
        }
        return email;
    }

    public static BigDecimal positiveAmount(BigDecimal value) {
        if (value == null) {
            throw ApiException.badRequest("Montant obligatoire");
        }
        if (value.signum() <= 0) {
            throw ApiException.badRequest("Le montant doit être strictement positif");
        }
        if (value.scale() > 2 || value.precision() - value.scale() > 8) {
            throw ApiException.badRequest("Montant invalide (2 décimales maximum)");
        }
        return value;
    }

    public static LocalDate date(String value, String label) {
        if (value == null || value.isBlank()) {
            throw ApiException.badRequest(label + " obligatoire");
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            throw ApiException.badRequest(label + " invalide (format attendu : AAAA-MM-JJ)");
        }
    }
}
