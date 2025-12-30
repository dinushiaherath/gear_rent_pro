package lk.ijse.gear_rent_pro.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lk.ijse.gear_rent_pro.model.*;
import lk.ijse.gear_rent_pro.service.*;
import lk.ijse.gear_rent_pro.service.impl.*;
import lk.ijse.gear_rent_pro.service.ReservationService;
import lk.ijse.gear_rent_pro.service.impl.ReservationServiceImpl;
import lk.ijse.gear_rent_pro.util.SessionContext;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class RentalFormController {
    
    @FXML
    private TextField rentalIdField;
    @FXML
    private ComboBox<Branch> branchCombo;
    @FXML
    private ComboBox<Equipment> equipmentCombo;
    @FXML
    private ComboBox<Customer> customerCombo;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private TextField dailyRateField;
    @FXML
    private TextField durationField;
    @FXML
    private TextField baseAmountField;
    @FXML
    private TextField membershipDiscountField;
    @FXML
    private TextField longRentalDiscountField;
    @FXML
    private TextField finalAmountField;
    @FXML
    private TextField securityDepositField;
    @FXML
    private TextArea breakdownArea;
    
    private final RentalService rentalService = new RentalServiceImpl();
    private final EquipmentService equipmentService = new EquipmentServiceImpl();
    private final CustomerService customerService = new CustomerServiceImpl();
    private final BranchService branchService = new BranchServiceImpl();
    private final CategoryService categoryService = new CategoryServiceImpl();
    private final ReservationService reservationService = new ReservationServiceImpl();
    private final lk.ijse.gear_rent_pro.service.MembershipConfigService membershipConfigService = new lk.ijse.gear_rent_pro.service.impl.MembershipConfigServiceImpl();

    private static final double LONG_RENTAL_DISCOUNT_PERCENT = 15.0; // percent applied when duration >= 7 days
    
    @FXML
    public void initialize() {
        setupFields();
        loadBranches();
        loadEquipment();
        loadCustomers();
        
        // Add listeners for calculations and equipment availability
        startDatePicker.valueProperty().addListener((obs, old, newVal) -> {
            loadEquipment();
            calculateRentalAmount();
        });
        endDatePicker.valueProperty().addListener((obs, old, newVal) -> {
            loadEquipment();
            calculateRentalAmount();
        });
        // Refresh equipment list when branch changes as availability depends on branch + dates
        branchCombo.valueProperty().addListener((obs, old, newVal) -> {
            loadEquipment();
            calculateRentalAmount();
        });
        equipmentCombo.valueProperty().addListener((obs, old, newVal) -> calculateRentalAmount());
        customerCombo.valueProperty().addListener((obs, old, newVal) -> calculateRentalAmount());
    }
    
    private void setupFields() {
        // Generate rental ID
        rentalIdField.setText("RENT-" + System.currentTimeMillis());
        
        // Disable editing for calculated fields
        dailyRateField.setEditable(false);
        durationField.setEditable(false);
        baseAmountField.setEditable(false);
        membershipDiscountField.setEditable(false);
        longRentalDiscountField.setEditable(false);
        finalAmountField.setEditable(false);
        securityDepositField.setEditable(false);
        
        // Set today as default start date
        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now().plusDays(1));
        
        // Disable branch combo for non-admins
        User currentUser = SessionContext.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getRole() != UserRole.ADMIN) {
            branchCombo.setDisable(true);
        }
    }
    
    private void loadBranches() {
        try {
            List<Branch> branches = branchService.findAll();
            User currentUser = SessionContext.getInstance().getCurrentUser();
            
            // Non-admins can only see their branch
            if (currentUser != null && currentUser.getRole() != UserRole.ADMIN) {
                branches = branches.stream()
                    .filter(b -> b.getId() == currentUser.getBranchId())
                    .collect(Collectors.toList());
            }
            
            ObservableList<Branch> branchList = FXCollections.observableArrayList(branches);
            branchCombo.setItems(branchList);
            
            // Pre-select current user's branch for non-admins
            if (currentUser != null && currentUser.getRole() != UserRole.ADMIN) {
                Branch userBranch = branches.stream()
                    .filter(b -> b.getId() == currentUser.getBranchId())
                    .findFirst()
                    .orElse(null);
                branchCombo.setValue(userBranch);
            }
        } catch (Exception e) {
            showError("Error loading branches", e.getMessage());
        }
    }
    
    private void loadEquipment() {
        try {
            LocalDate start = startDatePicker.getValue();
            LocalDate end = endDatePicker.getValue();
            Branch selectedBranch = branchCombo.getValue();
            List<Equipment> equipment = equipmentService.findAll().stream()
                .filter(e -> e.getStatus() != EquipmentStatus.UNDER_MAINTENANCE)
                .filter(e -> selectedBranch == null || e.getBranchId() == selectedBranch.getId())
                .filter(e -> {
                    if (start == null || end == null) {
                        return e.getStatus() == EquipmentStatus.AVAILABLE;
                    }
                    return isEquipmentAvailableForPeriod(e, start, end);
                })
                .collect(Collectors.toList());
            ObservableList<Equipment> equipmentList = FXCollections.observableArrayList(equipment);
            equipmentCombo.setItems(equipmentList);
            equipmentCombo.setCellFactory(lv -> new ListCell<Equipment>() {
                @Override
                protected void updateItem(Equipment item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : (item.getBrand() + " " + item.getModel()) + " (" + item.getEquipmentId() + ")");
                }
            });
            equipmentCombo.setButtonCell(new ListCell<Equipment>() {
                @Override
                protected void updateItem(Equipment item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : (item.getBrand() + " " + item.getModel()) + " (" + item.getEquipmentId() + ")");
                }
            });
        } catch (Exception e) {
            showError("Error loading equipment", e.getMessage());
        }
    }

    private boolean datesOverlap(LocalDate aStart, LocalDate aEnd, LocalDate bStart, LocalDate bEnd) {
        if (aStart == null || aEnd == null || bStart == null || bEnd == null) return false;
        return !(aEnd.isBefore(bStart) || aStart.isAfter(bEnd));
    }

    private boolean isEquipmentAvailableForPeriod(Equipment equipment, LocalDate start, LocalDate end) {
        if (equipment == null || start == null || end == null) return false;
        if (start.isAfter(end)) return false;

        // If equipment is currently under maintenance
        if (equipment.getStatus() == EquipmentStatus.UNDER_MAINTENANCE) return false;

        try {
            // Check overlapping active rentals
            List<Rental> rentals = rentalService.findAll().stream()
                .filter(r -> r.getEquipmentId() == equipment.getId())
                .collect(Collectors.toList());

            for (Rental r : rentals) {
                if (r.getStartDate() == null || r.getEndDate() == null) continue;
                if (RentalStatus.ACTIVE.equals(r.getRentalStatus()) && datesOverlap(r.getStartDate(), r.getEndDate(), start, end)) {
                    return false;
                }
            }

            // Check overlapping reservations
            List<Reservation> reservations = reservationService.findAll().stream()
                .filter(res -> res.getEquipmentId() == equipment.getId())
                .collect(Collectors.toList());

            for (Reservation res : reservations) {
                if (res.getStartDate() == null || res.getEndDate() == null) continue;
                if (datesOverlap(res.getStartDate(), res.getEndDate(), start, end)) {
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }
    
    private void loadCustomers() {
        try {
            List<Customer> customers = customerService.findAll();
            ObservableList<Customer> customerList = FXCollections.observableArrayList(customers);
            customerCombo.setItems(customerList);
            customerCombo.setCellFactory(lv -> new ListCell<Customer>() {
                @Override
                protected void updateItem(Customer item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getName() + " (" + item.getCustomerId() + ")");
                }
            });
            customerCombo.setButtonCell(new ListCell<Customer>() {
                @Override
                protected void updateItem(Customer item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getName() + " (" + item.getCustomerId() + ")");
                }
            });
        } catch (Exception e) {
            showError("Error loading customers", e.getMessage());
        }
    }
    
    private void calculateRentalAmount() {
        Equipment equipment = equipmentCombo.getValue();
        Customer customer = customerCombo.getValue();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        
        if (equipment == null || customer == null || startDate == null || endDate == null) {
            clearCalculations();
            return;
        }
        
        try {
            if (startDate.isAfter(endDate)) {
                endDatePicker.setValue(startDate.plusDays(1));
                return;
            }
            
            // Get category modifiers
            double dailyBase = equipment.getBaseDailyPrice();
            Category category = null;
            try {
                category = categoryService.findById(equipment.getCategoryId());
            } catch (Exception ignored) {}

            double categoryFactor = category != null ? category.getBasePriceFactor() : 1.0;
            double weekendMultiplier = category != null ? category.getWeekendMultiplier() : 1.0;

            // Calculate duration (number of chargeable days)
            long durationDays = ChronoUnit.DAYS.between(startDate, endDate);
            if (durationDays == 0) durationDays = 1;

            // Sum per-day prices taking weekend multiplier into account
            double totalAmount = 0.0;
            for (int i = 0; i < durationDays; i++) {
                LocalDate day = startDate.plusDays(i);
                boolean isWeekend = day.getDayOfWeek() == java.time.DayOfWeek.SATURDAY || day.getDayOfWeek() == java.time.DayOfWeek.SUNDAY;
                double dayPrice = dailyBase * categoryFactor * (isWeekend ? weekendMultiplier : 1.0);
                totalAmount += dayPrice;
            }

            // Membership discount (percentage comes from admin-configured values)
            double membershipDiscount = 0;
            try {
                if (customer.getMembership() != null && customer.getMembership() != MembershipLevel.REGULAR) {
                    double pct = membershipConfigService.findByLevel(customer.getMembership()).getDiscountPercent();
                    membershipDiscount = totalAmount * (pct / 100.0);
                }
            } catch (Exception ex) {
                membershipDiscount = 0;
            }

            // Long-rental discount: apply when duration >= 7 days (percent configurable via constant)
            double longRentalDiscount = 0;
            if (durationDays >= 7) {
                longRentalDiscount = totalAmount * (LONG_RENTAL_DISCOUNT_PERCENT / 100.0);
            }

            double finalAmount = totalAmount - membershipDiscount - longRentalDiscount;

            // Security deposit
            double securityDeposit = equipment.getSecurityDeposit();

            // Update fields
            double avgDaily = durationDays > 0 ? (totalAmount / durationDays) : dailyBase;
            dailyRateField.setText(String.format("%.2f", avgDaily));
            durationField.setText(String.valueOf(durationDays));
            baseAmountField.setText(String.format("%.2f", totalAmount));
            membershipDiscountField.setText(String.format("%.2f", membershipDiscount));
            longRentalDiscountField.setText(String.format("%.2f", longRentalDiscount));
            finalAmountField.setText(String.format("%.2f", finalAmount));
            securityDepositField.setText(String.format("%.2f", securityDeposit));

            // Build pricing breakdown text
            StringBuilder breakdown = new StringBuilder();
            breakdown.append("=== PRICING CALCULATION BREAKDOWN ===\n\n");
            breakdown.append("EQUIPMENT & CATEGORY:\n");
            breakdown.append(String.format("  Base Daily Price: %.2f\n", dailyBase));
            breakdown.append(String.format("  Category: %s (Factor: %.2f)\n", 
                category != null ? category.getName() : "N/A", categoryFactor));
            breakdown.append(String.format("  Weekend Multiplier: %.2f\n\n", weekendMultiplier));

            // Count weekdays and weekends
            int weekdayCount = 0, weekendCount = 0;
            for (int i = 0; i < durationDays; i++) {
                LocalDate day = startDate.plusDays(i);
                boolean isWeekend = day.getDayOfWeek() == java.time.DayOfWeek.SATURDAY || day.getDayOfWeek() == java.time.DayOfWeek.SUNDAY;
                if (isWeekend) weekendCount++;
                else weekdayCount++;
            }

            breakdown.append("DURATION BREAKDOWN:\n");
            breakdown.append(String.format("  Total Days: %d\n", durationDays));
            breakdown.append(String.format("    - Weekdays: %d days @ %.2f = %.2f\n", 
                weekdayCount, dailyBase * categoryFactor, weekdayCount * dailyBase * categoryFactor));
            breakdown.append(String.format("    - Weekends: %d days @ %.2f = %.2f\n", 
                weekendCount, dailyBase * categoryFactor * weekendMultiplier, weekendCount * dailyBase * categoryFactor * weekendMultiplier));
            breakdown.append(String.format("\n  SUBTOTAL: %.2f\n\n", totalAmount));

            breakdown.append("DISCOUNTS APPLIED:\n");
            if (customer.getMembership() != MembershipLevel.REGULAR) {
                double membershipPercent = membershipConfigService.findByLevel(customer.getMembership()).getDiscountPercent();
                breakdown.append(String.format("  - Membership Discount (%s, %.0f%%): -%.2f\n", 
                    customer.getMembership().toString(), membershipPercent, membershipDiscount));
            }

            if (durationDays >= 7) {
                breakdown.append(String.format("  - Long-Rental Discount (≥7 days, %.0f%%): -%.2f\n", 
                    LONG_RENTAL_DISCOUNT_PERCENT, longRentalDiscount));
            }
            
            if (membershipDiscount == 0 && longRentalDiscount == 0) {
                breakdown.append("  (No discounts applied)\n");
            }
            
            breakdown.append(String.format("\n  TOTAL DISCOUNTS: -%.2f\n\n", membershipDiscount + longRentalDiscount));

            breakdown.append("FINAL CHARGES:\n");
            breakdown.append(String.format("  Rental Amount (after discounts): %.2f\n", finalAmount));
            breakdown.append(String.format("  Security Deposit: %.2f\n", securityDeposit));
            breakdown.append(String.format("  ───────────────────────────────\n"));
            breakdown.append(String.format("  TOTAL DUE: %.2f\n", finalAmount + securityDeposit));

            breakdownArea.setText(breakdown.toString());
        } catch (Exception e) {
            showError("Error calculating rental amount", e.getMessage());
        }
    }
    
    private void clearCalculations() {
        dailyRateField.clear();
        durationField.clear();
        baseAmountField.clear();
        membershipDiscountField.clear();
        longRentalDiscountField.clear();
        finalAmountField.clear();
        securityDepositField.clear();
        breakdownArea.clear();
    }
    
    @FXML
    public void createRental() {
        try {
            // Validation
            if (equipmentCombo.getValue() == null) {
                showError("Validation Error", "Please select equipment");
                return;
            }
            if (customerCombo.getValue() == null) {
                showError("Validation Error", "Please select customer");
                return;
            }
            if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
                showError("Validation Error", "Please select start and end dates");
                return;
            }
            if (branchCombo.getValue() == null) {
                showError("Validation Error", "Please select branch");
                return;
            }
            
            Equipment equipment = equipmentCombo.getValue();
            Customer customer = customerCombo.getValue();
            Branch branch = branchCombo.getValue();
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            
            // Create rental object
            Rental rental = new Rental();
            rental.setRentalId(rentalIdField.getText());
            rental.setEquipmentId(equipment.getId());
            rental.setCustomerId(customer.getId());
            rental.setBranchId(branch.getId());
            rental.setStartDate(startDate);
            rental.setEndDate(endDate);
            rental.setCalculatedRentalAmount(Double.parseDouble(finalAmountField.getText()));
            rental.setSecurityDeposit(Double.parseDouble(securityDepositField.getText()));
            rental.setMembershipDiscount(Double.parseDouble(membershipDiscountField.getText()));
            rental.setLongRentalDiscount(Double.parseDouble(longRentalDiscountField.getText()));
            rental.setFinalPayableAmount(Double.parseDouble(finalAmountField.getText()));
            rental.setPaymentStatus(PaymentStatus.UNPAID);
            rental.setRentalStatus(RentalStatus.ACTIVE);
            
            // Availability check: ensure equipment is free for chosen dates
            if (!isEquipmentAvailableForPeriod(equipment, startDate, endDate)) {
                showError("Validation Error", "Selected equipment is NOT available for the chosen dates.");
                return;
            }

            // Create rental with validation
            Rental created = rentalService.createWithValidation(rental);
            
            // Update equipment status to RENTED
            equipment.setStatus(EquipmentStatus.RENTED);
            equipmentService.update(equipment);
            
            showAlert("Success", "Rental Created", "Rental has been created successfully");
            closeForm();
        } catch (IllegalArgumentException e) {
            showError("Validation Error", e.getMessage());
        } catch (Exception e) {
            showError("Error creating rental", e.getMessage());
        }
    }
    
    @FXML
    public void cancelForm() {
        closeForm();
    }
    
    private void closeForm() {
        Stage stage = (Stage) rentalIdField.getScene().getWindow();
        stage.close();
    }
    
    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Error");
        alert.setContentText(content);
        alert.showAndWait();
    }
}
