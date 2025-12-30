package lk.ijse.gear_rent_pro.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

public class ReportController {
    
    @FXML
	private ComboBox<String> reportTypeCombo;
    @FXML
    private DatePicker startDatePicker;
    
    @FXML
    private DatePicker endDatePicker;
    
    @FXML
    private Label reportTitle;
    
    @FXML
    private TableView<?> reportTable;
    
    @FXML
    public void initialize() {
        initializeReportTypes();
    }
    
    private void initializeReportTypes() {
        ObservableList<String> reportTypes = FXCollections.observableArrayList(
            "Branch-wise Revenue",
            "Equipment Utilization",
            "Customer Activity",
            "Late Fees Report"
        );
        reportTypeCombo.setItems(reportTypes);
    }
    
    @FXML
    public void generateReport() {
        showAlert("Info", "Generate Report", "Report generation will be implemented soon.");
    }
    
    @FXML
    public void exportReport() {
        showAlert("Info", "Export Report", "Report export will be implemented soon.");
    }
    
    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
