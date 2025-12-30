package lk.ijse.gear_rent_pro.controller;

import java.io.IOException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lk.ijse.gear_rent_pro.model.Customer;
import lk.ijse.gear_rent_pro.model.MembershipLevel;
import lk.ijse.gear_rent_pro.service.CustomerService;
import lk.ijse.gear_rent_pro.service.impl.CustomerServiceImpl;

public class CustomerController {
    
    @FXML
    private TableView<Customer> customerTable;
    @FXML
    private TableColumn<Customer, String> customerIdCol;
    @FXML
    private TableColumn<Customer, String> nameCol;
    @FXML
    private TableColumn<Customer, String> nicCol;
    @FXML
    private TableColumn<Customer, String> contactCol;
    @FXML
    private TableColumn<Customer, String> emailCol;
    @FXML
    private TableColumn<Customer, MembershipLevel> membershipCol;
    @FXML
    private TextField nameSearchField;
    @FXML
    private ComboBox<MembershipLevel> membershipFilterCombo;
    
    private final CustomerService customerService = new CustomerServiceImpl();
    
    @FXML
    public void initialize() {
        try {
            customerIdCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
            nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
            nicCol.setCellValueFactory(new PropertyValueFactory<>("nicOrPassport"));
            contactCol.setCellValueFactory(new PropertyValueFactory<>("contactNo"));
            emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
            membershipCol.setCellValueFactory(new PropertyValueFactory<>("membership"));
            membershipFilterCombo.setItems(FXCollections.observableArrayList(MembershipLevel.values()));
            membershipFilterCombo.getItems().add(0, null);
            membershipFilterCombo.setPromptText("All");
            nameSearchField.textProperty().addListener((obs, oldV, newV) -> refreshCustomerTable());
            membershipFilterCombo.valueProperty().addListener((obs, oldV, newV) -> refreshCustomerTable());
            refreshCustomerTable();
        } catch (Exception e) {
            System.err.println("Error initializing CustomerController: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    public void showAddCustomerForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lk/ijse/ui/customers/form.fxml"));
            Parent root = loader.load();
            CustomerFormController ctrl = loader.getController();
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Add Customer");
            dialog.setScene(new javafx.scene.Scene(root));
            ctrl.setDialogStage(dialog);
            ctrl.setCustomer(null);
            dialog.showAndWait();

            if (ctrl.isSaved()) {
                Customer c = ctrl.getCustomer();
                customerService.create(c);
                refreshCustomerTable();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Add Customer", e.getMessage());
        }
    }
    
    @FXML
    public void editCustomer() {
        Customer selected = customerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Validation", "No selection", "Please select a customer to edit.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lk/ijse/ui/customers/form.fxml"));
            Parent root = loader.load();
            CustomerFormController ctrl = loader.getController();
            ctrl.setDialogStage(new Stage());
            ctrl.setCustomer(selected);
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Edit Customer");
            dialog.setScene(new javafx.scene.Scene(root));
            ctrl.setDialogStage(dialog);
            dialog.showAndWait();

            if (ctrl.isSaved()) {
                Customer c = ctrl.getCustomer();
                customerService.update(c);
                refreshCustomerTable();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Edit Customer", e.getMessage());
        }
    }
    
    @FXML
    public void deleteCustomer() {
        Customer selected = customerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Validation", "No selection", "Please select a customer to delete.");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete selected customer?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm");
        confirm.showAndWait().ifPresent(b -> {
            if (b == ButtonType.YES) {
                try {
                    customerService.delete(selected.getId());
                    refreshCustomerTable();
                } catch (Exception e) {
                    if (e.getMessage() != null && e.getMessage().contains("Integrity constraint violation")) {
                        showAlert("Cannot Delete", "Customer has rentals/reservations", "This customer cannot be deleted because they have active rentals or reservations.");
                    } else {
                        showAlert("Error", "Delete failed", e.getMessage());
                    }
                }
            }
        });
    }
    
    @FXML
    public void viewCustomerHistory() {
        Customer selected = customerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Validation", "No selection", "Please select a customer to view history.");
            return;
        }
        showAlert("Info", "Customer History", "Customer history for " + selected.getName() + " - Feature coming soon.");
    }
    
    @FXML
    public void refreshCustomerTable() {
        try {
            if (customerService == null) {
                System.err.println("CustomerService is null");
                return;
            }
            ObservableList<Customer> data = FXCollections.observableArrayList(customerService.findAll());
            String search = nameSearchField != null ? nameSearchField.getText() : null;
            MembershipLevel sel = membershipFilterCombo != null ? membershipFilterCombo.getValue() : null;
            ObservableList<Customer> filtered = data.filtered(c -> {
                boolean ok = true;
                if (search != null && !search.isBlank()) {
                    ok = c.getName() != null && c.getName().toLowerCase().contains(search.toLowerCase());
                }
                if (ok && sel != null) {
                    ok = sel.equals(c.getMembership());
                }
                return ok;
            });
            customerTable.setItems(filtered);
        } catch (Exception e) {
            System.err.println("Error refreshing customer table: " + e.getMessage());
            e.printStackTrace();
            try {
                showAlert("Error", "Refresh", "Failed to load customers: " + e.getMessage());
            } catch (Exception ex) {
                System.err.println("Error showing alert: " + ex.getMessage());
            }
        }
    }

    @FXML
    public void clearFilters() {
        if (nameSearchField != null) nameSearchField.clear();
        if (membershipFilterCombo != null) membershipFilterCombo.getSelectionModel().clearSelection();
        refreshCustomerTable();
    }
    
    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
