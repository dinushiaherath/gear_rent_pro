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
import lk.ijse.gear_rent_pro.model.Rental;
import lk.ijse.gear_rent_pro.model.RentalStatus;
import lk.ijse.gear_rent_pro.service.BranchService;
import lk.ijse.gear_rent_pro.service.CustomerService;
import lk.ijse.gear_rent_pro.service.EquipmentService;
import lk.ijse.gear_rent_pro.service.RentalService;
import lk.ijse.gear_rent_pro.service.impl.BranchServiceImpl;
import lk.ijse.gear_rent_pro.service.impl.CustomerServiceImpl;
import lk.ijse.gear_rent_pro.service.impl.EquipmentServiceImpl;
import lk.ijse.gear_rent_pro.service.impl.RentalServiceImpl;

public class RentalController {

    @FXML
    private TableView<Rental> rentalTable;

    @FXML
    private TableColumn<Rental, String> rentalIdCol;
    @FXML
    private TableColumn<Rental, String> customerCol;
    @FXML
    private TableColumn<Rental, String> equipmentCol;
    @FXML
    private TableColumn<Rental, String> startDateCol;
    @FXML
    private TableColumn<Rental, String> endDateCol;
    @FXML
    private TableColumn<Rental, Double> amountCol;
    @FXML
    private TableColumn<Rental, String> statusCol;
    @FXML
    private TableColumn<Rental, String> paymentCol;

    private final RentalService rentalService = new RentalServiceImpl();
    private final EquipmentService equipmentService = new EquipmentServiceImpl();
    private final CustomerService customerService = new CustomerServiceImpl();
    private final BranchService branchService = new BranchServiceImpl();
    @FXML
    private javafx.scene.control.ComboBox<lk.ijse.gear_rent_pro.model.Branch> branchFilterCombo;
    @FXML
    private TextField customerSearchField;
    @FXML
    private DatePicker startDateFilterPicker;
    @FXML
    private DatePicker endDateFilterPicker;

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
                branchFilterCombo.valueProperty().addListener((obs, old, nw) -> refreshRentalTable());
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
            startDateFilterPicker.valueProperty().addListener((obs, old, nw) -> refreshRentalTable());
        }
        if (endDateFilterPicker != null) {
            endDateFilterPicker.valueProperty().addListener((obs, old, nw) -> refreshRentalTable());
        }

        // Setup customer search
        if (customerSearchField != null) {
            customerSearchField.textProperty().addListener((obs, old, nw) -> refreshRentalTable());
        }

        refreshRentalTable();
    }

    private void setupTableColumns() {
        rentalIdCol.setCellValueFactory(new PropertyValueFactory<>("rentalId"));
        startDateCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateCol.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        amountCol.setCellValueFactory(new PropertyValueFactory<>("finalPayableAmount"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("rentalStatus"));
        paymentCol.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));

        // Custom cell factories for customer and equipment names
        customerCol.setCellFactory(col -> new TableCell<Rental, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    Rental rental = getTableRow().getItem();
                    try {
                        Customer customer = customerService.findById(rental.getCustomerId());
                        setText(customer != null ? customer.getName() : "N/A");
                    } catch (Exception e) {
                        setText("Error");
                    }
                }
            }
        });

        equipmentCol.setCellFactory(col -> new TableCell<Rental, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    Rental rental = getTableRow().getItem();
                    try {
                        Equipment equipment = equipmentService.findById(rental.getEquipmentId());
                        setText(equipment != null ? (equipment.getBrand() + " " + equipment.getModel()) : "N/A");
                    } catch (Exception e) {
                        setText("Error");
                    }
                }
            }
        });
    }

    @FXML
    public void showNewRentalForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lk/ijse/ui/rentals/form.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("New Rental");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setOnHidden(e -> refreshRentalTable());
            stage.showAndWait();
        } catch (IOException e) {
            showAlert("Error", "Failed to load rental form", e.getMessage());
        }
    }

    @FXML
    public void processPayment() {
        Rental selectedRental = rentalTable.getSelectionModel().getSelectedItem();
        if (selectedRental == null) {
            showAlert("Warning", "No Selection", "Please select a rental to process payment/return");
            return;
        }

        if (selectedRental.getRentalStatus() != RentalStatus.ACTIVE && selectedRental.getRentalStatus() != RentalStatus.OVERDUE) {
            showAlert("Warning", "Invalid Status", "Can only process return for ACTIVE or OVERDUE rentals");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lk/ijse/ui/rentals/return-form.fxml"));
            Scene scene = new Scene(loader.load());

            ReturnFormController controller = loader.getController();
            if (controller != null) {
                controller.setRental(selectedRental);
            }

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Process Return & Settlement");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setOnHidden(e -> refreshRentalTable());
            stage.showAndWait();
        } catch (IOException e) {
            showAlert("Error", "Failed to load return form", e.getMessage());
        }
    }

    @FXML
    public void refreshRentalTable() {
        try {
            LocalDate today = LocalDate.now();
            rentalService.markOverdueRentals(today);
            List<Rental> rentals = rentalService.findAll();
            lk.ijse.gear_rent_pro.model.Branch sel = null;
            try {
                sel = branchFilterCombo != null ? branchFilterCombo.getValue() : null;
            } catch (Exception ignored) {
            }
            if (sel != null) {
                final int selId = sel.getId();
                rentals = rentals.stream().filter(r -> r.getBranchId() == selId).collect(Collectors.toList());
            }

            // Filter by customer name
            String customerSearch = null;
            try {
                customerSearch = customerSearchField != null ? customerSearchField.getText().trim() : "";
            } catch (Exception ignored) {
            }
            if (customerSearch != null && !customerSearch.isEmpty()) {
                final String search = customerSearch.toLowerCase();
                rentals = rentals.stream().filter(r -> {
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
                rentals = rentals.stream().filter(r -> {
                    if (sd != null && r.getStartDate() != null && r.getStartDate().isBefore(sd)) {
                        return false;
                    }
                    if (ed != null && r.getEndDate() != null && r.getEndDate().isAfter(ed)) {
                        return false;
                    }
                    return true;
                }).collect(Collectors.toList());
            }

            ObservableList<Rental> data = FXCollections.observableArrayList(rentals);
            rentalTable.setItems(data);
        } catch (Exception e) {
            showAlert("Error", "Failed to load rentals", e.getMessage());
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
        refreshRentalTable();
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
