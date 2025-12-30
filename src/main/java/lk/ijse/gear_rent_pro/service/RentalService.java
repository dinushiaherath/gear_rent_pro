package lk.ijse.gear_rent_pro.service;

import java.time.LocalDate;
import java.util.List;

import lk.ijse.gear_rent_pro.model.Rental;

public interface RentalService {
    Rental create(Rental rental);
    boolean update(Rental rental);
    boolean delete(int id);
    Rental findById(int id);
    List<Rental> findAll();
    
    /**
     * Validates that the customer has sufficient deposit credit for the required amount.
     * 
     * @param customerId The customer ID
     * @param requiredDeposit The security deposit required
     * @throws IllegalArgumentException if customer doesn't have sufficient credit
     */
    void validateCustomerDeposit(int customerId, double requiredDeposit);
    
    /**
     * Creates a new rental with deposit validation.
     * 
     * @param rental The rental to create
     * @return The created rental
     * @throws IllegalArgumentException if validation fails (insufficient deposit)
     */
    Rental createWithValidation(Rental rental);
    
    /**
     * Processes the return of a rental (transactional operation).
     * Updates rental status, calculates late fees and damage charges,
     * updates equipment status back to AVAILABLE.
     * 
     * @param rentalId The rental ID to process
     * @param actualReturnDate The actual return date
     * @param damageDescription Description of any damage (null if no damage)
     * @param damageCharge Cost of damage repairs
     * @return Updated rental object with all charges calculated
     * @throws IllegalArgumentException if rental not found or invalid parameters
     * @throws RuntimeException if transaction fails
     */
    Rental processReturn(int rentalId, LocalDate actualReturnDate, String damageDescription, double damageCharge);
    
    /**
     * Finds all active rentals for a specific customer.
     * 
     * @param customerId The customer ID
     * @return List of active rentals for that customer
     */
    List<Rental> findActiveRentalsByCustomerId(int customerId);
    
    /**
     * Finds all overdue rentals.
     * 
     * @return List of rentals with return date in the past
     */
    List<Rental> findOverdueRentals();

    /**
     * Mark all ACTIVE rentals whose end date is before the given date as OVERDUE.
     * @param date cutoff date (exclusive)
     */
    void markOverdueRentals(java.time.LocalDate date);
}
