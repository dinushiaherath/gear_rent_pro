package lk.ijse.gear_rent_pro.dao;

import java.sql.SQLException;
import java.util.List;
import lk.ijse.gear_rent_pro.model.Equipment;

public interface EquipmentDao {
    Equipment save(Equipment equipment) throws SQLException;
    boolean update(Equipment equipment) throws SQLException;
    boolean deleteById(int id) throws SQLException;
    Equipment findById(int id) throws SQLException;
    List<Equipment> findAll() throws SQLException;
    List<Equipment> findByBranchId(int branchId) throws SQLException;
}
