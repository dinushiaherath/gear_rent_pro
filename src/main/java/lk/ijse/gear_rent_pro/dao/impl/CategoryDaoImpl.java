package lk.ijse.gear_rent_pro.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import lk.ijse.gear_rent_pro.dao.CategoryDao;
import lk.ijse.gear_rent_pro.model.Category;
import lk.ijse.gear_rent_pro.util.DBConnection;

public class CategoryDaoImpl implements CategoryDao {

    @Override
    public Category save(Category category) throws SQLException {
        String sql = "INSERT INTO category (name, description, base_price_factor, weekend_multiplier, default_late_fee_per_day, active) VALUES (?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement p = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setString(1, category.getName());
            p.setString(2, category.getDescription());
            p.setDouble(3, category.getBasePriceFactor());
            p.setDouble(4, category.getWeekendMultiplier());
            p.setDouble(5, category.getDefaultLateFeePerDay());
            p.setBoolean(6, category.isActive());
            int affected = p.executeUpdate();
            if (affected == 0) return null;
            try (ResultSet rs = p.getGeneratedKeys()) {
                if (rs.next()) category.setId(rs.getInt(1));
            }
            return category;
        }
    }

    @Override
    public boolean update(Category category) throws SQLException {
        String sql = "UPDATE category SET name=?, description=?, base_price_factor=?, weekend_multiplier=?, default_late_fee_per_day=?, active=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, category.getName());
            p.setString(2, category.getDescription());
            p.setDouble(3, category.getBasePriceFactor());
            p.setDouble(4, category.getWeekendMultiplier());
            p.setDouble(5, category.getDefaultLateFeePerDay());
            p.setBoolean(6, category.isActive());
            p.setInt(7, category.getId());
            return p.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteById(int id) throws SQLException {
        String sql = "DELETE FROM category WHERE id=?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, id);
            return p.executeUpdate() > 0;
        }
    }

    @Override
    public Category findById(int id) throws SQLException {
        String sql = "SELECT id, name, description, base_price_factor, weekend_multiplier, default_late_fee_per_day, active FROM category WHERE id=?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, id);
            try (ResultSet rs = p.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    @Override
    public List<Category> findAll() throws SQLException {
        String sql = "SELECT id, name, description, base_price_factor, weekend_multiplier, default_late_fee_per_day, active FROM category";
        List<Category> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection(); PreparedStatement p = conn.prepareStatement(sql); ResultSet rs = p.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    private Category map(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setId(rs.getInt("id"));
        c.setName(rs.getString("name"));
        c.setDescription(rs.getString("description"));
        c.setBasePriceFactor(rs.getDouble("base_price_factor"));
        c.setWeekendMultiplier(rs.getDouble("weekend_multiplier"));
        c.setDefaultLateFeePerDay(rs.getDouble("default_late_fee_per_day"));
        c.setActive(rs.getBoolean("active"));
        return c;
    }
}
