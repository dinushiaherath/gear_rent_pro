package lk.ijse.gear_rent_pro.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import lk.ijse.gear_rent_pro.dao.BranchDao;
import lk.ijse.gear_rent_pro.model.Branch;
import lk.ijse.gear_rent_pro.util.DBConnection;

public class BranchDaoImpl implements BranchDao {

    @Override
    public Branch save(Branch branch) throws SQLException {
        String sql = "INSERT INTO branch (code, name, address, contact) VALUES (?,?,?,?)";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement p = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setString(1, branch.getCode());
            p.setString(2, branch.getName());
            p.setString(3, branch.getAddress());
            p.setString(4, branch.getContact());
            int affected = p.executeUpdate();
            if (affected == 0) return null;
            try (ResultSet rs = p.getGeneratedKeys()) {
                if (rs.next()) branch.setId(rs.getInt(1));
            }
            return branch;
        }
    }

    @Override
    public boolean update(Branch branch) throws SQLException {
        String sql = "UPDATE branch SET code=?, name=?, address=?, contact=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, branch.getCode());
            p.setString(2, branch.getName());
            p.setString(3, branch.getAddress());
            p.setString(4, branch.getContact());
            p.setInt(5, branch.getId());
            return p.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteById(int id) throws SQLException {
        String sql = "DELETE FROM branch WHERE id=?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, id);
            return p.executeUpdate() > 0;
        }
    }

    @Override
    public Branch findById(int id) throws SQLException {
        String sql = "SELECT id, code, name, address, contact FROM branch WHERE id=?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, id);
            try (ResultSet rs = p.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Branch> findAll() throws SQLException {
        String sql = "SELECT id, code, name, address, contact FROM branch";
        List<Branch> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection(); PreparedStatement p = conn.prepareStatement(sql); ResultSet rs = p.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    private Branch map(ResultSet rs) throws SQLException {
        Branch b = new Branch();
        b.setId(rs.getInt("id"));
        b.setCode(rs.getString("code"));
        b.setName(rs.getString("name"));
        b.setAddress(rs.getString("address"));
        b.setContact(rs.getString("contact"));
        return b;
    }
}
