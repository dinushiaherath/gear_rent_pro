package lk.ijse.gear_rent_pro.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lk.ijse.gear_rent_pro.model.*;
import lk.ijse.gear_rent_pro.service.*;
import lk.ijse.gear_rent_pro.service.impl.*;
import lk.ijse.gear_rent_pro.service.RentalService;
import lk.ijse.gear_rent_pro.service.impl.RentalServiceImpl;
import lk.ijse.gear_rent_pro.util.SessionContext;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class ReservationFormController {
    
    @FXML
    private TextField reservationIdField;
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
    
    private final ReservationService reservationService = new ReservationServiceImpl();
    private final EquipmentService equipmentService = new EquipmentServiceImpl();
    private final CustomerService customerService = new CustomerServiceImpl();
    private final BranchService branchService = new BranchServiceImpl();
    private final RentalService rentalService = new RentalServiceImpl();
    
    @FXML
    public void initialize() {
        setupFields();
        loadBranches();
        loadEquipment();
        loadCustomers();
        
        // Add listeners for equipment availability updates
        startDatePicker.valueProperty().addListener((obs, old, newVal) -> loadEquipment());
        endDatePicker.valueProperty().addListener((obs, old, newVal) -> loadEquipment());
        // Refresh equipment list when branch changes (branch affects availability)
        branchCombo.valueProperty().addListener((obs, old, newVal) -> loadEquipment());
    }
    
    private void setupFields() {
        // Generate reservation ID
        reservationIdField.setText("RES-" + System.currentTimeMillis());
        
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

        if (equipment.getStatus() == EquipmentStatus.UNDER_MAINTENANCE) return false;

        try {
            // Check rentals
            List<Rental> rentals = rentalService.findAll().stream()
                .filter(r -> r.getEquipmentId() == equipment.getId())
                .collect(Collectors.toList());
            for (Rental r : rentals) {
                if (r.getStartDate() == null || r.getEndDate() == null) continue;
                if (RentalStatus.ACTIVE.equals(r.getRentalStatus()) && datesOverlap(r.getStartDate(), r.getEndDate(), start, end)) {
                    return false;
                }
            }

            // Check reservations
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
    
    @FXML
    public void createReservation() {
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
            
            if (startDate.isAfter(endDate)) {
                showError("Validation Error", "Start date must be before or equal to end date");
                return;
            }
            
            // Create reservation object
            Reservation reservation = new Reservation();
            reservation.setReservationId(reservationIdField.getText());
            reservation.setEquipmentId(equipment.getId());
            reservation.setCustomerId(customer.getId());
            reservation.setBranchId(branch.getId());
            reservation.setStartDate(startDate);
            reservation.setEndDate(endDate);
            
            // Availability check (consider existing rentals/reservations)
            if (!isEquipmentAvailableForPeriod(equipment, startDate, endDate)) {
                showError("Validation Error", "Selected equipment is NOT available for the chosen dates.");
                return;
            }

            // Create reservation with validation
            Reservation created = reservationService.createWithValidation(reservation);
            
            showAlert("Success", "Reservation Created", "Reservation has been created successfully");
            closeForm();
        } catch (IllegalArgumentException e) {
            showError("Validation Error", e.getMessage());
        } catch (Exception e) {
            showError("Error creating reservation", e.getMessage());
        }
    }
    
    @FXML
    public void cancelForm() {
        closeForm();
    }
    
    private void closeForm() {
        Stage stage = (Stage) reservationIdField.getScene().getWindow();
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
