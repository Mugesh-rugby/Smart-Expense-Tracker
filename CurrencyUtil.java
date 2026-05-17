package utils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * CurrencyUtil - Helpers for currency formatting and parsing.
 */
public final class CurrencyUtil {

    private static final NumberFormat INR_FORMAT =
        NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    private CurrencyUtil() {}

    /**
     * Formats a BigDecimal amount as Indian Rupee currency string.
     * e.g., 12345.67 → "₹12,345.67"
     */
    public static String format(BigDecimal amount) {
        if (amount == null) return "₹0.00";
        return INR_FORMAT.format(amount);
    }

    /** Formats a double amount. */
    public static String format(double amount) {
        return format(BigDecimal.valueOf(amount));
    }

    /**
     * Parses a currency string back to BigDecimal.
     * Strips ₹, commas, and whitespace before parsing.
     */
    public static BigDecimal parse(String text) {
        if (text == null || text.isBlank()) return BigDecimal.ZERO;
        String cleaned = text.replaceAll("[₹,\\s]", "").trim();
        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    /** Returns a compact format for large numbers (1K, 1M). */
    public static String compact(BigDecimal amount) {
        if (amount == null) return "₹0";
        double val = amount.doubleValue();
        if (val >= 1_000_000) return String.format("₹%.1fM", val / 1_000_000);
        if (val >= 1_000)     return String.format("₹%.1fK", val / 1_000);
        return format(amount);
    }
}
