package model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Budget - Represents a monthly budget limit for a category.
 */
public class Budget {

    private int        id;
    private int        userId;
    private Category   category;
    private BigDecimal amount;
    private int        month;
    private int        year;
    private BigDecimal spent; // runtime field (not stored in DB)

    // ── Constructors ───────────────────────────────────────────────────────

    public Budget() {}

    public Budget(int userId, Category category, BigDecimal amount, int month, int year) {
        this.userId   = userId;
        this.category = category;
        this.amount   = amount;
        this.month    = month;
        this.year     = year;
        this.spent    = BigDecimal.ZERO;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────

    public int        getId()           { return id; }
    public void       setId(int id)     { this.id = id; }

    public int        getUserId()             { return userId; }
    public void       setUserId(int userId)   { this.userId = userId; }

    public Category   getCategory()                    { return category; }
    public void       setCategory(Category category)   { this.category = category; }

    public BigDecimal getAmount()                   { return amount; }
    public void       setAmount(BigDecimal amount)  { this.amount = amount; }

    public int        getMonth()            { return month; }
    public void       setMonth(int month)   { this.month = month; }

    public int        getYear()           { return year; }
    public void       setYear(int year)   { this.year = year; }

    public BigDecimal getSpent()                  { return spent != null ? spent : BigDecimal.ZERO; }
    public void       setSpent(BigDecimal spent)  { this.spent = spent; }

    /** Returns remaining budget (may be negative if over-budget). */
    public BigDecimal getRemaining() {
        return amount.subtract(getSpent());
    }

    /** Returns percentage of budget consumed (0–100+). */
    public double getUsagePercent() {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) return 0;
        return getSpent().divide(amount, 4, RoundingMode.HALF_UP)
                         .multiply(BigDecimal.valueOf(100))
                         .doubleValue();
    }

    /** Returns true when spending exceeds 90% of budget. */
    public boolean isNearLimit() { return getUsagePercent() >= 90; }

    /** Returns true when budget is exceeded. */
    public boolean isExceeded()  { return getSpent().compareTo(amount) > 0; }
}
