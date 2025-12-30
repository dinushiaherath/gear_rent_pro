package lk.ijse.gear_rent_pro.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import lk.ijse.gear_rent_pro.dao.EquipmentDao;
import lk.ijse.gear_rent_pro.model.Equipment;
import lk.ijse.gear_rent_pro.model.EquipmentStatus;
import lk.ijse.gear_rent_pro.util.DBConnection;
import lk.ijse.gear_rent_pro.util.TransactionManager;

public class EquipmentDaoImpl implements EquipmentDao {

    @Override
    public Equipment save(Equipment equipment) throws SQLException {
        String sql = "INSERT INTO equipment (equipment_code, category_id, brand, model, purchase_year, base_daily_price, security_deposit, status, branch_id) VALUES (?,?,?,?,?,?,?,?,?)";
        Connection conn = TransactionManager.getConnection();
        try (PreparedStatement p = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setString(1, equipment.getEquipmentId());
            p.setInt(2, equipment.getCategoryId());
            p.setString(3, equipment.getBrand());
            p.setString(4, equipment.getModel());
            p.setInt(5, equipment.getPurchaseYear());
            p.setDouble(6, equipment.getBaseDailyPrice());
            p.setDouble(7, equipment.getSecurityDeposit());
            p.setString(8, equipment.getStatus().name());
            p.setInt(9, equipment.getBranchId());
            int affected = p.executeUpdate();
            if (affected == 0) return null;
            try (ResultSet rs = p.getGeneratedKeys()) {
                if (rs.next()) equipment.setId(rs.getInt(1));
            }
            return equipment;
        }
    }

    @Override
    public boolean update(Equipment equipment) throws SQLException {
        String sql = "UPDATE equipment SET equipment_code=?, category_id=?, brand=?, model=?, purchase_year=?, base_daily_price=?, security_deposit=?, status=?, branch_id=? WHERE id=?";
        Connection conn = TransactionManager.getConnection();
        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, equipment.getEquipmentId());
            p.setInt(2, equipment.getCategoryId());
            p.setString(3, equipment.getBrand());
            p.setString(4, equipment.getModel());
            p.setInt(5, equipment.getPurchaseYear());
            p.setDouble(6, equipment.getBaseDailyPrice());
            p.setDouble(7, equipment.getSecurityDeposit());
            p.setString(8, equipment.getStatus().name());
            p.setInt(9, equipment.getBranchId());
            p.setInt(10, equipment.getId());
            return p.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteById(int id) throws SQLException {
        String sql = "DELETE FROM equipment WHERE id=?";
        Connection conn = TransactionManager.getConnection();
        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, id);
            return p.executeUpdate() > 0;
        }
    }

    @Override
    public Equipment findById(int id) throws SQLException {
        String sql = "SELECT * FROM equipment WHERE id=?";
        Connection conn = TransactionManager.getConnection();
        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, id);
            try (ResultSet rs = p.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    @Override
    public List<Equipment> findAll() throws SQLException {
        String sql = "SELECT * FROM equipment";
        List<Equipment> list = new ArrayList<>();
        Connection conn = TransactionManager.getConnection();
        try (PreparedStatement p = conn.prepareStatement(sql); ResultSet rs = p.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    @Override
    public List<Equipment> findByBranchId(int branchId) throws SQLException {
        String sql = "SELECT * FROM equipment WHERE branch_id = ?";
        List<Equipment> list = new ArrayList<>();
        Connection conn = TransactionManager.getConnection();
        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, branchId);
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    private Equipment map(ResultSet rs) throws SQLException {
        Equipment e = new Equipment();
        e.setId(rs.getInt("id"));
        e.setEquipmentId(rs.getString("equipment_code"));
        e.setCategoryId(rs.getInt("category_id"));
        e.setBrand(rs.getString("brand"));
        e.setModel(rs.getString("model"));
        e.setPurchaseYear(rs.getInt("purchase_year"));
        e.setBaseDailyPrice(rs.getDouble("base_daily_price"));
        e.setSecurityDeposit(rs.getDouble("security_deposit"));
        e.setStatus(EquipmentStatus.valueOf(rs.getString("status")));
        e.setBranchId(rs.getInt("branch_id"));
        return e;
    }
}
