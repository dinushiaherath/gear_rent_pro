package lk.ijse.gear_rent_pro.service;

import java.time.LocalDate;
import java.util.List;

import lk.ijse.gear_rent_pro.model.Reservation;

public interface ReservationService {
    Reservation create(Reservation reservation);
    boolean update(Reservation reservation);
    boolean delete(int id);
    Reservation findById(int id);
    List<Reservation> findAll();
    
    /**
     * Validates that there are no overlapping reservations or rentals for the given equipment
     * during the specified date range.
     * 
     * @param equipmentId The equipment ID to check
     * @param startDate The reservation start date
     * @param endDate The reservation end date
     * @throws IllegalArgumentException if overlap detected with details of conflicting reservations/rental
     */
    void validateNoOverlap(int equipmentId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Creates a new reservation with overlap validation.
     * 
     * @param reservation The reservation to create
     * @return The created reservation
     * @throws IllegalArgumentException if validation fails (overlap or invalid dates)
     */
    Reservation createWithValidation(Reservation reservation);
    
    /**
     * Finds all reservations for a specific equipment.
     * 
     * @param equipmentId The equipment ID
     * @return List of reservations for that equipment
     */
    List<Reservation> findByEquipmentId(int equipmentId);
}
