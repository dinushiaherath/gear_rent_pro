package lk.ijse.gear_rent_pro.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lk.ijse.gear_rent_pro.model.Customer;
import lk.ijse.gear_rent_pro.model.MembershipLevel;
import lk.ijse.gear_rent_pro.service.CustomerService;
import lk.ijse.gear_rent_pro.service.impl.CustomerServiceImpl;

public class CustomerFormController {

    @FXML
    private TextField customerIdField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField nicOrPassportField;
    @FXML
    private TextField contactNoField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField addressField;
    @FXML
    private ComboBox<MembershipLevel> membershipCombo;

    private Stage dialogStage;
    private boolean saved = false;
    private Customer customer;

    private final CustomerService customerService = new CustomerServiceImpl();

    @FXML
    public void initialize() {
        customerIdField.setEditable(false);
        membershipCombo.setItems(FXCollections.observableArrayList(MembershipLevel.values()));
        membershipCombo.setValue(MembershipLevel.REGULAR);
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
        if (customer != null) {
            customerIdField.setText(customer.getCustomerId());
            nameField.setText(customer.getName());
            nicOrPassportField.setText(customer.getNicOrPassport());
            contactNoField.setText(customer.getContactNo());
            emailField.setText(customer.getEmail());
            addressField.setText(customer.getAddress());
            membershipCombo.setValue(customer.getMembership());
        } else {
            // Generate new customer ID for add mode
            customerIdField.setText(customerService.generateCustomerId());
        }
    }

    public Customer getCustomer() {
        return customer;
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    private void onOk() {
        if (!validate()) return;
        if (customer == null) customer = new Customer();
        customer.setCustomerId(customerIdField.getText().trim());
        customer.setName(nameField.getText().trim());
        customer.setNicOrPassport(nicOrPassportField.getText().trim());
        customer.setContactNo(contactNoField.getText().trim());
        customer.setEmail(emailField.getText().trim());
        customer.setAddress(addressField.getText().trim());
        customer.setMembership(membershipCombo.getValue());
        saved = true;
        if (dialogStage != null) dialogStage.close();
    }

    @FXML
    private void onCancel() {
        saved = false;
        if (dialogStage != null) dialogStage.close();
    }

    private boolean validate() {
        if (nameField.getText().trim().isEmpty() || nicOrPassportField.getText().trim().isEmpty()) {
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("Validation");
            a.setHeaderText("Required Fields");
            a.setContentText("Name and NIC/Passport are required.");
            a.showAndWait();
            return false;
        }
        if (contactNoField.getText().trim().isEmpty()) {
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("Validation");
            a.setHeaderText("Required Fields");
            a.setContentText("Contact Number is required.");
            a.showAndWait();
            return false;
        }
        return true;
    }
}
