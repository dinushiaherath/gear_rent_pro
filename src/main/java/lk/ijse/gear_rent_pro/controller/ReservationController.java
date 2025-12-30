package lk.ijse.gear_rent_pro.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lk.ijse.gear_rent_pro.model.Branch;
import lk.ijse.gear_rent_pro.model.Customer;
import lk.ijse.gear_rent_pro.model.Equipment;
import lk.ijse.gear_rent_pro.model.EquipmentStatus;
import lk.ijse.gear_rent_pro.model.PaymentStatus;
import lk.ijse.gear_rent_pro.model.Rental;
import lk.ijse.gear_rent_pro.model.RentalStatus;
import lk.ijse.gear_rent_pro.model.Reservation;
import lk.ijse.gear_rent_pro.service.BranchService;
import lk.ijse.gear_rent_pro.service.CustomerService;
import lk.ijse.gear_rent_pro.service.EquipmentService;
import lk.ijse.gear_rent_pro.service.RentalService;
import lk.ijse.gear_rent_pro.service.ReservationService;
import lk.ijse.gear_rent_pro.service.impl.BranchServiceImpl;
import lk.ijse.gear_rent_pro.service.impl.CustomerServiceImpl;
import lk.ijse.gear_rent_pro.service.impl.EquipmentServiceImpl;
import lk.ijse.gear_rent_pro.service.impl.RentalServiceImpl;
import lk.ijse.gear_rent_pro.service.impl.ReservationServiceImpl;

public class ReservationController {

    @FXML
    private TableView<Reservation> reservationTable;

    @FXML
    private TableColumn<Reservation, String> reservationIdCol;
    @FXML
    private TableColumn<Reservation, String> customerCol;
    @FXML
    private TableColumn<Reservation, String> equipmentCol;
    @FXML
    private TableColumn<Reservation, String> startDateCol;
    @FXML
    private TableColumn<Reservation, String> endDateCol;

    private final ReservationService reservationService = new ReservationServiceImpl();
    private final EquipmentService equipmentService = new EquipmentServiceImpl();
    private final CustomerService customerService = new CustomerServiceImpl();
    private final RentalService rentalService = new RentalServiceImpl();
    private final BranchService branchService = new BranchServiceImpl();
    @FXML
    private javafx.scene.control.ComboBox<lk.ijse.gear_rent_pro.model.Branch> branchFilterCombo;
    @FXML
    private TextField customerSearchField;
    @FXML
    private DatePicker startDateFilterPicker;
    @FXML
    private DatePicker endDateFilterPicker;

    private boolean datesOverlap(LocalDate aStart, LocalDate aEnd, LocalDate bStart, LocalDate bEnd) {
        if (aStart == null || aEnd == null || bStart == null || bEnd == null) {
            return false;
        }
        return !(aEnd.isBefore(bStart) || aStart.isAfter(bEnd));
    }

