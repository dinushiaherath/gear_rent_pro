package lk.ijse.gear_rent_pro.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lk.ijse.gear_rent_pro.dao.ReservationDao;
import lk.ijse.gear_rent_pro.model.Reservation;
import lk.ijse.gear_rent_pro.util.DBConnection;
import lk.ijse.gear_rent_pro.util.TransactionManager;

public class ReservationDaoImpl implements ReservationDao {

    @Override
    public Reservation save(Reservation reservation) throws SQLException {
        String sql = "INSERT INTO reservation (reservation_code, equipment_id, customer_id, branch_id, start_date, end_date) VALUES (?,?,?,?,?,?)";
        Connection conn = TransactionManager.getConnection();
        try (PreparedStatement p = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setString(1, reservation.getReservationId());
            p.setInt(2, reservation.getEquipmentId());
            p.setInt(3, reservation.getCustomerId());
            p.setInt(4, reservation.getBranchId());
            p.setObject(5, reservation.getStartDate());
            p.setObject(6, reservation.getEndDate());
            int affected = p.executeUpdate();
            if (affected == 0) return null;
            try (ResultSet rs = p.getGeneratedKeys()) { if (rs.next()) reservation.setId(rs.getInt(1)); }
            return reservation;
        }
    }

    @Override
    public boolean update(Reservation reservation) throws SQLException {
        String sql = "UPDATE reservation SET reservation_code=?, equipment_id=?, customer_id=?, branch_id=?, start_date=?, end_date=? WHERE id=?";
        Connection conn = TransactionManager.getConnection();
        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, reservation.getReservationId());
            p.setInt(2, reservation.getEquipmentId());
            p.setInt(3, reservation.getCustomerId());
            p.setInt(4, reservation.getBranchId());
            p.setObject(5, reservation.getStartDate());
            p.setObject(6, reservation.getEndDate());
            p.setInt(7, reservation.getId());
            return p.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteById(int id) throws SQLException {
        String sql = "DELETE FROM reservation WHERE id=?";
        Connection conn = TransactionManager.getConnection();
        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, id);
            return p.executeUpdate() > 0;
        }
    }

    @Override
    public Reservation findById(int id) throws SQLException {
        String sql = "SELECT * FROM reservation WHERE id=?";
        Connection conn = TransactionManager.getConnection();
        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, id);
            try (ResultSet rs = p.executeQuery()) { if (rs.next()) return map(rs); }
        }
        return null;
    }

    @Override
    public List<Reservation> findAll() throws SQLException {
        String sql = "SELECT * FROM reservation";
        List<Reservation> list = new ArrayList<>();
        Connection conn = TransactionManager.getConnection();
        try (PreparedStatement p = conn.prepareStatement(sql); ResultSet rs = p.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    private Reservation map(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setId(rs.getInt("id"));
        r.setReservationId(rs.getString("reservation_code"));
        r.setEquipmentId(rs.getInt("equipment_id"));
        r.setCustomerId(rs.getInt("customer_id"));
        r.setBranchId(rs.getInt("branch_id"));
        r.setStartDate(rs.getObject("start_date", LocalDate.class));
        r.setEndDate(rs.getObject("end_date", LocalDate.class));
        return r;
    }
}
