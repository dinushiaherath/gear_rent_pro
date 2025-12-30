package lk.ijse.gear_rent_pro.util;

/**
 * Utility to generate password hashes for database insertion.
 * Run this to generate hashes for default passwords.
 */
public class PasswordHashGenerator {
    public static void main(String[] args) {
        String password = "admin123"; // Default password for all test users
        String hash = PasswordUtil.hashPassword(password);
        System.out.println("Password: " + password);
        System.out.println("Hash: " + hash);
        System.out.println("\nUse the hash above in the INSERT statements in schema.sql");
        
        // Verify it works
        boolean verified = PasswordUtil.verifyPassword(password, hash);
        System.out.println("Verification test: " + (verified ? "PASSED" : "FAILED"));
    }
}
