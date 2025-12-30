package lk.ijse.gear_rent_pro.service.impl;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lk.ijse.gear_rent_pro.dao.RentalDao;
import lk.ijse.gear_rent_pro.dao.ReservationDao;
import lk.ijse.gear_rent_pro.dao.impl.RentalDaoImpl;
import lk.ijse.gear_rent_pro.dao.impl.ReservationDaoImpl;
import lk.ijse.gear_rent_pro.model.Rental;
import lk.ijse.gear_rent_pro.model.RentalStatus;
import lk.ijse.gear_rent_pro.model.Reservation;
import lk.ijse.gear_rent_pro.model.UserRole;
import lk.ijse.gear_rent_pro.service.ReservationService;
import lk.ijse.gear_rent_pro.util.SessionContext;

public class ReservationServiceImpl implements ReservationService {

    private final ReservationDao reservationDao = new ReservationDaoImpl();
    private final RentalDao rentalDao = new RentalDaoImpl();

    @Override
    public Reservation create(Reservation reservation) {
        try { return reservationDao.save(reservation); } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public boolean update(Reservation reservation) {
        try { return reservationDao.update(reservation); } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public boolean delete(int id) {
        try { return reservationDao.deleteById(id); } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public Reservation findById(int id) {
        try { return reservationDao.findById(id); } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<Reservation> findAll() {
        try {
            List<Reservation> allReservations = reservationDao.findAll();
            
            // Apply branch-level filtering for non-admin users
            var currentUser = SessionContext.getInstance().getCurrentUser();
            if (currentUser != null && currentUser.getRole() != UserRole.ADMIN) {
                int userBranchId = currentUser.getBranchId();
                return allReservations.stream()
                    .filter(reservation -> reservation.getBranchId() == userBranchId)
                    .collect(Collectors.toList());
            }
            
            return allReservations;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void validateNoOverlap(int equipmentId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }

        try {
            // Check for overlapping reservations
            List<Reservation> reservations = reservationDao.findAll();
            for (Reservation res : reservations) {
                if (res.getEquipmentId() == equipmentId) {
                    // Check if dates overlap: res.start <= endDate AND res.end >= startDate
                    if (!res.getEndDate().isBefore(startDate) && !res.getStartDate().isAfter(endDate)) {
                        throw new IllegalArgumentException(
                            String.format("Equipment is reserved from %s to %s (Reservation ID: %d)",
                                res.getStartDate(), res.getEndDate(), res.getId())
                        );
                    }
                }
            }

            // Check for overlapping rentals (active ones)
            List<Rental> rentals = rentalDao.findAll();
            for (Rental rental : rentals) {
                if (rental.getEquipmentId() == equipmentId && RentalStatus.ACTIVE.equals(rental.getRentalStatus())) {
                    if (!rental.getEndDate().isBefore(startDate) && !rental.getStartDate().isAfter(endDate)) {
                        throw new IllegalArgumentException(
                            String.format("Equipment is actively rented from %s to %s (Rental ID: %d)",
                                rental.getStartDate(), rental.getEndDate(), rental.getId())
                        );
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking equipment availability: " + e.getMessage(), e);
        }
    }

    @Override
    public Reservation createWithValidation(Reservation reservation) {
        // Validate input
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation cannot be null");
        }
        if (reservation.getEquipmentId() <= 0) {
            throw new IllegalArgumentException("Invalid equipment ID");
        }
        if (reservation.getCustomerId() <= 0) {
            throw new IllegalArgumentException("Invalid customer ID");
        }
        if (reservation.getBranchId() <= 0) {
            throw new IllegalArgumentException("Invalid branch ID");
        }

        // Validate no overlaps
        validateNoOverlap(reservation.getEquipmentId(), reservation.getStartDate(), reservation.getEndDate());

        // Create the reservation
        return create(reservation);
    }

    @Override
    public List<Reservation> findByEquipmentId(int equipmentId) {
        try {
            List<Reservation> allReservations = reservationDao.findAll();
            return allReservations.stream()
                .filter(r -> r.getEquipmentId() == equipmentId)
                .collect(Collectors.toList());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
