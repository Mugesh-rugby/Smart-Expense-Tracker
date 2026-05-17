package model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Transaction - Core entity for income and expense records.
 * Uses BigDecimal for precise monetary arithmetic.
 */
public class Transaction {

    public enum TransactionType { INCOME, EXPENSE }

    private int             id;
    private int             userId;
    private Category        category;
    private TransactionType type;
    private BigDecimal      amount;
    private String          description;
    private String          notes;
    private LocalDate       transactionDate;
    private LocalDateTime   createdAt;

    // ── Constructors ───────────────────────────────────────────────────────

    public Transaction() {}

    public Transaction(int userId, Category category, TransactionType type,
                       BigDecimal amount, String description,
                       String notes, LocalDate transactionDate) {
        this.userId          = userId;
        this.category        = category;
        this.type            = type;
        this.amount          = amount;
        this.description     = description;
        this.notes           = notes;
        this.transactionDate = transactionDate;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────

    public int             getId()            { return id; }
    public void            setId(int id)      { this.id = id; }

    public int             getUserId()               { return userId; }
    public void            setUserId(int userId)     { this.userId = userId; }

    public Category        getCategory()                      { return category; }
    public void            setCategory(Category category)     { this.category = category; }

    public TransactionType getType()                    { return type; }
    public void            setType(TransactionType t)   { this.type = t; }

    public BigDecimal      getAmount()                    { return amount; }
    public void            setAmount(BigDecimal amount)   { this.amount = amount; }

    public String          getDescription()                          { return description; }
    public void            setDescription(String description)        { this.description = description; }

    public String          getNotes()                { return notes; }
    public void            setNotes(String notes)    { this.notes = notes; }

    public LocalDate       getTransactionDate()                          { return transactionDate; }
    public void            setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }

    public LocalDateTime   getCreatedAt()                 { return createdAt; }
    public void            setCreatedAt(LocalDateTime t)  { this.createdAt = t; }

    /** Returns true if this is an expense transaction. */
    public boolean isExpense() { return type == TransactionType.EXPENSE; }

    /** Returns true if this is an income transaction. */
    public boolean isIncome()  { return type == TransactionType.INCOME;  }

    @Override
    public String toString() {
        return "Transaction{id=" + id + ", type=" + type
               + ", amount=" + amount + ", date=" + transactionDate + "}";
    }
}
