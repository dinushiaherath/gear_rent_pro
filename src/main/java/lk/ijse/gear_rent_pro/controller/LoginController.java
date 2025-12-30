package lk.ijse.gear_rent_pro.controller;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lk.ijse.gear_rent_pro.model.User;
import lk.ijse.gear_rent_pro.model.UserRole;
import lk.ijse.gear_rent_pro.service.UserService;
import lk.ijse.gear_rent_pro.service.impl.UserServiceImpl;
import lk.ijse.gear_rent_pro.util.PasswordUtil;
import lk.ijse.gear_rent_pro.util.SessionContext;

public class LoginController {

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Button btnLogin;

    private final UserService userService = new UserServiceImpl();

    @FXML
    public void initialize() {
        // Clear any existing session on login page load
        if (SessionContext.getInstance().getCurrentUser() != null) {
            txtUsername.clear();
            txtPassword.clear();
        }
    }

    @FXML
    private void onLogin(ActionEvent event) {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Validation", "Please enter username and password");
            return;
        }

        try {
            User user = userService.findByUsername(username);
            if (user == null) {
                showError("Login Failed", "Invalid username or password");
                return;
            }

            if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
                showError("Login Failed", "Invalid username or password");
                return;
            }

            // Store user in session
            SessionContext.getInstance().setCurrentUser(user);

            // Load appropriate dashboard based on role
            String dashboardFile = getDashboardByRole(user.getRole());
            Parent dashboard = FXMLLoader.load(getClass().getResource("/lk/ijse/ui/" + dashboardFile));
            Scene scene = ((Node) event.getSource()).getScene();
            scene.setRoot(dashboard);
            Stage stage = (Stage) scene.getWindow();
            stage.setTitle(user.getRole() + " Dashboard - Gear Rent Pro");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error", "Failed to load dashboard");
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String getDashboardByRole(UserRole role) {
        switch (role) {
            case ADMIN:
                return "dashboard/admin.fxml";
            case BRANCH_MANAGER:
                return "dashboard/manager.fxml";
            case STAFF:
                return "dashboard/staff.fxml";
            default:
                showError("Error", "Invalid user role");
                throw new IllegalArgumentException("Unknown role: " + role);
        }
    }
}
