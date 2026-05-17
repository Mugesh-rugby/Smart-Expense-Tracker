package database;

import model.Category;
import model.Category.CategoryType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CategoryDAO - CRUD operations for expense/income categories.
 */
public class CategoryDAO {

    private Connection conn;

    public CategoryDAO() throws SQLException {
        this.conn = DBConnection.getInstance().getConnection();
    }

    /** Returns all categories of a given type (EXPENSE or INCOME). */
    public List<Category> getByType(CategoryType type) throws SQLException {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT id, name, icon, color_hex, description, type "
                   + "FROM categories WHERE type = ? OR type = 'BOTH' ORDER BY name";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapCategory(rs));
            }
        }
        return list;
    }

    /** Returns all categories regardless of type. */
    public List<Category> getAll() throws SQLException {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT id, name, icon, color_hex, description, type FROM categories ORDER BY type, name";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapCategory(rs));
        }
        return list;
    }

    /** Returns only expense categories. */
    public List<Category> getExpenseCategories() throws SQLException {
        return getByType(CategoryType.EXPENSE);
    }

    /** Returns only income categories. */
    public List<Category> getIncomeCategories() throws SQLException {
        return getByType(CategoryType.INCOME);
    }

    public Category getById(int id) throws SQLException {
        String sql = "SELECT id, name, icon, color_hex, description, type FROM categories WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapCategory(rs);
            }
        }
        return null;
    }

    // ── Mapping ────────────────────────────────────────────────────────────

    private Category mapCategory(ResultSet rs) throws SQLException {
        return new Category(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("icon"),
            rs.getString("color_hex"),
            rs.getString("description"),
            CategoryType.valueOf(rs.getString("type"))
        );
    }
}
