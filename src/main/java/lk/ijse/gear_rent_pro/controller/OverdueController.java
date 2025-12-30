package lk.ijse.gear_rent_pro.controller;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import lk.ijse.gear_rent_pro.model.Customer;
import lk.ijse.gear_rent_pro.model.Equipment;
import lk.ijse.gear_rent_pro.model.Rental;
import lk.ijse.gear_rent_pro.service.CustomerService;
import lk.ijse.gear_rent_pro.service.EquipmentService;
import lk.ijse.gear_rent_pro.service.RentalService;
import lk.ijse.gear_rent_pro.service.impl.CustomerServiceImpl;
import lk.ijse.gear_rent_pro.service.impl.EquipmentServiceImpl;
import lk.ijse.gear_rent_pro.service.impl.RentalServiceImpl;

public class OverdueController {

    @FXML
    private TableView<OverdueRow> overdueTable;

    private final RentalService rentalService = new RentalServiceImpl();
    private final CustomerService customerService = new CustomerServiceImpl();
    private final EquipmentService equipmentService = new EquipmentServiceImpl();

    @FXML
    public void initialize() {
        refreshOverdueTable();
    }

    @FXML
    public void contactCustomer() {
        OverdueRow sel = overdueTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert("Validation", "No selection", "Please select an overdue rental to contact.");
            return;
        }
        String contact = sel.getCustomerContact() != null ? sel.getCustomerContact() : "-";
        String email = sel.getCustomerEmail() != null ? sel.getCustomerEmail() : "-";
        String msg = String.format("Customer: %s\nContact: %s\nEmail: %s", sel.getCustomerName(), contact, email);
        showAlert("Contact Customer", "Contact info", msg);
    }

    @FXML
    public void refreshOverdueTable() {
        try {
            // Let the service/DAO bulk-mark overdue rentals (ACTIVE & end_date < today)
            LocalDate today = LocalDate.now();
            rentalService.markOverdueRentals(today);

            // After updates, load current overdue rentals for display
            List<Rental> updated = rentalService.findOverdueRentals();

            ObservableList<OverdueRow> rows = FXCollections.observableArrayList();
            for (Rental r : updated) {
                Customer c = null;
                Equipment eq = null;
                try { c = customerService.findById(r.getCustomerId()); } catch (Exception ignore) {}
                try { eq = equipmentService.findById(r.getEquipmentId()); } catch (Exception ignore) {}

                long daysOverdue = 0;
                if (r.getEndDate() != null && r.getEndDate().isBefore(today)) {
                    daysOverdue = ChronoUnit.DAYS.between(r.getEndDate(), today);
                }

                OverdueRow row = new OverdueRow(
                        r.getRentalId(),
                        c != null ? c.getName() : "-",
                        eq != null ? (eq.getBrand() + " " + eq.getModel()) : "-",
                        r.getEndDate(),
                        (int) daysOverdue,
                        c != null ? c.getContactNo() : "-",
                        c != null ? c.getEmail() : "-"
                );
                rows.add(row);
            }

            overdueTable.setItems(rows);
        } catch (Exception e) {
            showAlert("Error", "Failed to load overdue rentals", e.getMessage());
        }
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Simple DTO for table rows.
     */
    public static class OverdueRow {
        private final String rentalId;
        private final String customerName;
        private final String equipmentDescription;
        private final java.time.LocalDate endDate;
        private final int daysOverdue;
        private final String customerContact;
        private final String customerEmail;

        public OverdueRow(String rentalId, String customerName, String equipmentDescription,
                          LocalDate endDate, int daysOverdue, String customerContact, String customerEmail) {
            this.rentalId = rentalId;
            this.customerName = customerName;
            this.equipmentDescription = equipmentDescription;
            this.endDate = endDate;
            this.daysOverdue = daysOverdue;
            this.customerContact = customerContact;
            this.customerEmail = customerEmail;
        }

        public String getRentalId() { return rentalId; }
        public String getCustomerName() { return customerName; }
        public String getEquipmentDescription() { return equipmentDescription; }
        public LocalDate getEndDate() { return endDate; }
        public int getDaysOverdue() { return daysOverdue; }
        public String getCustomerContact() { return customerContact; }
        public String getCustomerEmail() { return customerEmail; }
    }
}
