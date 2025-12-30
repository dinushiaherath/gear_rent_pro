package lk.ijse.gear_rent_pro.controller;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lk.ijse.gear_rent_pro.model.Category;
import lk.ijse.gear_rent_pro.model.Customer;
import lk.ijse.gear_rent_pro.model.Equipment;
import lk.ijse.gear_rent_pro.model.EquipmentStatus;
import lk.ijse.gear_rent_pro.model.PaymentStatus;
import lk.ijse.gear_rent_pro.model.Rental;
import lk.ijse.gear_rent_pro.service.CategoryService;
import lk.ijse.gear_rent_pro.service.CustomerService;
import lk.ijse.gear_rent_pro.service.EquipmentService;
import lk.ijse.gear_rent_pro.service.RentalService;
import lk.ijse.gear_rent_pro.service.impl.CategoryServiceImpl;
import lk.ijse.gear_rent_pro.service.impl.CustomerServiceImpl;
import lk.ijse.gear_rent_pro.service.impl.EquipmentServiceImpl;
import lk.ijse.gear_rent_pro.service.impl.RentalServiceImpl;

public class ReturnFormController {

    @FXML
    private TextField rentalIdField;
    @FXML
    private TextField customerField;
    @FXML
    private TextField equipmentField;
    @FXML
    private TextField startDateField;
    @FXML
    private TextField endDateField;
    @FXML
    private TextField daysRentedField;

    @FXML
    private DatePicker actualReturnDatePicker;
    @FXML
    private TextField daysLateField;
    @FXML
    private CheckBox damagedCheckBox;
    @FXML
    private TextArea damageDescriptionArea;
    @FXML
    private TextField damageChargeField;

    @FXML
    private TextField rentalAmountField;
    @FXML
    private TextField securityDepositField;
    @FXML
    private TextField lateFeeField;
    @FXML
    private TextField totalChargesField;
    @FXML
    private TextField balanceField;
    @FXML
    private Label settlementTypeLabel;
    @FXML
    private TextArea breakdownArea;

    private Rental currentRental;
    private final RentalService rentalService = new RentalServiceImpl();
    private final CustomerService customerService = new CustomerServiceImpl();
    private final EquipmentService equipmentService = new EquipmentServiceImpl();
    private final CategoryService categoryService = new CategoryServiceImpl();

    public void setRental(Rental rental) {
        this.currentRental = rental;
        populateRentalDetails();
        setupListeners();
    }

    @FXML
    public void initialize() {
        actualReturnDatePicker.setValue(LocalDate.now());
        damageChargeField.setText("0.00");
    }

    private void setupListeners() {
        // Recalculate settlement on date/damage changes
        actualReturnDatePicker.valueProperty().addListener((obs, old, newVal) -> calculateSettlement());
        damageChargeField.textProperty().addListener((obs, old, newVal) -> calculateSettlement());
        damagedCheckBox.selectedProperty().addListener((obs, old, newVal) -> {
            damageDescriptionArea.setDisable(!newVal);
            damageChargeField.setDisable(!newVal);
            if (!newVal) {
                damageChargeField.setText("0.00");
                damageDescriptionArea.clear();
            }
            calculateSettlement();
        });
    }

    private void populateRentalDetails() {
        if (currentRental == null) {
            return;
        }

        try {
            rentalIdField.setText(currentRental.getRentalId());
            startDateField.setText(currentRental.getStartDate().toString());
            endDateField.setText(currentRental.getEndDate().toString());
            rentalAmountField.setText(String.format("%.2f", currentRental.getFinalPayableAmount()));
            securityDepositField.setText(String.format("%.2f", currentRental.getSecurityDeposit()));

            // Get customer and equipment names
            Customer customer = customerService.findById(currentRental.getCustomerId());
            Equipment equipment = equipmentService.findById(currentRental.getEquipmentId());

            customerField.setText(customer != null ? customer.getName() : "N/A");
            equipmentField.setText(equipment != null ? (equipment.getBrand() + " " + equipment.getModel()) : "N/A");

            // Calculate days rented
            long daysRented = ChronoUnit.DAYS.between(currentRental.getStartDate(), currentRental.getEndDate());
            daysRentedField.setText(String.valueOf(daysRented));

            // Show original rental calculation breakdown
            buildOriginalRentalBreakdown();

            calculateSettlement();
        } catch (Exception e) {
            showError("Error loading rental details", e.getMessage());
        }
    }