    private boolean isEquipmentAvailableForPeriod(Equipment equipment, LocalDate start, LocalDate end) {
        if (equipment == null || start == null || end == null) {
            return false;
        }
        if (start.isAfter(end)) {
            return false;
        }

        if (equipment.getStatus() == EquipmentStatus.UNDER_MAINTENANCE) {
            return false;
        }

        try {
            List<Rental> rentals = rentalService.findAll().stream()
                    .filter(r -> r.getEquipmentId() == equipment.getId())
                    .collect(Collectors.toList());
            for (Rental r : rentals) {
                if (r.getStartDate() == null || r.getEndDate() == null) {
                    continue;
                }
                if (RentalStatus.ACTIVE.equals(r.getRentalStatus()) && datesOverlap(r.getStartDate(), r.getEndDate(), start, end)) {
                    return false;
                }
            }

            List<Reservation> reservations = reservationService.findAll().stream()
                    .filter(res -> res.getEquipmentId() == equipment.getId())
                    .collect(Collectors.toList());
            for (Reservation res : reservations) {
                if (res.getStartDate() == null || res.getEndDate() == null) {
                    continue;
                }
                if (datesOverlap(res.getStartDate(), res.getEndDate(), start, end)) {
                    // allow the same reservation being converted, but check others
                    // The caller will ensure selectedReservation is the one being converted
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        // Setup branch filter
        var user = lk.ijse.gear_rent_pro.util.SessionContext.getInstance().getCurrentUser();
        try {
            if (user != null && user.getRole() == lk.ijse.gear_rent_pro.model.UserRole.ADMIN) {
                var branches = branchService.findAll();
                branchFilterCombo.setItems(FXCollections.observableArrayList(branches));
                branchFilterCombo.getItems().add(0, null);
                Supplier<javafx.scene.control.ListCell<Branch>> branchCellSupplier = () -> new javafx.scene.control.ListCell<Branch>() {
                    @Override
                    protected void updateItem(Branch item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? "All Branches" : item.getName());
                    }
                };
                branchFilterCombo.setCellFactory(param -> branchCellSupplier.get());
                branchFilterCombo.setButtonCell(branchCellSupplier.get());
                branchFilterCombo.valueProperty().addListener((obs, old, nw) -> refreshReservationTable());
            } else if (user != null && user.getBranchId() != null) {
                var b = branchService.findById(user.getBranchId());
                if (b != null) {
                    branchFilterCombo.setItems(FXCollections.observableArrayList(b));
                    branchFilterCombo.setValue(b);
                    branchFilterCombo.setDisable(true);
                }
            }
        } catch (Exception e) {
        }

        // Setup date filters
        if (startDateFilterPicker != null) {
            startDateFilterPicker.valueProperty().addListener((obs, old, nw) -> refreshReservationTable());
        }
        if (endDateFilterPicker != null) {
            endDateFilterPicker.valueProperty().addListener((obs, old, nw) -> refreshReservationTable());
        }

        // Setup customer search
        if (customerSearchField != null) {
            customerSearchField.textProperty().addListener((obs, old, nw) -> refreshReservationTable());
        }

        refreshReservationTable();
    }

    private void setupTableColumns() {
        reservationIdCol.setCellValueFactory(new PropertyValueFactory<>("reservationId"));
        startDateCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateCol.setCellValueFactory(new PropertyValueFactory<>("endDate"));

        // Custom cell factories for customer and equipment names
        customerCol.setCellFactory(col -> new TableCell<Reservation, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    Reservation reservation = getTableRow().getItem();
                    try {
                        Customer customer = customerService.findById(reservation.getCustomerId());
                        setText(customer != null ? customer.getName() : "N/A");
                    } catch (Exception e) {
                        setText("Error");
                    }
                }
            }
        });

