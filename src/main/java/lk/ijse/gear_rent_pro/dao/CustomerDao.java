package lk.ijse.gear_rent_pro.dao;

import java.sql.SQLException;
import java.util.List;
import lk.ijse.gear_rent_pro.model.Customer;

public interface CustomerDao {
    Customer save(Customer customer) throws SQLException;
    boolean update(Customer customer) throws SQLException;
    boolean deleteById(int id) throws SQLException;
    Customer findById(int id) throws SQLException;
    List<Customer> findAll() throws SQLException;
}
