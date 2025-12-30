package lk.ijse.gear_rent_pro.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import lk.ijse.gear_rent_pro.dao.UserDao;
import lk.ijse.gear_rent_pro.model.User;
import lk.ijse.gear_rent_pro.model.UserRole;
import lk.ijse.gear_rent_pro.util.DBConnection;
import lk.ijse.gear_rent_pro.util.TransactionManager;

public class UserDaoImpl implements UserDao {

    @Override
    public User save(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, role, branch_id) VALUES (?,?,?,?)";
        Connection conn = TransactionManager.getConnection();
        try (PreparedStatement p = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setString(1, user.getUsername());
            p.setString(2, user.getPasswordHash());
            p.setString(3, user.getRole().name());
            if (user.getBranchId() != null) p.setInt(4, user.getBranchId()); else p.setNull(4, java.sql.Types.INTEGER);
            int affected = p.executeUpdate();
            if (affected == 0) return null;
            try (ResultSet rs = p.getGeneratedKeys()) { if (rs.next()) user.setId(rs.getInt(1)); }
            return user;
        }
    }

    @Override
    public boolean update(User user) throws SQLException {
        String sql = "UPDATE users SET username=?, password_hash=?, role=?, branch_id=? WHERE id=?";
        Connection conn = TransactionManager.getConnection();
        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, user.getUsername());
            p.setString(2, user.getPasswordHash());
            p.setString(3, user.getRole().name());
            if (user.getBranchId() != null) p.setInt(4, user.getBranchId()); else p.setNull(4, java.sql.Types.INTEGER);
            p.setInt(5, user.getId());
            return p.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteById(int id) throws SQLException {
        String sql = "DELETE FROM users WHERE id=?";
        Connection conn = TransactionManager.getConnection();
        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, id);
            return p.executeUpdate() > 0;
        }
    }

    @Override
    public User findById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id=?";
        Connection conn = TransactionManager.getConnection();
        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, id);
            try (ResultSet rs = p.executeQuery()) { if (rs.next()) return map(rs); }
        }
        return null;
    }

    @Override
    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username=?";
        Connection conn = TransactionManager.getConnection();
        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, username);
            try (ResultSet rs = p.executeQuery()) { if (rs.next()) return map(rs); }
        }
        return null;
    }

    @Override
    public List<User> findAll() throws SQLException {
        String sql = "SELECT * FROM users";
        List<User> list = new ArrayList<>();
        Connection conn = TransactionManager.getConnection();
        try (PreparedStatement p = conn.prepareStatement(sql); ResultSet rs = p.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        String r = rs.getString("role");
        if (r != null) u.setRole(UserRole.valueOf(r));
        int bid = rs.getInt("branch_id");
        if (!rs.wasNull()) u.setBranchId(bid);
        return u;
    }
}
