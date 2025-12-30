package lk.ijse.gear_rent_pro.service.impl;

import java.sql.SQLException;
import java.util.List;
import lk.ijse.gear_rent_pro.dao.CategoryDao;
import lk.ijse.gear_rent_pro.dao.impl.CategoryDaoImpl;
import lk.ijse.gear_rent_pro.model.Category;
import lk.ijse.gear_rent_pro.service.CategoryService;

public class CategoryServiceImpl implements CategoryService {

    private final CategoryDao dao = new CategoryDaoImpl();

    @Override
    public Category create(Category category) {
        try { return dao.save(category); } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public boolean update(Category category) {
        try { return dao.update(category); } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public boolean delete(int id) {
        try { return dao.deleteById(id); } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public Category findById(int id) {
        try { return dao.findById(id); } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<Category> findAll() {
        try { return dao.findAll(); } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
