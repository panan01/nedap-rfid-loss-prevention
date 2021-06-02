package nl.utwente.m4.lossprevention.register;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

public enum PasswordHasher {
    instance;
    private final String PEPPER = "$1$N8qsKOcF$dWj1idimpoRJbyVJhU4uk1";

    public byte[] hashPassword(String password, String salt){
        String salted_Pass = salt + password;
        KeySpec spec = new PBEKeySpec(salted_Pass.toCharArray(), this.PEPPER.getBytes(), 65536, 128);
        try {
            SecretKeyFactory fact = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return Base64.getEncoder().encode(fact.generateSecret(spec).getEncoded());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }
}
