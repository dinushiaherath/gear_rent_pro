package lk.ijse.gear_rent_pro.service.impl;

import java.sql.SQLException;
import java.util.List;
import lk.ijse.gear_rent_pro.dao.EquipmentDao;
import lk.ijse.gear_rent_pro.dao.impl.EquipmentDaoImpl;
import lk.ijse.gear_rent_pro.model.Category;
import lk.ijse.gear_rent_pro.model.Equipment;
import lk.ijse.gear_rent_pro.model.UserRole;
import lk.ijse.gear_rent_pro.service.CategoryService;
import lk.ijse.gear_rent_pro.service.EquipmentService;
import lk.ijse.gear_rent_pro.service.impl.CategoryServiceImpl;
import lk.ijse.gear_rent_pro.util.SessionContext;

public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentDao dao = new EquipmentDaoImpl();
    private final CategoryService categoryService = new CategoryServiceImpl();

    @Override
    public String generateEquipmentId(int categoryId) {
        try {
            Category cat = categoryService.findById(categoryId);
            if (cat == null) return "EQ-001";
            String prefix = cat.getName().substring(0, Math.min(3, cat.getName().length())).toUpperCase();
            List<Equipment> all = dao.findAll();
            long count = all.stream().filter(e -> e.getCategoryId() == categoryId).count();
            return String.format("%s-%03d", prefix, count + 1);
        } catch (Exception e) {
            return "EQ-" + System.currentTimeMillis() % 1000;
        }
    }

    @Override
    public Equipment create(Equipment equipment) {
        try { return dao.save(equipment); } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public boolean update(Equipment equipment) {
        try { return dao.update(equipment); } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public boolean delete(int id) {
        try { return dao.deleteById(id); } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public Equipment findById(int id) {
        try { return dao.findById(id); } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<Equipment> findAll() {
        try {
            var user = SessionContext.getInstance().getCurrentUser();
            if (user == null) return dao.findAll();
            if (user.getRole() == UserRole.ADMIN) {
                return dao.findAll();
            } else if (user.getBranchId() != null) {
                return dao.findByBranchId(user.getBranchId());
            } else {
                return List.of();
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
