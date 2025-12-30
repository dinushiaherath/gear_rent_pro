package lk.ijse.gear_rent_pro.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lk.ijse.gear_rent_pro.model.Branch;
import lk.ijse.gear_rent_pro.model.Category;
import lk.ijse.gear_rent_pro.model.Equipment;
import lk.ijse.gear_rent_pro.model.EquipmentStatus;
import lk.ijse.gear_rent_pro.model.UserRole;
import lk.ijse.gear_rent_pro.service.BranchService;
import lk.ijse.gear_rent_pro.service.CategoryService;
import lk.ijse.gear_rent_pro.service.impl.BranchServiceImpl;
import lk.ijse.gear_rent_pro.service.impl.CategoryServiceImpl;
import lk.ijse.gear_rent_pro.service.impl.EquipmentServiceImpl;
import lk.ijse.gear_rent_pro.util.SessionContext;

public class EquipmentFormController {

    @FXML
    private TextField equipmentIdField;
    @FXML
    private ComboBox<Category> categoryCombo;
    @FXML
    private TextField brandField;
    @FXML
    private TextField modelField;
    @FXML
    private TextField yearField;
    @FXML
    private TextField dailyPriceField;
    @FXML
    private TextField depositField;
    @FXML
    private ComboBox<EquipmentStatus> statusCombo;
    @FXML
    private ComboBox<Branch> branchCombo;
    @FXML
    private javafx.scene.control.Label branchLabel;

    private Stage dialogStage;
    private boolean saved = false;
    private Equipment equipment;

    private final CategoryService categoryService = new CategoryServiceImpl();
    private final BranchService branchService = new BranchServiceImpl();

    @FXML
    public void initialize() {
        statusCombo.setItems(FXCollections.observableArrayList(EquipmentStatus.values()));
        try {
            categoryCombo.setItems(FXCollections.observableArrayList(categoryService.findAll()));
            branchCombo.setItems(FXCollections.observableArrayList(branchService.findAll()));
        } catch (Exception e) {
            System.err.println("Failed to load categories/branches: " + e.getMessage());
        }

        categoryCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (equipment == null && newVal != null) {
                String generatedId = new EquipmentServiceImpl().generateEquipmentId(newVal.getId());
                equipmentIdField.setText(generatedId);
            }
        });

        var user = SessionContext.getInstance().getCurrentUser();
        boolean isAdmin = user != null && user.getRole() == UserRole.ADMIN;
        branchCombo.setVisible(isAdmin);
        branchCombo.setManaged(isAdmin);
        branchLabel.setVisible(isAdmin);
        branchLabel.setManaged(isAdmin);
    }

    public void setDialogStage(Stage dialogStage) { this.dialogStage = dialogStage; }

    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
        if (equipment != null) {
            equipmentIdField.setText(equipment.getEquipmentId());
            brandField.setText(equipment.getBrand());
            modelField.setText(equipment.getModel());
            yearField.setText(String.valueOf(equipment.getPurchaseYear()));
            dailyPriceField.setText(String.valueOf(equipment.getBaseDailyPrice()));
            depositField.setText(String.valueOf(equipment.getSecurityDeposit()));
            statusCombo.setValue(equipment.getStatus());
            // select category
            try { categoryCombo.getItems().stream().filter(c -> c.getId() == equipment.getCategoryId()).findFirst().ifPresent(categoryCombo::setValue); } catch (Exception ignored) {}
            try { branchCombo.getItems().stream().filter(b -> b.getId() == equipment.getBranchId()).findFirst().ifPresent(branchCombo::setValue); } catch (Exception ignored) {}
        }
    }

    public Equipment getEquipment() { return equipment; }

    public boolean isSaved() { return saved; }

    @FXML
    private void onOk() {
        if (!validate()) return;
        if (equipment == null) equipment = new Equipment();
        equipment.setEquipmentId(equipmentIdField.getText().trim());
        Category c = categoryCombo.getValue();
        if (c != null) equipment.setCategoryId(c.getId());
        equipment.setBrand(brandField.getText().trim());
        equipment.setModel(modelField.getText().trim());
        equipment.setPurchaseYear(Integer.parseInt(yearField.getText().trim()));
        equipment.setBaseDailyPrice(Double.parseDouble(dailyPriceField.getText().trim()));
        equipment.setSecurityDeposit(Double.parseDouble(depositField.getText().trim()));
        equipment.setStatus(statusCombo.getValue() != null ? statusCombo.getValue() : EquipmentStatus.AVAILABLE);

        var user = SessionContext.getInstance().getCurrentUser();
        if (user != null && user.getRole() != UserRole.ADMIN) {
            // force branch to user's branch
            if (user.getBranchId() != null) equipment.setBranchId(user.getBranchId());
        } else {
            if (branchCombo.getValue() != null) equipment.setBranchId(branchCombo.getValue().getId());
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
        if (equipmentIdField.getText().trim().isEmpty() || brandField.getText().trim().isEmpty()) {
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("Validation");
            a.setHeaderText("Required fields");
            a.setContentText("Equipment ID and Brand are required.");
            a.showAndWait();
            return false;
        }
        try { Integer.parseInt(yearField.getText().trim()); } catch (Exception e) { showInvalid("Purchase Year"); return false; }
        try { Double.parseDouble(dailyPriceField.getText().trim()); } catch (Exception e) { showInvalid("Daily Price"); return false; }
        try { Double.parseDouble(depositField.getText().trim()); } catch (Exception e) { showInvalid("Security Deposit"); return false; }
        return true;
    }

    private void showInvalid(String field) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Validation");
        a.setHeaderText("Invalid value");
        a.setContentText(field + " is invalid.");
        a.showAndWait();
    }
}
