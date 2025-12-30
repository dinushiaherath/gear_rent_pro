package lk.ijse.gear_rent_pro.service.impl;

import java.sql.SQLException;
import java.util.UUID;
import lk.ijse.gear_rent_pro.dao.EquipmentDao;
import lk.ijse.gear_rent_pro.dao.RentalDao;
import lk.ijse.gear_rent_pro.dao.ReservationDao;
import lk.ijse.gear_rent_pro.dao.impl.EquipmentDaoImpl;
import lk.ijse.gear_rent_pro.dao.impl.RentalDaoImpl;
import lk.ijse.gear_rent_pro.dao.impl.ReservationDaoImpl;
import lk.ijse.gear_rent_pro.model.Equipment;
import lk.ijse.gear_rent_pro.model.EquipmentStatus;
import lk.ijse.gear_rent_pro.model.PaymentStatus;
import lk.ijse.gear_rent_pro.model.Rental;
import lk.ijse.gear_rent_pro.model.RentalStatus;
import lk.ijse.gear_rent_pro.model.Reservation;
import lk.ijse.gear_rent_pro.service.ReservationRentalService;
import lk.ijse.gear_rent_pro.util.TransactionManager;

public class ReservationRentalServiceImpl implements ReservationRentalService {

    private final ReservationDao reservationDao = new ReservationDaoImpl();
    private final RentalDao rentalDao = new RentalDaoImpl();
    private final EquipmentDao equipmentDao = new EquipmentDaoImpl();

    @Override
    public Rental convertReservationToRental(
            int reservationId,
            double rentalAmount,
            double securityDeposit,
            double membershipDiscount) {

        if (reservationId <= 0) {
            throw new IllegalArgumentException("Invalid reservation ID");
        }
        if (rentalAmount < 0) {
            throw new IllegalArgumentException("Rental amount cannot be negative");
        }
        if (securityDeposit < 0) {
            throw new IllegalArgumentException("Security deposit cannot be negative");
        }
        if (membershipDiscount < 0) {
            throw new IllegalArgumentException("Membership discount cannot be negative");
        }

        try {
            return TransactionManager.executeTransaction(connection -> {
                // 1. Fetch the reservation
                Reservation reservation = reservationDao.findById(reservationId);
                if (reservation == null) {
                    throw new IllegalArgumentException("Reservation not found: " + reservationId);
                }

                // 2. Verify equipment exists
                Equipment equipment = equipmentDao.findById(reservation.getEquipmentId());
                if (equipment == null) {
                    throw new IllegalArgumentException(
                        "Equipment not found for reservation: " + reservation.getEquipmentId());
                }

                // 3. Check if equipment is AVAILABLE (not already rented)
                if (!EquipmentStatus.AVAILABLE.name().equals(equipment.getStatus())) {
                    throw new IllegalArgumentException(
                        "Equipment is not available for rental (current status: " + equipment.getStatus() + ")");
                }

                // 4. Create new rental from reservation data
                Rental rental = new Rental();
                rental.setRentalId("RNT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                rental.setEquipmentId(reservation.getEquipmentId());
                rental.setCustomerId(reservation.getCustomerId());
                rental.setBranchId(reservation.getBranchId());
                rental.setStartDate(reservation.getStartDate());
                rental.setEndDate(reservation.getEndDate());
                rental.setCalculatedRentalAmount(rentalAmount);
                rental.setSecurityDeposit(securityDeposit);
                rental.setMembershipDiscount(membershipDiscount);
                
                // Calculate long-rental discount (for rentals >= 7 days)
                long rentalDays = java.time.temporal.ChronoUnit.DAYS.between(
                    reservation.getStartDate(), reservation.getEndDate()) + 1;
                double longRentalDiscount = 0;
                if (rentalDays >= 7) {
                    // 10% discount for long rentals
                    longRentalDiscount = rentalAmount * 0.10;
                }
                rental.setLongRentalDiscount(longRentalDiscount);
                
                // Final payable = rental amount - membership discount - long rental discount + security deposit
                double finalPayable = rentalAmount - membershipDiscount - longRentalDiscount + securityDeposit;
                rental.setFinalPayableAmount(Math.max(0, finalPayable));
                
                rental.setPaymentStatus(PaymentStatus.UNPAID);
                rental.setRentalStatus(RentalStatus.ACTIVE);

                // 5. Save the rental
                Rental savedRental = rentalDao.save(rental);
                if (savedRental == null || savedRental.getId() <= 0) {
                    throw new RuntimeException("Failed to create rental from reservation");
                }

                // 6. Update equipment status to RENTED
                equipment.setStatus(EquipmentStatus.RENTED);
                if (!equipmentDao.update(equipment)) {
                    throw new RuntimeException("Failed to update equipment status to RENTED");
                }

                return savedRental;
            });
        } catch (RuntimeException e) {
            throw e;
        } catch (SQLException e) {
            throw new RuntimeException("Database error converting reservation to rental: " + e.getMessage(), e);
        } catch (Throwable e) {
            throw new RuntimeException("Error converting reservation to rental: " + e.getMessage(), e);
        }
    }
}
