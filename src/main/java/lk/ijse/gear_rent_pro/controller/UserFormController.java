package lk.ijse.gear_rent_pro.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lk.ijse.gear_rent_pro.model.Branch;
import lk.ijse.gear_rent_pro.model.User;
import lk.ijse.gear_rent_pro.model.UserRole;
import lk.ijse.gear_rent_pro.service.BranchService;
import lk.ijse.gear_rent_pro.service.impl.BranchServiceImpl;

public class UserFormController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private ComboBox<UserRole> roleCombo;
    @FXML
    private ComboBox<Branch> branchCombo;
    @FXML
    private Label branchLabel;

    private Stage dialogStage;
    private boolean saved = false;
    private User user;
    private BranchService branchService;

    @FXML
    public void initialize() {
        branchService = new BranchServiceImpl();
        roleCombo.setItems(FXCollections.observableArrayList(UserRole.values()));
        
        // Load branches into combo
        try {
            branchCombo.setItems(FXCollections.observableArrayList(branchService.findAll()));
        } catch (Exception e) {
            showAlert("Error", "Failed to load branches", e.getMessage());
        }
        
        // Listen to role changes to show/hide branch field
        roleCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isNotAdmin = newVal != UserRole.ADMIN;
            branchCombo.setVisible(isNotAdmin);
            branchCombo.setManaged(isNotAdmin);
            branchLabel.setVisible(isNotAdmin);
            branchLabel.setManaged(isNotAdmin);
        });
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            usernameField.setText(user.getUsername());
            usernameField.setEditable(false);
            roleCombo.setValue(user.getRole());
            
            // Set branch if not admin
            if (user.getRole() != UserRole.ADMIN && user.getBranchId() != null) {
                try {
                    Branch branch = branchService.findById(user.getBranchId());
                    if (branch != null) {
                        branchCombo.setValue(branch);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to load branch: " + e.getMessage());
                }
            }
            
            passwordField.setPromptText("Leave empty to keep current password");
            confirmPasswordField.setPromptText("Leave empty to keep current password");
        }
    }

    public User getUser() {
        return user;
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    private void onOk() {
        if (!validate()) return;
        if (user == null) user = new User();
        user.setUsername(usernameField.getText().trim());
        user.setRole(roleCombo.getValue());
        
        // Set branch only if not admin
        if (roleCombo.getValue() != UserRole.ADMIN && branchCombo.getValue() != null) {
            user.setBranchId(branchCombo.getValue().getId());
        } else {
            user.setBranchId(null);
        }
        
        // Only set password if provided
        if (!passwordField.getText().isEmpty()) {
            user.setPasswordHash(passwordField.getText());
        }
        
        saved = true;
        if (dialogStage != null) dialogStage.close();
    }

    @FXML
    private void onCancel() {
        saved = false;
        if (dialogStage != null) dialogStage.close();
    }

    private boolean validate() {
        if (usernameField.getText().trim().isEmpty()) {
            showAlert("Validation", "Required Field", "Username is required.");
            return false;
        }
        
        // For new users, password is required
        if (user == null && passwordField.getText().isEmpty()) {
            showAlert("Validation", "Required Field", "Password is required for new users.");
            return false;
        }
        
        // If password is filled, confirm must match
        if (!passwordField.getText().isEmpty() && !passwordField.getText().equals(confirmPasswordField.getText())) {
            showAlert("Validation", "Password Mismatch", "Passwords do not match.");
            return false;
        }
        
        if (roleCombo.getValue() == null) {
            showAlert("Validation", "Required Field", "Role is required.");
            return false;
        }
        
        // Branch is required for non-admin users
        if (roleCombo.getValue() != UserRole.ADMIN && branchCombo.getValue() == null) {
            showAlert("Validation", "Required Field", "Branch is required for non-admin users.");
            return false;
        }
        
        return true;
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
