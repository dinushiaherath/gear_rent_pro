package lk.ijse.gear_rent_pro.service;

import java.util.List;
import lk.ijse.gear_rent_pro.model.Equipment;

public interface EquipmentService {
    String generateEquipmentId(int categoryId);
    Equipment create(Equipment equipment);
    boolean update(Equipment equipment);
    boolean delete(int id);
    Equipment findById(int id);
    List<Equipment> findAll();
}
