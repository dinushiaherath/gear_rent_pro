package lk.ijse.gear_rent_pro.service;

import java.util.List;
import lk.ijse.gear_rent_pro.model.Customer;

public interface CustomerService {
    String generateCustomerId();
    Customer create(Customer customer);
    boolean update(Customer customer);
    boolean delete(int id);
    Customer findById(int id);
    List<Customer> findAll();
}