    private void buildOriginalRentalBreakdown() {
        StringBuilder breakdown = new StringBuilder();
        breakdown.append("=== ORIGINAL RENTAL CALCULATION ===\n");
        breakdown.append("(Breakdown at time of rental creation)\n\n");
        
        long daysRented = ChronoUnit.DAYS.between(currentRental.getStartDate(), currentRental.getEndDate());
        double baseAmount = currentRental.getCalculatedRentalAmount();
        double membershipDiscount = currentRental.getMembershipDiscount();
        double longRentalDiscount = currentRental.getLongRentalDiscount();
        double finalAmount = currentRental.getFinalPayableAmount();
        double deposit = currentRental.getSecurityDeposit();
        
        breakdown.append(String.format("Duration: %d days\n", daysRented));
        breakdown.append(String.format("Base Amount: %.2f\n\n", baseAmount));
        
        breakdown.append("DISCOUNTS APPLIED AT RENTAL TIME:\n");
        if (membershipDiscount > 0) {
            double percent = (membershipDiscount / baseAmount) * 100;
            breakdown.append(String.format("  - Membership Discount (%.1f%%): -%.2f\n", percent, membershipDiscount));
        }
        if (longRentalDiscount > 0) {
            double percent = (longRentalDiscount / baseAmount) * 100;
            breakdown.append(String.format("  - Long-Rental Discount (%.1f%%): -%.2f\n", percent, longRentalDiscount));
        }
        if (membershipDiscount == 0 && longRentalDiscount == 0) {
            breakdown.append("  (No discounts)\n");
        }
        
        double totalDiscount = membershipDiscount + longRentalDiscount;
        breakdown.append(String.format("\nTotal Discounts: -%.2f\n", totalDiscount));
        breakdown.append(String.format("\nFinal Rental Amount: %.2f\n", finalAmount));
        breakdown.append(String.format("Security Deposit: %.2f\n", deposit));
        breakdown.append(String.format("─────────────────────\n"));
        breakdown.append(String.format("Total Charged: %.2f\n", finalAmount + deposit));
        
        breakdownArea.setText(breakdown.toString());
    }

    private void calculateSettlement() {
        if (currentRental == null) {
            return;
        }

        try {
            LocalDate returnDate = actualReturnDatePicker.getValue();
            if (returnDate == null) {
                returnDate = LocalDate.now();
            }

            // Calculate days late
            long daysLate = 0;
            if (returnDate.isAfter(currentRental.getEndDate())) {
                daysLate = ChronoUnit.DAYS.between(currentRental.getEndDate(), returnDate);
            }
            daysLateField.setText(String.valueOf(daysLate));

            // Calculate late fee. Prefer category default late-fee-per-day; if not provided allow manual edit
            double lateFee = 0;
            try {
                Equipment equipment = equipmentService.findById(currentRental.getEquipmentId());
                Category category = null;
                if (equipment != null) {
                    category = categoryService.findById(equipment.getCategoryId());
                }
                double perDay = category != null ? category.getDefaultLateFeePerDay() : 0.0;
                if (perDay > 0) {
                    lateFee = daysLate * perDay;
                    lateFeeField.setEditable(false);
                    lateFeeField.setText(String.format("%.2f", lateFee));
                } else {
                    // allow manual entry when category doesn't define a value
                    lateFeeField.setEditable(true);
                    try {
                        String txt = lateFeeField.getText();
                        if (txt == null || txt.isBlank()) {
                            lateFee = 0;
                            lateFeeField.setText(String.format("%.2f", lateFee));
                        } else {
                            lateFee = Double.parseDouble(txt);
                        }
                    } catch (NumberFormatException ex) {
                        lateFee = 0;
                        lateFeeField.setText(String.format("%.2f", lateFee));
                    }
                }
            } catch (Exception ex) {
                // fallback to default per-day 500 if services fail
                lateFee = daysLate * 500;
                lateFeeField.setEditable(true);
                lateFeeField.setText(String.format("%.2f", lateFee));
            }

            // Get damage charge
            double damageCharge = 0;
            if (damagedCheckBox.isSelected()) {
                try {
                    damageCharge = Double.parseDouble(damageChargeField.getText());
                } catch (NumberFormatException e) {
                    damageCharge = 0;
                }
            }

            // Calculate total charges: rental amount + late fees + damage charges - security deposit
            double rentalAmount = currentRental.getFinalPayableAmount();
            double securityDeposit = currentRental.getSecurityDeposit();
            double totalCharges = rentalAmount + lateFee + damageCharge;

            totalChargesField.setText(String.format("%.2f", totalCharges));

            // Settlement calculation:
            // If security deposit >= total charges: customer gets refunded
            // If security deposit < total charges: customer pays the balance
            double balance = securityDeposit - totalCharges;

            balanceField.setText(String.format("%.2f", Math.abs(balance)));

            if (balance > 0) {
                settlementTypeLabel.setText("✓ REFUND TO CUSTOMER: LKR " + String.format("%.2f", balance));
                settlementTypeLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            } else if (balance < 0) {
                settlementTypeLabel.setText("✗ PAYMENT DUE FROM CUSTOMER: LKR " + String.format("%.2f", Math.abs(balance)));
                settlementTypeLabel.setStyle("-fx-text-fill: #d9534f; -fx-font-weight: bold;");
            } else {
                settlementTypeLabel.setText("= NO BALANCE (Deposit covers all charges)");
                settlementTypeLabel.setStyle("-fx-text-fill: #5cb85c; -fx-font-weight: bold;");
            }

            // Build settlement breakdown
            buildSettlementBreakdown(rentalAmount, securityDeposit, lateFee, damageCharge, totalCharges, balance);
        } catch (Exception e) {
            showError("Error calculating settlement", e.getMessage());
        }
    }

