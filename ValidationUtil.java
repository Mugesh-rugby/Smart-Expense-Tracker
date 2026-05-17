package utils;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * ValidationUtil - Input validation helpers used across forms.
 */
public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern USERNAME_PATTERN =
        Pattern.compile("^[A-Za-z0-9_]{3,30}$");

    private ValidationUtil() {}

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username.trim()).matches();
    }

    public static boolean isValidAmount(String text) {
        if (text == null || text.isBlank()) return false;
        try {
            BigDecimal val = new BigDecimal(text.trim());
            return val.compareTo(BigDecimal.ZERO) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isNotBlank(String text) {
        return text != null && !text.isBlank();
    }

    public static boolean isValidName(String name) {
        return name != null && name.trim().length() >= 2 && name.trim().length() <= 100;
    }

    /** Returns a CSS-like color for a password strength indicator (0-100). */
    public static int passwordStrengthScore(String password) {
        if (password == null || password.length() < 4) return 0;
        int score = 0;
        if (password.length() >= 8)  score += 25;
        if (password.length() >= 12) score += 10;
        if (password.chars().anyMatch(Character::isUpperCase)) score += 25;
        if (password.chars().anyMatch(Character::isDigit))     score += 20;
        if (password.chars().anyMatch(c ->
            "!@#$%^&*()_+-=[]{}|;:,.<>?".indexOf(c) >= 0))    score += 20;
        return Math.min(score, 100);
    }
}
