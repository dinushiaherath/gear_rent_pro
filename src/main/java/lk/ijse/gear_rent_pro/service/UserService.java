package lk.ijse.gear_rent_pro.service;

import java.util.List;
import lk.ijse.gear_rent_pro.model.User;

public interface UserService {
    User create(User user);
    boolean update(User user);
    boolean delete(int id);
    User findById(int id);
    User findByUsername(String username);
    List<User> findAll();
}
