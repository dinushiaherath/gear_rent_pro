package lk.ijse.gear_rent_pro.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lk.ijse.gear_rent_pro.dao.RentalDao;
import lk.ijse.gear_rent_pro.model.PaymentStatus;
import lk.ijse.gear_rent_pro.model.Rental;
import lk.ijse.gear_rent_pro.model.RentalStatus;
import lk.ijse.gear_rent_pro.util.TransactionManager;

public class RentalDaoImpl implements RentalDao {

    @Override
    public Rental save(Rental rental) throws SQLException {
        String sql = "INSERT INTO rental (rental_code, equipment_id, customer_id, branch_id, start_date, end_date, calculated_rental_amount, security_deposit, membership_discount, long_rental_discount, final_payable_amount, payment_status, rental_status) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        Connection conn = TransactionManager.getConnection();
        try (PreparedStatement p = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setString(1, rental.getRentalId());
            p.setInt(2, rental.getEquipmentId());
            p.setInt(3, rental.getCustomerId());
            p.setInt(4, rental.getBranchId());
            p.setObject(5, rental.getStartDate());
            p.setObject(6, rental.getEndDate());
            p.setDouble(7, rental.getCalculatedRentalAmount());
            p.setDouble(8, rental.getSecurityDeposit());
            p.setDouble(9, rental.getMembershipDiscount());
            p.setDouble(10, rental.getLongRentalDiscount());
            p.setDouble(11, rental.getFinalPayableAmount());
            p.setString(12, rental.getPaymentStatus().name());
            p.setString(13, rental.getRentalStatus().name());
            int affected = p.executeUpdate();
            if (affected == 0) return null;
            try (ResultSet rs = p.getGeneratedKeys()) { if (rs.next()) rental.setId(rs.getInt(1)); }
            return rental;
        }
    }

    @Override
    public boolean update(Rental rental) throws SQLException {
        String sql = "UPDATE rental SET equipment_id=?, customer_id=?, branch_id=?, start_date=?, end_date=?, calculated_rental_amount=?, security_deposit=?, membership_discount=?, long_rental_discount=?, final_payable_amount=?, payment_status=?, rental_status=?, actual_return_date=?, damage_description=?, damage_charge=?, late_fee=? WHERE id=?";
        Connection conn = TransactionManager.getConnection();
        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, rental.getEquipmentId());
            p.setInt(2, rental.getCustomerId());
            p.setInt(3, rental.getBranchId());
            p.setObject(4, rental.getStartDate());
            p.setObject(5, rental.getEndDate());
            p.setDouble(6, rental.getCalculatedRentalAmount());
            p.setDouble(7, rental.getSecurityDeposit());
            p.setDouble(8, rental.getMembershipDiscount());
            p.setDouble(9, rental.getLongRentalDiscount());
            p.setDouble(10, rental.getFinalPayableAmount());
            p.setString(11, rental.getPaymentStatus().name());
            p.setString(12, rental.getRentalStatus().name());
            p.setObject(13, rental.getActualReturnDate());
            p.setString(14, rental.getDamageDescription());
            p.setDouble(15, rental.getDamageCharge());
            p.setDouble(16, rental.getLateFee());
            p.setInt(17, rental.getId());
            return p.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteById(int id) throws SQLException {
        String sql = "DELETE FROM rental WHERE id=?";
        Connection conn = TransactionManager.getConnection();
        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, id);
            return p.executeUpdate() > 0;
        }
    }

    @Override
    public Rental findById(int id) throws SQLException {
        String sql = "SELECT * FROM rental WHERE id=?";
        Connection conn = TransactionManager.getConnection();
        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, id);
            try (ResultSet rs = p.executeQuery()) { if (rs.next()) return map(rs); }
        }
        return null;
    }

    @Override
    public List<Rental> findAll() throws SQLException {
        String sql = "SELECT * FROM rental";
        List<Rental> list = new ArrayList<>();
        Connection conn = TransactionManager.getConnection();
        try (PreparedStatement p = conn.prepareStatement(sql); ResultSet rs = p.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    @Override
    public int markOverdueBefore(java.time.LocalDate date) throws SQLException {
        String sql = "UPDATE rental SET rental_status=? WHERE rental_status=? AND end_date < ?";
        Connection conn = TransactionManager.getConnection();
        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, lk.ijse.gear_rent_pro.model.RentalStatus.OVERDUE.name());
            p.setString(2, lk.ijse.gear_rent_pro.model.RentalStatus.ACTIVE.name());
            p.setObject(3, date);
            return p.executeUpdate();
        }
    }

    private Rental map(ResultSet rs) throws SQLException {
        Rental r = new Rental();
        r.setId(rs.getInt("id"));
        r.setRentalId(rs.getString("rental_code"));
        r.setEquipmentId(rs.getInt("equipment_id"));
        r.setCustomerId(rs.getInt("customer_id"));
        r.setBranchId(rs.getInt("branch_id"));
        r.setStartDate(rs.getObject("start_date", LocalDate.class));
        r.setEndDate(rs.getObject("end_date", LocalDate.class));
        r.setCalculatedRentalAmount(rs.getDouble("calculated_rental_amount"));
        r.setSecurityDeposit(rs.getDouble("security_deposit"));
        r.setMembershipDiscount(rs.getDouble("membership_discount"));
        r.setLongRentalDiscount(rs.getDouble("long_rental_discount"));
        r.setFinalPayableAmount(rs.getDouble("final_payable_amount"));
        r.setPaymentStatus(PaymentStatus.valueOf(rs.getString("payment_status")));
        r.setRentalStatus(RentalStatus.valueOf(rs.getString("rental_status")));
        r.setActualReturnDate(rs.getObject("actual_return_date", LocalDate.class));
        r.setDamageDescription(rs.getString("damage_description"));
        r.setDamageCharge(rs.getDouble("damage_charge"));
        r.setLateFee(rs.getDouble("late_fee"));
        return r;
    }
}
