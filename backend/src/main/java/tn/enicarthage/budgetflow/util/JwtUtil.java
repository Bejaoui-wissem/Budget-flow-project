package tn.enicarthage.budgetflow.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import tn.enicarthage.budgetflow.exception.ApiException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

/** Jeton JWT HS256 (signature HMAC-SHA256), durée de vie : 2 heures. */
public final class JwtUtil {

    private static final String SECRET =
            System.getenv().getOrDefault("JWT_SECRET", "dev-secret-a-changer-en-production-32c");
    private static final long TTL_SECONDS = 2 * 60 * 60;
    private static final Base64.Encoder ENC = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DEC = Base64.getUrlDecoder();

    private JwtUtil() {
    }

    public static String generate(int userId, String email) {
        String header = ENC.encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
        JsonObject payload = new JsonObject();
        payload.addProperty("sub", userId);
        payload.addProperty("email", email);
        payload.addProperty("exp", Instant.now().getEpochSecond() + TTL_SECONDS);
        String body = ENC.encodeToString(payload.toString().getBytes(StandardCharsets.UTF_8));
        String unsigned = header + "." + body;
        return unsigned + "." + sign(unsigned);
    }

    /** Vérifie la signature et la date d'expiration, retourne l'identifiant de l'utilisateur. */
    public static int verify(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw ApiException.unauthorized("Jeton invalide");
            }
            String expected = sign(parts[0] + "." + parts[1]);
            if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                    parts[2].getBytes(StandardCharsets.UTF_8))) {
                throw ApiException.unauthorized("Jeton invalide");
            }
            JsonObject payload = JsonParser.parseString(
                    new String(DEC.decode(parts[1]), StandardCharsets.UTF_8)).getAsJsonObject();
            if (payload.get("exp").getAsLong() < Instant.now().getEpochSecond()) {
                throw ApiException.unauthorized("Session expirée, reconnectez-vous");
            }
            return payload.get("sub").getAsInt();
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw ApiException.unauthorized("Jeton invalide");
        }
    }

    private static String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return ENC.encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de signer le jeton", e);
        }
    }
}
