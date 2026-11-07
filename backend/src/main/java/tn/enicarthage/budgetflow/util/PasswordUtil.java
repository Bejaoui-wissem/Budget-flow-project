package tn.enicarthage.budgetflow.util;

import org.mindrot.jbcrypt.BCrypt;

/** Hachage des mots de passe avec BCrypt (jamais de mot de passe en clair en base). */
public final class PasswordUtil {

    private PasswordUtil() {
    }

    public static String hash(String plain) {
        return BCrypt.hashpw(plain, BCrypt.gensalt(10));
    }

    public static boolean matches(String plain, String hash) {
        try {
            return BCrypt.checkpw(plain, hash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
