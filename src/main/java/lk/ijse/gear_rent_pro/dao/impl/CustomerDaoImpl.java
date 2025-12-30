package lk.ijse.gear_rent_pro.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import lk.ijse.gear_rent_pro.dao.CustomerDao;
import lk.ijse.gear_rent_pro.model.Customer;
import lk.ijse.gear_rent_pro.model.MembershipLevel;
import lk.ijse.gear_rent_pro.util.DBConnection;

public class CustomerDaoImpl implements CustomerDao {

    @Override
    public Customer save(Customer customer) throws SQLException {
        String sql = "INSERT INTO customer (customer_code, name, nic_passport, contact_no, email, address, membership) VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement p = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setString(1, customer.getCustomerId());
            p.setString(2, customer.getName());
            p.setString(3, customer.getNicOrPassport());
            p.setString(4, customer.getContactNo());
            p.setString(5, customer.getEmail());
            p.setString(6, customer.getAddress());
            p.setString(7, customer.getMembership().name());
            int affected = p.executeUpdate();
            if (affected == 0) return null;
            try (ResultSet rs = p.getGeneratedKeys()) { if (rs.next()) customer.setId(rs.getInt(1)); }
            return customer;
        }
    }

    @Override
    public boolean update(Customer customer) throws SQLException {
        String sql = "UPDATE customer SET customer_code=?, name=?, nic_passport=?, contact_no=?, email=?, address=?, membership=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, customer.getCustomerId());
            p.setString(2, customer.getName());
            p.setString(3, customer.getNicOrPassport());
            p.setString(4, customer.getContactNo());
            p.setString(5, customer.getEmail());
            p.setString(6, customer.getAddress());
            p.setString(7, customer.getMembership().name());
            p.setInt(8, customer.getId());
            return p.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteById(int id) throws SQLException {
        String sql = "DELETE FROM customer WHERE id=?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, id);
            return p.executeUpdate() > 0;
        }
    }

    @Override
    public Customer findById(int id) throws SQLException {
        String sql = "SELECT * FROM customer WHERE id=?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, id);
            try (ResultSet rs = p.executeQuery()) { if (rs.next()) return map(rs); }
        }
        return null;
    }

    @Override
    public List<Customer> findAll() throws SQLException {
        String sql = "SELECT * FROM customer";
        List<Customer> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection(); PreparedStatement p = conn.prepareStatement(sql); ResultSet rs = p.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    private Customer map(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setId(rs.getInt("id"));
        c.setCustomerId(rs.getString("customer_code"));
        c.setName(rs.getString("name"));
        c.setNicOrPassport(rs.getString("nic_passport"));
        c.setContactNo(rs.getString("contact_no"));
        c.setEmail(rs.getString("email"));
        c.setAddress(rs.getString("address"));
        String m = rs.getString("membership");
        if (m != null) c.setMembership(MembershipLevel.valueOf(m));
        return c;
    }
}