    private void buildSettlementBreakdown(double rentalAmount, double securityDeposit, double lateFee, 
                                          double damageCharge, double totalCharges, double balance) {
        StringBuilder breakdown = new StringBuilder();
        
        // First, show the original rental calculation (using stored values)
        long daysRented = ChronoUnit.DAYS.between(currentRental.getStartDate(), currentRental.getEndDate());
        double baseAmount = currentRental.getCalculatedRentalAmount();
        double membershipDiscount = currentRental.getMembershipDiscount();
        double longRentalDiscount = currentRental.getLongRentalDiscount();
        
        breakdown.append("=== ORIGINAL RENTAL (Stored at rental creation) ===\n");
        breakdown.append(String.format("Duration: %d days\n", daysRented));
        breakdown.append(String.format("Base Rental Amount: %.2f\n", baseAmount));
        if (membershipDiscount > 0 || longRentalDiscount > 0) {
            if (membershipDiscount > 0) breakdown.append(String.format("  - Membership Discount: -%.2f\n", membershipDiscount));
            if (longRentalDiscount > 0) breakdown.append(String.format("  - Long-Rental Discount: -%.2f\n", longRentalDiscount));
            breakdown.append(String.format("  = Rental Amount: %.2f\n", rentalAmount));
        }
        breakdown.append(String.format("Security Deposit: %.2f\n\n", securityDeposit));
        
        // Then show the settlement calculation
        breakdown.append("=== SETTLEMENT CALCULATION ===\n");
        breakdown.append("CHARGES & DEDUCTIONS:\n");
        breakdown.append(String.format("  Original Rental Amount: %.2f\n", rentalAmount));
        
        if (lateFee > 0 || damageCharge > 0) {
            breakdown.append("\nADDITIONAL CHARGES:\n");
            if (lateFee > 0) {
                long daysLate = ChronoUnit.DAYS.between(currentRental.getEndDate(), actualReturnDatePicker.getValue());
                breakdown.append(String.format("  Late Fees (%d days): +%.2f\n", Math.max(0, daysLate), lateFee));
            }
            if (damageCharge > 0) {
                breakdown.append(String.format("  Damage Charges: +%.2f\n", damageCharge));
            }
        }
        
        breakdown.append(String.format("\nTOTAL CHARGES: %.2f\n", totalCharges));
        
        breakdown.append("\nSETTLEMENT:\n");
        breakdown.append(String.format("  Security Deposit Paid: %.2f\n", securityDeposit));
        breakdown.append(String.format("  Total Charges: -%.2f\n", totalCharges));
        breakdown.append(String.format("  ─────────────────────────────\n"));
        
        if (balance > 0) {
            breakdown.append(String.format("  ✓ REFUND TO CUSTOMER: +%.2f\n", balance));
        } else if (balance < 0) {
            breakdown.append(String.format("  ✗ PAYMENT DUE FROM CUSTOMER: %.2f\n", Math.abs(balance)));
        } else {
            breakdown.append("  = NO BALANCE (Settled)\n");
        }
        
        breakdownArea.setText(breakdown.toString());
    }

