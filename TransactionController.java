package controller;

import database.CategoryDAO;
import database.TransactionDAO;
import model.Category;
import model.Transaction;
import model.Transaction.TransactionType;
import utils.ValidationUtil;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * TransactionController - Business logic for income and expense management.
 */
public class TransactionController {

    private TransactionDAO transactionDAO;
    private CategoryDAO    categoryDAO;
    private int            currentUserId;

    public TransactionController(int userId) throws SQLException {
        this.currentUserId  = userId;
        this.transactionDAO = new TransactionDAO();
        this.categoryDAO    = new CategoryDAO();
    }

    // ── Categories ─────────────────────────────────────────────────────────

    public List<Category> getExpenseCategories() throws SQLException {
        return categoryDAO.getExpenseCategories();
    }

    public List<Category> getIncomeCategories() throws SQLException {
        return categoryDAO.getIncomeCategories();
    }

    // ── Add ────────────────────────────────────────────────────────────────

    public int addExpense(Category category, String amountStr, String description,
                          String notes, LocalDate date) throws SQLException {
        validate(category, amountStr, date);
        BigDecimal amount = new BigDecimal(amountStr.trim());
        Transaction t = new Transaction(currentUserId, category, TransactionType.EXPENSE,
                                        amount, description, notes, date);
        return transactionDAO.addTransaction(t);
    }

    public int addIncome(Category category, String amountStr, String description,
                         String notes, LocalDate date) throws SQLException {
        validate(category, amountStr, date);
        BigDecimal amount = new BigDecimal(amountStr.trim());
        Transaction t = new Transaction(currentUserId, category, TransactionType.INCOME,
                                        amount, description, notes, date);
        return transactionDAO.addTransaction(t);
    }

    // ── Update & Delete ────────────────────────────────────────────────────

    public boolean updateTransaction(int txId, Category category, TransactionType type,
                                     String amountStr, String description,
                                     String notes, LocalDate date) throws SQLException {
        validate(category, amountStr, date);
        BigDecimal amount = new BigDecimal(amountStr.trim());
        Transaction t = new Transaction();
        t.setId(txId);
        t.setUserId(currentUserId);
        t.setCategory(category);
        t.setType(type);
        t.setAmount(amount);
        t.setDescription(description);
        t.setNotes(notes);
        t.setTransactionDate(date);
        return transactionDAO.update(t);
    }

    public boolean deleteTransaction(int txId) throws SQLException {
        return transactionDAO.delete(txId, currentUserId);
    }

    // ── Queries ────────────────────────────────────────────────────────────

    public List<Transaction> getRecentTransactions(int limit) throws SQLException {
        return transactionDAO.getRecent(currentUserId, limit);
    }

    public List<Transaction> getMonthTransactions(int month, int year) throws SQLException {
        return transactionDAO.getByMonth(currentUserId, month, year);
    }

    public List<Transaction> getByDateRange(LocalDate from, LocalDate to) throws SQLException {
        return transactionDAO.getByDateRange(currentUserId, from, to);
    }

    public List<Transaction> search(String keyword) throws SQLException {
        if (!ValidationUtil.isNotBlank(keyword))
            throw new IllegalArgumentException("Search keyword cannot be empty.");
        return transactionDAO.search(currentUserId, keyword.trim());
    }

    public Transaction getById(int id) throws SQLException {
        return transactionDAO.getById(id);
    }

    // ── Aggregates ─────────────────────────────────────────────────────────

    public BigDecimal getMonthIncome(int month, int year) throws SQLException {
        return transactionDAO.getTotalIncome(currentUserId, month, year);
    }

    public BigDecimal getMonthExpense(int month, int year) throws SQLException {
        return transactionDAO.getTotalExpense(currentUserId, month, year);
    }

    public BigDecimal getMonthBalance(int month, int year) throws SQLException {
        return getMonthIncome(month, year).subtract(getMonthExpense(month, year));
    }

    public BigDecimal getAllTimeBalance() throws SQLException {
        BigDecimal income  = transactionDAO.getAllTimeIncome(currentUserId);
        BigDecimal expense = transactionDAO.getAllTimeExpense(currentUserId);
        return income.subtract(expense);
    }

    public Map<String, BigDecimal> getCategoryExpenses(int month, int year) throws SQLException {
        return transactionDAO.getCategoryExpenses(currentUserId, month, year);
    }

    public Map<String, BigDecimal> getMonthlyTrend(int months) throws SQLException {
        return transactionDAO.getMonthlyTrend(currentUserId, months);
    }

    // ── Validation ─────────────────────────────────────────────────────────

    private void validate(Category category, String amountStr, LocalDate date) {
        if (category == null)
            throw new IllegalArgumentException("Please select a category.");
        if (!ValidationUtil.isValidAmount(amountStr))
            throw new IllegalArgumentException("Please enter a valid positive amount.");
        if (date == null)
            throw new IllegalArgumentException("Please select a date.");
        if (date.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Transaction date cannot be in the future.");
    }
}
