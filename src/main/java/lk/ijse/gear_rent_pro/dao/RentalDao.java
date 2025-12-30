package lk.ijse.gear_rent_pro.dao;

import java.sql.SQLException;
import java.util.List;

import lk.ijse.gear_rent_pro.model.Rental;

public interface RentalDao {
    Rental save(Rental rental) throws SQLException;
    boolean update(Rental rental) throws SQLException;
    boolean deleteById(int id) throws SQLException;
    Rental findById(int id) throws SQLException;
    List<Rental> findAll() throws SQLException;

    /**
     * Mark rentals with status ACTIVE and end_date before the given date as OVERDUE.
     * @param date cutoff date (exclusive) - rentals with end_date < date will be marked
     * @return number of rows updated
     */
    int markOverdueBefore(java.time.LocalDate date) throws SQLException;
}
