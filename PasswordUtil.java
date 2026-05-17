package utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * PasswordUtil - Utility class for secure password hashing and validation.
 * Uses SHA-256 with salt for password security.
 */
public final class PasswordUtil {

    private static final String HASH_ALGORITHM = "SHA-256";
    private static final String SEPARATOR       = ":";

    // Prevent instantiation
    private PasswordUtil() {}

    /**
     * Hashes a plain-text password with a random salt.
     *
     * @param plainPassword the raw password entered by the user
     * @return salted hash in the format "base64salt:hexhash"
     */
    public static String hashPassword(String plainPassword) {
        try {
            // Generate random 16-byte salt
            SecureRandom random = new SecureRandom();
            byte[] saltBytes = new byte[16];
            random.nextBytes(saltBytes);
            String salt = Base64.getEncoder().encodeToString(saltBytes);

            String hash = computeHash(plainPassword, salt);
            return salt + SEPARATOR + hash;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /**
     * Verifies a plain-text password against a stored salted hash.
     *
     * @param plainPassword the raw password to check
     * @param storedHash    the stored "salt:hash" string
     * @return true if the password matches
     */
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        try {
            String[] parts = storedHash.split(SEPARATOR, 2);
            if (parts.length != 2) {
                // Legacy MD5 hash without salt (demo user seed)
                return legacyMd5Check(plainPassword, storedHash);
            }
            String salt          = parts[0];
            String expectedHash  = parts[1];
            String computedHash  = computeHash(plainPassword, salt);
            return computedHash.equals(expectedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /** Validates password strength: min 8 chars, 1 digit, 1 uppercase, 1 special char. */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) return false;
        boolean hasDigit   = password.chars().anyMatch(Character::isDigit);
        boolean hasUpper   = password.chars().anyMatch(Character::isUpperCase);
        boolean hasSpecial = password.chars().anyMatch(c -> "!@#$%^&*()_+-=[]{}|;:,.<>?".indexOf(c) >= 0);
        return hasDigit && hasUpper && hasSpecial;
    }

    /** Password strength feedback message. */
    public static String getPasswordStrengthMessage(String password) {
        if (password == null || password.length() < 8)
            return "Password must be at least 8 characters";
        if (!password.chars().anyMatch(Character::isUpperCase))
            return "Add at least one uppercase letter";
        if (!password.chars().anyMatch(Character::isDigit))
            return "Add at least one digit";
        if (!password.chars().anyMatch(c -> "!@#$%^&*()_+-=[]{}|;:,.<>?".indexOf(c) >= 0))
            return "Add at least one special character (!@#$%^&*)";
        return "Strong password ✓";
    }

    // ── Private helpers ────────────────────────────────────────────────────

    private static String computeHash(String password, String salt)
            throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
        digest.update(salt.getBytes());
        byte[] hashBytes = digest.digest(password.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /** Fallback for legacy MD5-hashed demo seed in the DB. */
    private static boolean legacyMd5Check(String plain, String stored) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(plain.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString().equals(stored);
        } catch (NoSuchAlgorithmException e) {
            return false;
        }
    }
}
