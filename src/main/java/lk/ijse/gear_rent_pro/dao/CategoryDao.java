package lk.ijse.gear_rent_pro.dao;

import java.sql.SQLException;
import java.util.List;
import lk.ijse.gear_rent_pro.model.Category;

public interface CategoryDao {
    Category save(Category category) throws SQLException;
    boolean update(Category category) throws SQLException;
    boolean deleteById(int id) throws SQLException;
    Category findById(int id) throws SQLException;
    List<Category> findAll() throws SQLException;
}
