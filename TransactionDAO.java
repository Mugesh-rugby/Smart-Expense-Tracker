package database;

import model.Category;
import model.Transaction;
import model.Transaction.TransactionType;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * TransactionDAO - Full CRUD and reporting queries for transactions.
 * All queries use PreparedStatements to prevent SQL injection.
 */
public class TransactionDAO {

    private Connection conn;

    public TransactionDAO() throws SQLException {
        this.conn = DBConnection.getInstance().getConnection();
    }

    // ── Create ─────────────────────────────────────────────────────────────

    public int addTransaction(Transaction t) throws SQLException {
        String sql = "INSERT INTO transactions "
                   + "(user_id, category_id, type, amount, description, notes, transaction_date) "
                   + "VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, t.getUserId());
            ps.setInt(2, t.getCategory().getId());
            ps.setString(3, t.getType().name());
            ps.setBigDecimal(4, t.getAmount());
            ps.setString(5, t.getDescription());
            ps.setString(6, t.getNotes());
            ps.setDate(7, Date.valueOf(t.getTransactionDate()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Insert failed – no key generated.");
    }

    // ── Read ───────────────────────────────────────────────────────────────

    /** Returns recent N transactions for the given user. */
    public List<Transaction> getRecent(int userId, int limit) throws SQLException {
        String sql = buildSelectBase()
                   + "WHERE t.user_id = ? ORDER BY t.transaction_date DESC, t.id DESC LIMIT ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            return fetchList(ps);
        }
    }

    /** Returns all transactions for a user in a given month/year. */
    public List<Transaction> getByMonth(int userId, int month, int year) throws SQLException {
        String sql = buildSelectBase()
                   + "WHERE t.user_id = ? AND MONTH(t.transaction_date) = ? "
                   + "AND YEAR(t.transaction_date) = ? "
                   + "ORDER BY t.transaction_date DESC, t.id DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            return fetchList(ps);
        }
    }