    @FXML
    public void processReturnAndSettlement() {
        try {
            // Validation
            if (actualReturnDatePicker.getValue() == null) {
                showError("Validation Error", "Please select actual return date");
                return;
            }

            if (damagedCheckBox.isSelected() && damageDescriptionArea.getText().trim().isEmpty()) {
                showError("Validation Error", "Please describe the damage");
                return;
            }

            LocalDate actualReturnDate = actualReturnDatePicker.getValue();
            String damageDesc = damagedCheckBox.isSelected() ? damageDescriptionArea.getText() : "";
            double damageCharge = damagedCheckBox.isSelected() ? Double.parseDouble(damageChargeField.getText()) : 0;

                // Process return through service
                Rental processedRental = rentalService.processReturn(
                    currentRental.getId(),
                    actualReturnDate,
                    damageDesc,
                    damageCharge
                );

                // Ensure late fee persisted matches categories/manual choice above
                double controllerLateFee = 0;
                try {
                controllerLateFee = Double.parseDouble(lateFeeField.getText());
                } catch (Exception ex) {
                controllerLateFee = processedRental.getLateFee();
                }
                processedRental.setLateFee(controllerLateFee);
                // Recalculate final payable amount to reflect controller late fee
                double finalAmount = processedRental.getCalculatedRentalAmount()
                    - processedRental.getSecurityDeposit()
                    + processedRental.getDamageCharge()
                    + processedRental.getLateFee();
                processedRental.setFinalPayableAmount(Math.max(0, finalAmount));
                // persist updated late fee and final amount
                rentalService.update(processedRental);

            // Update equipment status
            Equipment equipment = equipmentService.findById(currentRental.getEquipmentId());
            if (equipment != null) {
                if (damagedCheckBox.isSelected()) {
                    equipment.setStatus(EquipmentStatus.UNDER_MAINTENANCE);
                } else {
                    equipment.setStatus(EquipmentStatus.AVAILABLE);
                }
                equipmentService.update(equipment);
            }

            // Update payment status to PAID since settlement processed here
            processedRental.setPaymentStatus(PaymentStatus.PAID);
            // Persist payment status update
            rentalService.update(processedRental);

            // Display settlement summary
            double balance = currentRental.getSecurityDeposit() - (processedRental.getLateFee() + processedRental.getDamageCharge());
            String settlementMsg;
            if (balance > 0) {
                settlementMsg = String.format("REFUND TO CUSTOMER: LKR %.2f\n\n", balance)
                        + String.format("Rental Amount: LKR %.2f\n", currentRental.getFinalPayableAmount())
                        + String.format("Security Deposit: LKR %.2f\n", currentRental.getSecurityDeposit())
                        + String.format("Late Fees: LKR %.2f\n", processedRental.getLateFee())
                        + String.format("Damage Charges: LKR %.2f\n", processedRental.getDamageCharge())
                        + String.format("---\nRefund: LKR %.2f", balance);
            } else if (balance < 0) {
                settlementMsg = String.format("PAYMENT DUE FROM CUSTOMER: LKR %.2f\n\n", Math.abs(balance))
                        + String.format("Rental Amount: LKR %.2f\n", currentRental.getFinalPayableAmount())
                        + String.format("Security Deposit: LKR %.2f\n", currentRental.getSecurityDeposit())
                        + String.format("Late Fees: LKR %.2f\n", processedRental.getLateFee())
                        + String.format("Damage Charges: LKR %.2f\n", processedRental.getDamageCharge())
                        + String.format("---\nPayment Due: LKR %.2f", Math.abs(balance));
            } else {
                settlementMsg = "SETTLEMENT COMPLETE\n\n"
                        + String.format("Security Deposit fully covers all charges.\n\n")
                        + String.format("Rental Amount: LKR %.2f\n", currentRental.getFinalPayableAmount())
                        + String.format("Late Fees: LKR %.2f\n", processedRental.getLateFee())
                        + String.format("Damage Charges: LKR %.2f", processedRental.getDamageCharge());
            }

            showAlert("Settlement Processed", "Return & Settlement Complete", settlementMsg);
            closeForm();
        } catch (Exception e) {
            showError("Error processing return", e.getMessage());
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
