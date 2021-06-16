package nl.utwente.m4.lossprevention.JWT;

import java.util.HashMap;

public enum TokenGarage {
    instance;
    // stores all the token with it's corresponding email
    private static HashMap<String , String> tokenUser = new HashMap<>();

    public static void storeToken(String token, String email){
        tokenUser.put(token,email);
    }

    public static String getTokenUser(String token){
        return tokenUser.get(token);
    }
}
