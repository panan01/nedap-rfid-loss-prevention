package nl.utwente.m4.lossprevention.JWT;



import org.json.JSONArray;
import org.json.JSONObject;
import org.postgresql.util.Base64;

import javax.crypto.KeyGenerator;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;

public enum JWTOffice {
    instance;

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

    public static String getTokenUser(String token) {
        return getJWTBody(token).getString("sub");
    }

    public static String getUserType(String token) {
        return getJWTBody(token).getString("aud");
    }

    public static Key getKey() {
        return key;
    }

    private static JSONObject getJWTBody(String token){
        String[] split_string = token.split("\\.");
        String base64EncodedBody = split_string[1];
        byte[] body = Base64.decode(base64EncodedBody);
        String strBody = new String(body);
        strBody += "}";
        JSONObject jobj = new JSONObject(strBody);
        return jobj;
    }

}
