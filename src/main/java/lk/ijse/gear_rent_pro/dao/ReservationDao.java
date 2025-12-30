package lk.ijse.gear_rent_pro.dao;

import java.sql.SQLException;
import java.util.List;
import lk.ijse.gear_rent_pro.model.Reservation;

public interface ReservationDao {
    Reservation save(Reservation reservation) throws SQLException;
    boolean update(Reservation reservation) throws SQLException;
    boolean deleteById(int id) throws SQLException;
    Reservation findById(int id) throws SQLException;
    List<Reservation> findAll() throws SQLException;
}
