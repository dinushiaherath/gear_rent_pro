package lk.ijse.gear_rent_pro.controller;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import lk.ijse.gear_rent_pro.util.SessionContext;

public class SideMenuAdminController {

    @FXML
    private Button btnLogout;
    
    private AdminDashboardController parentController;
    
    public void setParentController(AdminDashboardController controller) {
        this.parentController = controller;
    }

    @FXML
    public void loadBranches() {
        loadView("branches/view.fxml");
    }

    @FXML
    public void loadCategories() {
        loadView("categories/view.fxml");
    }

    @FXML
    public void loadMembership() {
        loadView("membership-config/view.fxml");
    }

    @FXML
    public void loadUsers() {
        loadView("users/view.fxml");
    }

    @FXML
    public void loadBranchRevenue() {
        loadView("reports/branch-revenue-view.fxml");
    }

    @FXML
    public void loadEquipmentUtilization() {
        loadView("reports/equipment-utilization-view.fxml");
    }

    @FXML
    public void loadEquipments() {
        loadView("equipments/view.fxml");
    }

    @FXML
    public void loadReservations() {
        loadView("reservations/view.fxml");
    }

    @FXML
    public void loadRentals() {
        loadView("rentals/view.fxml");
    }

    @FXML
    public void logout(ActionEvent event) {
        try {
            // Clear session
            SessionContext.getInstance().logout();

            // Get stage from the event source
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();

            // Load login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lk/ijse/ui/login/view.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("GearRent Pro - Login");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Logout Failed", "Failed to logout: " + e.getMessage());
        }
    }

    private void loadView(String fxmlFile) {
        if (parentController != null) {
            parentController.loadView(fxmlFile);
        }
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
