package lk.ijse.gear_rent_pro.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import lk.ijse.gear_rent_pro.dao.MembershipConfigDao;
import lk.ijse.gear_rent_pro.model.MembershipConfig;
import lk.ijse.gear_rent_pro.model.MembershipLevel;
import lk.ijse.gear_rent_pro.util.DBConnection;

public class MembershipConfigDaoImpl implements MembershipConfigDao {

    @Override
    public List<MembershipConfig> findAll() throws Exception {
        String sql = "SELECT level, discount_percent FROM membership_config";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement p = conn.prepareStatement(sql);
             ResultSet rs = p.executeQuery()) {
            List<MembershipConfig> list = new ArrayList<>();
            while (rs.next()) {
                MembershipLevel level = MembershipLevel.valueOf(rs.getString("level"));
                double percent = rs.getDouble("discount_percent");
                list.add(new MembershipConfig(level, percent));
            }
            return list;
        }
    }

    @Override
    public MembershipConfig findByLevel(MembershipLevel level) throws Exception {
        String sql = "SELECT level, discount_percent FROM membership_config WHERE level = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, level.name());
            try (ResultSet rs = p.executeQuery()) {
                if (rs.next()) {
                    double percent = rs.getDouble("discount_percent");
                    return new MembershipConfig(level, percent);
                }
            }
        }
        return null;
    }

    @Override
    public void saveOrUpdate(MembershipConfig config) throws Exception {
        String sql = "INSERT INTO membership_config (level, discount_percent) VALUES (?, ?) ON DUPLICATE KEY UPDATE discount_percent = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, config.getLevel().name());
            p.setDouble(2, config.getDiscountPercent());
            p.setDouble(3, config.getDiscountPercent());
            p.executeUpdate();
        }
    }
}