    /** Returns transactions in a date range. */
    public List<Transaction> getByDateRange(int userId, LocalDate from, LocalDate to) throws SQLException {
        String sql = buildSelectBase()
                   + "WHERE t.user_id = ? AND t.transaction_date BETWEEN ? AND ? "
                   + "ORDER BY t.transaction_date DESC, t.id DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(to));
            return fetchList(ps);
        }
    }

    /** Full-text search on description field. */
    public List<Transaction> search(int userId, String keyword) throws SQLException {
        String sql = buildSelectBase()
                   + "WHERE t.user_id = ? AND (t.description LIKE ? OR t.notes LIKE ?) "
                   + "ORDER BY t.transaction_date DESC LIMIT 100";
        String q = "%" + keyword + "%";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, q);
            ps.setString(3, q);
            return fetchList(ps);
        }
    }

    public Transaction getById(int id) throws SQLException {
        String sql = buildSelectBase() + "WHERE t.id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            List<Transaction> list = fetchList(ps);
            return list.isEmpty() ? null : list.get(0);
        }
    }

    // ── Update & Delete ────────────────────────────────────────────────────

    public boolean update(Transaction t) throws SQLException {
        String sql = "UPDATE transactions SET category_id=?, type=?, amount=?, "
                   + "description=?, notes=?, transaction_date=? WHERE id=? AND user_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, t.getCategory().getId());
            ps.setString(2, t.getType().name());
            ps.setBigDecimal(3, t.getAmount());
            ps.setString(4, t.getDescription());
            ps.setString(5, t.getNotes());
            ps.setDate(6, Date.valueOf(t.getTransactionDate()));
            ps.setInt(7, t.getId());
            ps.setInt(8, t.getUserId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int transactionId, int userId) throws SQLException {
        String sql = "DELETE FROM transactions WHERE id = ? AND user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, transactionId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    // ── Aggregations ───────────────────────────────────────────────────────

    /** Returns total income for a given month/year. */
    public BigDecimal getTotalIncome(int userId, int month, int year) throws SQLException {
        return getTotal(userId, "INCOME", month, year);
    }

    /** Returns total expense for a given month/year. */
    public BigDecimal getTotalExpense(int userId, int month, int year) throws SQLException {
        return getTotal(userId, "EXPENSE", month, year);
    }

    /** Returns total of all income ever recorded for the user. */
    public BigDecimal getAllTimeIncome(int userId) throws SQLException {
        return getSumByType(userId, "INCOME");
    }

    /** Returns total of all expenses ever recorded for the user. */
    public BigDecimal getAllTimeExpense(int userId) throws SQLException {
        return getSumByType(userId, "EXPENSE");
    }

    /**
     * Returns category-wise expense totals for the given month.
     * Map: category name → total amount
     */
    public Map<String, BigDecimal> getCategoryExpenses(int userId, int month, int year)
            throws SQLException {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        String sql = "SELECT c.name, SUM(t.amount) AS total "
                   + "FROM transactions t JOIN categories c ON t.category_id = c.id "
                   + "WHERE t.user_id = ? AND t.type = 'EXPENSE' "
                   + "AND MONTH(t.transaction_date) = ? AND YEAR(t.transaction_date) = ? "
                   + "GROUP BY c.name ORDER BY total DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("name"), rs.getBigDecimal("total"));
                }
            }
        }
        return map;
    }

    /**
     * Returns monthly expense totals for the past N months.
     * Map: "MMM yyyy" → total amount
     */
    public Map<String, BigDecimal> getMonthlyTrend(int userId, int months) throws SQLException {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        String sql = "SELECT DATE_FORMAT(transaction_date, '%b %Y') AS period, "
                   + "SUM(amount) AS total "
                   + "FROM transactions "
                   + "WHERE user_id = ? AND type = 'EXPENSE' "
                   + "AND transaction_date >= DATE_SUB(CURDATE(), INTERVAL ? MONTH) "
                   + "GROUP BY YEAR(transaction_date), MONTH(transaction_date) "
                   + "ORDER BY YEAR(transaction_date), MONTH(transaction_date)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, months);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("period"), rs.getBigDecimal("total"));
                }
            }
        }
        return map;
    }

    // ── Private helpers ────────────────────────────────────────────────────

    private String buildSelectBase() {
        return "SELECT t.id, t.user_id, t.type, t.amount, t.description, t.notes, "
             + "t.transaction_date, t.created_at, "
             + "c.id AS cat_id, c.name AS cat_name, c.icon, c.color_hex, c.description AS cat_desc, c.type AS cat_type "
             + "FROM transactions t JOIN categories c ON t.category_id = c.id ";
    }

    private List<Transaction> fetchList(PreparedStatement ps) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapTransaction(rs));
        }
        return list;
    }

    private Transaction mapTransaction(ResultSet rs) throws SQLException {
        Category cat = new Category(
            rs.getInt("cat_id"),
            rs.getString("cat_name"),
            rs.getString("icon"),
            rs.getString("color_hex"),
            rs.getString("cat_desc"),
            Category.CategoryType.valueOf(rs.getString("cat_type"))
        );
        Transaction t = new Transaction();
        t.setId(rs.getInt("id"));
        t.setUserId(rs.getInt("user_id"));
        t.setCategory(cat);
        t.setType(TransactionType.valueOf(rs.getString("type")));
        t.setAmount(rs.getBigDecimal("amount"));
        t.setDescription(rs.getString("description"));
        t.setNotes(rs.getString("notes"));
        t.setTransactionDate(rs.getDate("transaction_date").toLocalDate());
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) t.setCreatedAt(ts.toLocalDateTime());
        return t;
    }

    private BigDecimal getTotal(int userId, String type, int month, int year) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount),0) FROM transactions "
                   + "WHERE user_id=? AND type=? AND MONTH(transaction_date)=? AND YEAR(transaction_date)=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, type);
            ps.setInt(3, month);
            ps.setInt(4, year);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        }
    }

    private BigDecimal getSumByType(int userId, String type) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount),0) FROM transactions WHERE user_id=? AND type=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, type);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        }
    }
}
