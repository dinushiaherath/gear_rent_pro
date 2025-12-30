package lk.ijse.gear_rent_pro.dao;

import java.sql.SQLException;
import java.util.List;
import lk.ijse.gear_rent_pro.model.User;

public interface UserDao {
    User save(User user) throws SQLException;
    boolean update(User user) throws SQLException;
    boolean deleteById(int id) throws SQLException;
    User findById(int id) throws SQLException;
    User findByUsername(String username) throws SQLException;
    List<User> findAll() throws SQLException;
}
