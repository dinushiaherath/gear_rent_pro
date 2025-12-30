package lk.ijse.gear_rent_pro.service.impl;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import lk.ijse.gear_rent_pro.dao.EquipmentDao;
import lk.ijse.gear_rent_pro.dao.RentalDao;
import lk.ijse.gear_rent_pro.dao.impl.EquipmentDaoImpl;
import lk.ijse.gear_rent_pro.dao.impl.RentalDaoImpl;
import lk.ijse.gear_rent_pro.model.Equipment;
import lk.ijse.gear_rent_pro.model.EquipmentStatus;
import lk.ijse.gear_rent_pro.model.Rental;
import lk.ijse.gear_rent_pro.model.RentalStatus;
import lk.ijse.gear_rent_pro.model.UserRole;
import lk.ijse.gear_rent_pro.service.RentalService;
import lk.ijse.gear_rent_pro.util.SessionContext;
import lk.ijse.gear_rent_pro.util.TransactionManager;

public class RentalServiceImpl implements RentalService {

    private final RentalDao rentalDao = new RentalDaoImpl();
    private final EquipmentDao equipmentDao = new EquipmentDaoImpl();

    @Override
    public Rental create(Rental rental) {
        try {
            return rentalDao.save(rental);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean update(Rental rental) {
        try {
            return rentalDao.update(rental);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean delete(int id) {
        try {
            return rentalDao.deleteById(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Rental findById(int id) {
        try {
            return rentalDao.findById(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Rental> findAll() {
        try {
            List<Rental> allRentals = rentalDao.findAll();

            // Apply branch-level filtering for non-admin users
            var currentUser = SessionContext.getInstance().getCurrentUser();
            if (currentUser != null && currentUser.getRole() != UserRole.ADMIN) {
                int userBranchId = currentUser.getBranchId();
                return allRentals.stream()
                        .filter(rental -> rental.getBranchId() == userBranchId)
                        .collect(Collectors.toList());
            }

            return allRentals;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void validateCustomerDeposit(int customerId, double requiredDeposit) {
        if (customerId <= 0) {
            throw new IllegalArgumentException("Invalid customer ID");
        }
        if (requiredDeposit < 0) {
            throw new IllegalArgumentException("Deposit amount cannot be negative");
        }

        try {
            List<Rental> allRentals = rentalDao.findAll();

            // Only sum deposits from ACTIVE rentals
            double currentActiveDeposits = allRentals.stream()
                    .filter(r -> r.getCustomerId() == customerId)
                    .filter(r -> RentalStatus.ACTIVE.equals(r.getRentalStatus()))
                    .mapToDouble(Rental::getSecurityDeposit)
                    .sum();

            // Define deposit limit (should be configurable, e.g., 500,000)
            double DEPOSIT_LIMIT = 500000.0;

            double totalDepositsAfterNew = currentActiveDeposits + requiredDeposit;
            System.out.println("Current Active Deposits: " + totalDepositsAfterNew);

            if (totalDepositsAfterNew > DEPOSIT_LIMIT) {
                throw new IllegalArgumentException(
                        String.format("Deposit limit exceeded. Current active deposits: %.2f, "
                                + "Requested: %.2f, Limit: %.2f",
                                currentActiveDeposits, requiredDeposit, DEPOSIT_LIMIT)
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error validating customer deposit: " + e.getMessage(), e);
        }
    }

    @Override
    public Rental createWithValidation(Rental rental) {
        // Validate input
        if (rental == null) {
            throw new IllegalArgumentException("Rental cannot be null");
        }
        if (rental.getEquipmentId() <= 0) {
            throw new IllegalArgumentException("Invalid equipment ID");
        }
        if (rental.getCustomerId() <= 0) {
            throw new IllegalArgumentException("Invalid customer ID");
        }
        if (rental.getBranchId() <= 0) {
            throw new IllegalArgumentException("Invalid branch ID");
        }
        if (rental.getStartDate() == null || rental.getEndDate() == null) {
            throw new IllegalArgumentException("Start and end dates cannot be null");
        }
        if (rental.getStartDate().isAfter(rental.getEndDate())) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }

        // Validate deposit
        validateCustomerDeposit(rental.getCustomerId(), rental.getSecurityDeposit());

        // Create the rental
        return create(rental);
    }

    @Override
    public Rental processReturn(int rentalId, LocalDate actualReturnDate, String damageDescription, double damageCharge) {
        if (rentalId <= 0) {
            throw new IllegalArgumentException("Invalid rental ID");
        }
        if (actualReturnDate == null) {
            throw new IllegalArgumentException("Actual return date cannot be null");
        }
        if (damageCharge < 0) {
            throw new IllegalArgumentException("Damage charge cannot be negative");
        }

        try {
            return TransactionManager.executeTransaction(connection -> {
                // Get the rental
                Rental rental = rentalDao.findById(rentalId);
                if (rental == null) {
                    throw new IllegalArgumentException("Rental not found: " + rentalId);
                }
                if (rental.getRentalStatus() != RentalStatus.ACTIVE && rental.getRentalStatus() != RentalStatus.OVERDUE) {
                    throw new IllegalArgumentException("Can only process return for ACTIVE or OVERDUE rentals");
                }

                // Calculate late fees if returned after end date
                double lateFee = 0;
                if (actualReturnDate.isAfter(rental.getEndDate())) {
                    long daysLate = ChronoUnit.DAYS.between(rental.getEndDate(), actualReturnDate);
                    // Use default late fee of 500 per day (could be enhanced to use category late fees)
                    lateFee = daysLate * 500;
                }

                // Update rental with return information
                rental.setActualReturnDate(actualReturnDate);
                rental.setDamageDescription(damageDescription);
                rental.setDamageCharge(damageCharge);
                rental.setLateFee(lateFee);

                // Calculate final amount: rental amount - security deposit + damage charge + late fee
                double finalAmount = rental.getCalculatedRentalAmount()
                        - rental.getSecurityDeposit()
                        + damageCharge
                        + lateFee;
                rental.setFinalPayableAmount(Math.max(0, finalAmount));

                // Set rental status to RETURNED
                rental.setRentalStatus(RentalStatus.RETURNED);

                // Update rental in database
                if (!rentalDao.update(rental)) {
                    throw new RuntimeException("Failed to update rental return information");
                }

                // Update equipment status back to AVAILABLE
                Equipment equipment = equipmentDao.findById(rental.getEquipmentId());
                if (equipment != null) {
                    equipment.setStatus(EquipmentStatus.AVAILABLE);
                    if (!equipmentDao.update(equipment)) {
                        throw new RuntimeException("Failed to update equipment status");
                    }
                }

                return rental;
            });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error processing rental return: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Rental> findActiveRentalsByCustomerId(int customerId) {
        try {
            List<Rental> allRentals = rentalDao.findAll();
            return allRentals.stream()
                    .filter(r -> r.getCustomerId() == customerId && RentalStatus.ACTIVE.equals(r.getRentalStatus()))
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Rental> findOverdueRentals() {
        try {
            LocalDate today = LocalDate.now();
            List<Rental> allRentals = rentalDao.findAll();
            return allRentals.stream()
                    .filter(r -> RentalStatus.OVERDUE.equals(r.getRentalStatus()) && r.getEndDate().isBefore(today))
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void markOverdueRentals(LocalDate date) {
        try {
            rentalDao.markOverdueBefore(date);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to mark overdue rentals: " + e.getMessage(), e);
        }
    }
}