        equipmentCol.setCellFactory(col -> new TableCell<Reservation, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    Reservation reservation = getTableRow().getItem();
                    try {
                        Equipment equipment = equipmentService.findById(reservation.getEquipmentId());
                        setText(equipment != null ? (equipment.getBrand() + " " + equipment.getModel()) : "N/A");
                    } catch (Exception e) {
                        setText("Error");
                    }
                }
            }
        });
    }

    @FXML
    public void showNewReservationForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lk/ijse/ui/reservations/form.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("New Reservation");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setOnHidden(e -> refreshReservationTable());
            stage.showAndWait();
        } catch (IOException e) {
            showAlert("Error", "Failed to load reservation form", e.getMessage());
        }
    }

    @FXML
    public void convertToRental() {
        Reservation selectedReservation = reservationTable.getSelectionModel().getSelectedItem();
        if (selectedReservation == null) {
            showAlert("Warning", "No Selection", "Please select a reservation to convert");
            return;
        }

        try {
            Equipment equipment = equipmentService.findById(selectedReservation.getEquipmentId());
            // Verify availability for the reservation period (ignoring the reservation itself)
            boolean conflict = false;
            try {
                List<Rental> rentals = rentalService.findAll().stream()
                        .filter(r -> r.getEquipmentId() == selectedReservation.getEquipmentId())
                        .collect(Collectors.toList());
                for (Rental r : rentals) {
                    if (r.getStartDate() == null || r.getEndDate() == null) {
                        continue;
                    }
                    if (RentalStatus.ACTIVE.equals(r.getRentalStatus()) && datesOverlap(r.getStartDate(), r.getEndDate(), selectedReservation.getStartDate(), selectedReservation.getEndDate())) {
                        conflict = true;
                        break;
                    }
                }
                if (!conflict) {
                    List<Reservation> reservations = reservationService.findAll().stream()
                            .filter(res -> res.getEquipmentId() == selectedReservation.getEquipmentId() && res.getId() != selectedReservation.getId())
                            .collect(Collectors.toList());
                    for (Reservation res : reservations) {
                        if (res.getStartDate() == null || res.getEndDate() == null) {
                            continue;
                        }
                        if (datesOverlap(res.getStartDate(), res.getEndDate(), selectedReservation.getStartDate(), selectedReservation.getEndDate())) {
                            conflict = true;
                            break;
                        }
                    }
                }
            } catch (Exception ex) {
                conflict = true;
            }

            if (conflict) {
                showAlert("Error", "Conversion Failed", "Cannot convert reservation: equipment is no longer available for that period.");
                return;
            }
            // Create rental from reservation
            Rental rental = new Rental();
            rental.setRentalId("RENT-" + System.currentTimeMillis());
            rental.setEquipmentId(selectedReservation.getEquipmentId());
            rental.setCustomerId(selectedReservation.getCustomerId());
            rental.setBranchId(selectedReservation.getBranchId());
            rental.setStartDate(selectedReservation.getStartDate());
            rental.setEndDate(selectedReservation.getEndDate());
            rental.setPaymentStatus(PaymentStatus.UNPAID);
            rental.setRentalStatus(RentalStatus.ACTIVE);

            // Create rental
            Rental created = rentalService.create(rental);

            // Delete reservation
            if (reservationService.delete(selectedReservation.getId())) {
                showAlert("Success", "Conversion Successful", "Reservation has been converted to rental");
                refreshReservationTable();
            } else {
                showAlert("Error", "Conversion Failed", "Unable to delete reservation");
            }
        } catch (Exception e) {
            showAlert("Error", "Conversion Error", e.getMessage());
        }
    }

    @FXML
    public void cancelReservation() {
        Reservation selectedReservation = reservationTable.getSelectionModel().getSelectedItem();
        if (selectedReservation == null) {
            showAlert("Warning", "No Selection", "Please select a reservation to cancel");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Cancellation");
        confirmAlert.setHeaderText("Cancel Reservation?");
        confirmAlert.setContentText("Are you sure you want to cancel this reservation?");

        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            try {
                if (reservationService.delete(selectedReservation.getId())) {
                    showAlert("Success", "Reservation Cancelled", "Reservation has been cancelled successfully");
                    refreshReservationTable();
                } else {
                    showAlert("Error", "Cancellation Failed", "Unable to cancel reservation");
                }
            } catch (Exception e) {
                showAlert("Error", "Cancellation Error", e.getMessage());
            }
        }
    }

    @FXML
    public void refreshReservationTable() {
        try {
            List<Reservation> reservations = reservationService.findAll();
            lk.ijse.gear_rent_pro.model.Branch sel = null;
            try {
                sel = branchFilterCombo != null ? branchFilterCombo.getValue() : null;
            } catch (Exception ignored) {
            }
            if (sel != null) {
                final int selId = sel.getId();
                reservations = reservations.stream().filter(r -> r.getBranchId() == selId).collect(Collectors.toList());
            }

            // Filter by customer name
            String customerSearch = null;
            try {
                customerSearch = customerSearchField != null ? customerSearchField.getText().trim() : "";
            } catch (Exception ignored) {
            }
            if (customerSearch != null && !customerSearch.isEmpty()) {
                final String search = customerSearch.toLowerCase();
                reservations = reservations.stream().filter(r -> {
                    try {
                        Customer c = customerService.findById(r.getCustomerId());
                        return c != null && c.getName() != null && c.getName().toLowerCase().contains(search);
                    } catch (Exception e) {
                        return false;
                    }
                }).collect(Collectors.toList());
            }

            // Filter by date range
            LocalDate startFilter = null;
            LocalDate endFilter = null;
            try {
                startFilter = startDateFilterPicker != null ? startDateFilterPicker.getValue() : null;
            } catch (Exception ignored) {
            }
            try {
                endFilter = endDateFilterPicker != null ? endDateFilterPicker.getValue() : null;
            } catch (Exception ignored) {
            }
            if (startFilter != null || endFilter != null) {
                final LocalDate sd = startFilter;
                final LocalDate ed = endFilter;
                reservations = reservations.stream().filter(r -> {
                    if (sd != null && r.getStartDate() != null && r.getStartDate().isBefore(sd)) {
                        return false;
                    }
                    if (ed != null && r.getEndDate() != null && r.getEndDate().isAfter(ed)) {
                        return false;
                    }
                    return true;
                }).collect(Collectors.toList());
            }

            ObservableList<Reservation> data = FXCollections.observableArrayList(reservations);
            reservationTable.setItems(data);
        } catch (Exception e) {
            showAlert("Error", "Failed to load reservations", e.getMessage());
        }
    }

    @FXML
    public void clearFilters() {
        if (customerSearchField != null) {
            customerSearchField.clear();
        }
        if (startDateFilterPicker != null) {
            startDateFilterPicker.setValue(null);
        }
        if (endDateFilterPicker != null) {
            endDateFilterPicker.setValue(null);
        }
        refreshReservationTable();
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
