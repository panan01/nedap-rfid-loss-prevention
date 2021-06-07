package nl.utwente.m4.lossprevention.login;

import java.security.SecureRandom;
import java.util.Base64;

public class Session {
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();

    private final String token;

    public Session(String email) {
        this.token = generateNewToken();
    }

    public String getToken() {
        return this.token;
    }

    public static String generateNewToken() {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }
}
