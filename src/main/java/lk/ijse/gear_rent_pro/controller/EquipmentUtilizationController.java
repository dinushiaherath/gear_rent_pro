package lk.ijse.gear_rent_pro.controller;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;
import lk.ijse.gear_rent_pro.model.Branch;
import lk.ijse.gear_rent_pro.model.Equipment;
import lk.ijse.gear_rent_pro.model.Rental;
import lk.ijse.gear_rent_pro.model.UserRole;
import lk.ijse.gear_rent_pro.service.BranchService;
import lk.ijse.gear_rent_pro.service.EquipmentService;
import lk.ijse.gear_rent_pro.service.RentalService;
import lk.ijse.gear_rent_pro.service.impl.BranchServiceImpl;
import lk.ijse.gear_rent_pro.service.impl.EquipmentServiceImpl;
import lk.ijse.gear_rent_pro.service.impl.RentalServiceImpl;
import lk.ijse.gear_rent_pro.util.SessionContext;

public class EquipmentUtilizationController {

    @FXML
    private ComboBox<lk.ijse.gear_rent_pro.model.Branch> branchCombo;
    @FXML
    private DatePicker fromDate;
    @FXML
    private DatePicker toDate;
    @FXML
    private TableView<UtilRow> utilTable;

    private final EquipmentService equipmentService = new EquipmentServiceImpl();
    private final RentalService rentalService = new RentalServiceImpl();
    private final BranchService branchService = new BranchServiceImpl();

    @FXML
    public void initialize() {
        if (fromDate.getValue() == null) fromDate.setValue(LocalDate.now().minusMonths(1));
        if (toDate.getValue() == null) toDate.setValue(LocalDate.now());
        refreshBranches();
    }

    @FXML
    public void refreshBranches() {
        try {
            var user = SessionContext.getInstance().getCurrentUser();
            var branches = branchService.findAll();

            Supplier<javafx.scene.control.ListCell<Branch>> branchCellSupplier = () -> new javafx.scene.control.ListCell<Branch>() {
                @Override
                protected void updateItem(Branch item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "All Branches" : item.getName());
                }
            };

            if (user != null && user.getRole() == UserRole.ADMIN) {
                ObservableList<Branch> items = FXCollections.observableArrayList(branches);
                items.add(0, null);
                branchCombo.setItems(items);
                branchCombo.setCellFactory(param -> branchCellSupplier.get());
                branchCombo.setButtonCell(branchCellSupplier.get());
            } else if (user != null && user.getBranchId() != null) {
                Branch b = branchService.findById(user.getBranchId());
                if (b != null) {
                    branchCombo.setItems(FXCollections.observableArrayList(b));
                    branchCombo.setValue(b);
                    branchCombo.setDisable(true);
                    branchCombo.setCellFactory(param -> branchCellSupplier.get());
                    branchCombo.setButtonCell(branchCellSupplier.get());
                }
            } else {
                ObservableList<Branch> items = FXCollections.observableArrayList(branches);
                items.add(0, null);
                branchCombo.setItems(items);
                branchCombo.setCellFactory(param -> branchCellSupplier.get());
                branchCombo.setButtonCell(branchCellSupplier.get());
            }
        } catch (Exception e) {
            // ignore
        }
    }

    @FXML
    public void generateReport() {
        try {
            LocalDate from = fromDate.getValue();
            LocalDate to = toDate.getValue();
            if (from == null || to == null) { showAlert("Validation","Invalid dates","Select from and to date"); return; }

            int daysRange = (int) ChronoUnit.DAYS.between(from, to) + 1;

            List<Equipment> equipments = equipmentService.findAll();
            if (branchCombo.getValue() != null) {
                int bid = branchCombo.getValue().getId();
                equipments = equipments.stream().filter(e -> e.getBranchId() == bid).collect(Collectors.toList());
            }

            List<Rental> rentals = rentalService.findAll();

            ObservableList<UtilRow> rows = FXCollections.observableArrayList();
            for (Equipment e : equipments) {
                int daysRented = rentals.stream()
                        .filter(r -> r.getEquipmentId() == e.getId())
                        .filter(r -> !(r.getEndDate().isBefore(from) || r.getStartDate().isAfter(to)))
                        .mapToInt(r -> {
                            LocalDate s = r.getStartDate().isBefore(from) ? from : r.getStartDate();
                            LocalDate en = r.getEndDate().isAfter(to) ? to : r.getEndDate();
                            return (int) ChronoUnit.DAYS.between(s, en) + 1;
                        }).sum();

                int daysAvailable = Math.max(0, daysRange - daysRented);
                double utilPercent = daysRange == 0 ? 0.0 : (daysRented * 100.0 / daysRange);

                rows.add(new UtilRow(e.getBrand() + " " + e.getModel(), daysRented, daysAvailable, String.format("%.2f", utilPercent)));
            }

            utilTable.setItems(rows);
        } catch (Exception ex) {
            showAlert("Error","Failed to generate utilization",""+ex.getMessage());
        }
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static class UtilRow {
        private final String equipmentDescription;
        private final int daysRented;
        private final int daysAvailable;
        private final String utilizationPercent;

        public UtilRow(String equipmentDescription, int daysRented, int daysAvailable, String utilizationPercent) {
            this.equipmentDescription = equipmentDescription;
            this.daysRented = daysRented;
            this.daysAvailable = daysAvailable;
            this.utilizationPercent = utilizationPercent;
        }

        public String getEquipmentDescription() { return equipmentDescription; }
        public int getDaysRented() { return daysRented; }
        public int getDaysAvailable() { return daysAvailable; }
        public String getUtilizationPercent() { return utilizationPercent; }
    }
}
