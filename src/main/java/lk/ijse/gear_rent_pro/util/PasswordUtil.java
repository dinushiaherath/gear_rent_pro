package lk.ijse.gear_rent_pro.util;

import java.security.MessageDigest;
import java.security.SecureRandom;

import java.util.Base64;

public class PasswordUtil {

    private static final int ITERATIONS = 10000;

    public static String hashPassword(String password) {
        try {
            byte[] salt = new byte[16];
            SecureRandom random = new SecureRandom();
            random.nextBytes(salt);

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hash = md.digest(password.getBytes());

            for (int i = 0; i < ITERATIONS; i++) {
                md.reset();
                md.update(hash);
                hash = md.digest();
            }

            byte[] saltAndHash = new byte[salt.length + hash.length];
            System.arraycopy(salt, 0, saltAndHash, 0, salt.length);
            System.arraycopy(hash, 0, saltAndHash, salt.length, hash.length);

            return Base64.getEncoder().encodeToString(saltAndHash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean verifyPassword(String password, String hash) {
        try {
            byte[] saltAndHash = Base64.getDecoder().decode(hash);
            byte[] salt = new byte[16];
            System.arraycopy(saltAndHash, 0, salt, 0, 16);

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] computedHash = md.digest(password.getBytes());

            for (int i = 0; i < ITERATIONS; i++) {
                md.reset();
                md.update(computedHash);
                computedHash = md.digest();
            }

            byte[] storedHash = new byte[saltAndHash.length - 16];
            System.arraycopy(saltAndHash, 16, storedHash, 0, storedHash.length);

            return MessageDigest.isEqual(computedHash, storedHash);
        } catch (Exception e) {
            return false;
        }
    }
}
