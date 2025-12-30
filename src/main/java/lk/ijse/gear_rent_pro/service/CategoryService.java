package lk.ijse.gear_rent_pro.service;

import java.util.List;
import lk.ijse.gear_rent_pro.model.Category;

public interface CategoryService {
    Category create(Category category);
    boolean update(Category category);
    boolean delete(int id);
    Category findById(int id);
    List<Category> findAll();
}
