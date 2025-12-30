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

public class SideMenuStaffController {

    @FXML
    private Button btnLogout;
    @FXML
    private Button btnEquipment;
    @FXML
    private Button btnCustomers;
    @FXML
    private Button btnNewReservation;
    @FXML
    private Button btnNewRental;
    @FXML
    private Button btnOverdue;
    
    private StaffDashboardController parentController;
    
    public void setParentController(StaffDashboardController controller) {
        this.parentController = controller;
    }

    @FXML
    public void loadReservation() {
        setActive(btnNewReservation);
        loadView("reservations/view.fxml");
    }

    @FXML
    public void loadRental() {
        setActive(btnNewRental);
        loadView("rentals/view.fxml");
    }

    @FXML
    public void loadEquipment() {
        setActive(btnEquipment);
        loadView("equipments/view.fxml");
    }

    @FXML
    public void loadCustomers() {
        setActive(btnCustomers);
        loadView("customers/view.fxml");
    }

    @FXML
    public void loadActiveRentals() {
        loadView("rentals/view.fxml");
    }

    @FXML
    public void loadOverdue() {
        setActive(btnOverdue);
        loadView("overdue/view.fxml");
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

    /**
     * Visually mark the clicked button as active by applying an inline style
     * (border on the left and slightly reduced opacity) and clearing it from others.
     */
    private void setActive(Button clicked) {
        String activeStyle = "-fx-opacity: 0.85; -fx-border-color: rgba(255,255,255,0.12); -fx-border-width: 0 0 0 4;";
        Button[] buttons = new Button[] { btnEquipment, btnCustomers, btnNewReservation, btnNewRental, btnOverdue };
        for (Button b : buttons) {
            if (b == null) continue;
            if (b.equals(clicked)) {
                b.setStyle(activeStyle);
            } else {
                b.setStyle("");
            }
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
