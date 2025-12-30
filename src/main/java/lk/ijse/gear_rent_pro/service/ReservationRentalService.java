package lk.ijse.gear_rent_pro.service;

import lk.ijse.gear_rent_pro.model.Rental;

/**
 * Service for handling multi-step operations involving both reservations and rentals.
 * Ensures transactional consistency when converting reservations to rentals.
 */
public interface ReservationRentalService {
    
    /**
     * Converts a reservation to an active rental (transactional operation).
     * This is an atomic operation that:
     * 1. Verifies the reservation exists and is still valid
     * 2. Creates a new rental from the reservation data
     * 3. Marks the equipment as RENTED
     * 4. Updates the reservation status (optional tracking)
     * 
     * @param reservationId The reservation ID to convert
     * @param rentalAmount Calculated rental amount
     * @param securityDeposit Security deposit for the rental
     * @param membershipDiscount Discount applied (if any)
     * @return The created rental
     * @throws IllegalArgumentException if reservation not found or invalid
     * @throws RuntimeException if transaction fails
     */
    Rental convertReservationToRental(
        int reservationId,
        double rentalAmount,
        double securityDeposit,
        double membershipDiscount
    );
}
