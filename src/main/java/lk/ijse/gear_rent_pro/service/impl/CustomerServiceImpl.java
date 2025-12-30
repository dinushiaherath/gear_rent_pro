package lk.ijse.gear_rent_pro.service.impl;

import java.sql.SQLException;
import java.util.List;
import lk.ijse.gear_rent_pro.dao.CustomerDao;
import lk.ijse.gear_rent_pro.dao.impl.CustomerDaoImpl;
import lk.ijse.gear_rent_pro.model.Customer;
import lk.ijse.gear_rent_pro.service.CustomerService;

public class CustomerServiceImpl implements CustomerService {

    private final CustomerDao dao = new CustomerDaoImpl();

    @Override
    public String generateCustomerId() {
        try {
            List<Customer> all = dao.findAll();
            long count = all.size() + 1;
            return String.format("CUST-%03d", count);
        } catch (SQLException e) {
            return "CUST-" + System.currentTimeMillis() % 1000;
        }
    }

    @Override
    public Customer create(Customer customer) {
        try { return dao.save(customer); } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public boolean update(Customer customer) {
        try { return dao.update(customer); } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public boolean delete(int id) {
        try { return dao.deleteById(id); } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public Customer findById(int id) {
        try { return dao.findById(id); } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<Customer> findAll() {
        try { return dao.findAll(); } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
