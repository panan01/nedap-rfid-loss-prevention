package nl.utwente.m4.lossprevention.register;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public enum PasswordHasher {
    instance;
    private final String SALT = "$1$N8qsKOcF$dWj1idimpoRJbyVJhU4uk1";

    public byte[] hashPassword(String password){
        KeySpec spec = new PBEKeySpec(password.toCharArray(), this.SALT.getBytes(), 65536, 128);
        try {
            SecretKeyFactory fact = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return fact.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }
}
