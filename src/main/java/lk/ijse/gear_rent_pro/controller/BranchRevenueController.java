package lk.ijse.gear_rent_pro.controller;

import java.time.LocalDate;
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
import lk.ijse.gear_rent_pro.model.Rental;
import lk.ijse.gear_rent_pro.model.UserRole;
import lk.ijse.gear_rent_pro.service.BranchService;
import lk.ijse.gear_rent_pro.service.RentalService;
import lk.ijse.gear_rent_pro.service.impl.BranchServiceImpl;
import lk.ijse.gear_rent_pro.service.impl.RentalServiceImpl;
import lk.ijse.gear_rent_pro.util.SessionContext;

public class BranchRevenueController {

    @FXML
    private ComboBox<Branch> branchCombo;
    @FXML
    private DatePicker fromDate;
    @FXML
    private DatePicker toDate;
    @FXML
    private TableView<BranchRevenueRow> reportTable;

    private final BranchService branchService = new BranchServiceImpl();
    private final RentalService rentalService = new RentalServiceImpl();

    @FXML
    public void initialize() {
        try {
            refreshBranches();
            if (fromDate.getValue() == null) fromDate.setValue(LocalDate.now().minusMonths(1));
            if (toDate.getValue() == null) toDate.setValue(LocalDate.now());
        } catch (Exception e) {
            System.err.println("Error init BranchRevenueController: " + e.getMessage());
        }
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

            // Admin: show all branches + an "All" option
            if (user != null && user.getRole() == UserRole.ADMIN) {
                ObservableList<Branch> items = FXCollections.observableArrayList(branches);
                items.add(0, null);
                branchCombo.setItems(items);
                branchCombo.setCellFactory(param -> branchCellSupplier.get());
                branchCombo.setButtonCell(branchCellSupplier.get());
            } else if (user != null && user.getBranchId() != null) {
                // Branch manager/staff: only their branch
                Branch b = branchService.findById(user.getBranchId());
                if (b != null) {
                    branchCombo.setItems(FXCollections.observableArrayList(b));
                    branchCombo.setValue(b);
                    branchCombo.setDisable(true);
                    branchCombo.setCellFactory(param -> branchCellSupplier.get());
                    branchCombo.setButtonCell(branchCellSupplier.get());
                } else {
                    branchCombo.setItems(FXCollections.observableArrayList(branches));
                    branchCombo.setCellFactory(param -> branchCellSupplier.get());
                    branchCombo.setButtonCell(branchCellSupplier.get());
                }
            } else {
                // fallback: show all
                ObservableList<Branch> items = FXCollections.observableArrayList(branches);
                items.add(0, null);
                branchCombo.setItems(items);
                branchCombo.setCellFactory(param -> branchCellSupplier.get());
                branchCombo.setButtonCell(branchCellSupplier.get());
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to load branches", e.getMessage());
        }
    }

    @FXML
    public void generateReport() {
        try {
            LocalDate from = fromDate.getValue();
            LocalDate to = toDate.getValue();
            if (from == null || to == null) {
                showAlert("Validation", "Invalid dates", "Please select a from and to date.");
                return;
            }
            List<Rental> rentals = rentalService.findAll();
            // filter by date range and branch
            List<Rental> filtered = rentals.stream()
                    .filter(r -> r.getStartDate() != null && r.getEndDate() != null)
                    .filter(r -> !(r.getEndDate().isBefore(from) || r.getStartDate().isAfter(to)))
                    .collect(Collectors.toList());

            // group by branch
            var grouped = filtered.stream().collect(Collectors.groupingBy(Rental::getBranchId));

            ObservableList<BranchRevenueRow> rows = FXCollections.observableArrayList();
            for (var entry : grouped.entrySet()) {
                int branchId = entry.getKey();
                if (branchCombo.getValue() != null && branchCombo.getValue().getId() != branchId) continue;
                var list = entry.getValue();
                double totalIncome = list.stream().mapToDouble(Rental::getFinalPayableAmount).sum();
                long rentalsCount = list.size();
                double totalLateFees = list.stream().mapToDouble(Rental::getLateFee).sum();
                double totalDamage = list.stream().mapToDouble(Rental::getDamageCharge).sum();
                Branch b = branchService.findById(branchId);
                rows.add(new BranchRevenueRow(b != null ? b.getName() : "Branch " + branchId, (int) rentalsCount, totalIncome, totalLateFees, totalDamage));
            }

            reportTable.setItems(rows);
        } catch (Exception e) {
            showAlert("Error", "Report generation failed", e.getMessage());
        }
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static class BranchRevenueRow {
        private final String branchName;
        private final int rentalCount;
        private final double totalIncome;
        private final double totalLateFees;
        private final double totalDamageCharges;

        public BranchRevenueRow(String branchName, int rentalCount, double totalIncome, double totalLateFees, double totalDamageCharges) {
            this.branchName = branchName;
            this.rentalCount = rentalCount;
            this.totalIncome = totalIncome;
            this.totalLateFees = totalLateFees;
            this.totalDamageCharges = totalDamageCharges;
        }

        public String getBranchName() { return branchName; }
        public int getRentalCount() { return rentalCount; }
        public double getTotalIncome() { return totalIncome; }
        public double getTotalLateFees() { return totalLateFees; }
        public double getTotalDamageCharges() { return totalDamageCharges; }
    }
}
