package nl.utwente.m4.lossprevention.JWT;

import javax.crypto.KeyGenerator;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;

public enum TokenGarage {
    instance;
    // stores all the token with it's corresponding email
    private static HashMap<String , String> tokenUser = new HashMap<>();
    private static Key key;
    private static KeyGenerator gen;
    static {
        try {
            gen = KeyGenerator.getInstance("HmacSHA512");
            gen.init(512, new SecureRandom());
            key = gen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static void storeToken(String token, String email) {
        tokenUser.put(token,email);
    }
    public static String getTokenUser(String token) {
        return tokenUser.get(token);
    }
    public static Key getKey() {
        return key;
    }
}
