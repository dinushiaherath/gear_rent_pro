package lk.ijse.gear_rent_pro.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lk.ijse.gear_rent_pro.model.Category;

public class CategoryFormController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField descriptionField;
    @FXML
    private TextField basePriceFactorField;
    @FXML
    private TextField weekendMultiplierField;
    @FXML
    private TextField lateFeePerDayField;

    private Stage dialogStage;
    private boolean saved = false;
    private Category category;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setCategory(Category category) {
        this.category = category;
        if (category != null) {
            nameField.setText(category.getName());
            descriptionField.setText(category.getDescription());
            basePriceFactorField.setText(String.valueOf(category.getBasePriceFactor()));
            weekendMultiplierField.setText(String.valueOf(category.getWeekendMultiplier()));
            lateFeePerDayField.setText(String.valueOf(category.getDefaultLateFeePerDay()));
        }
    }

    public Category getCategory() {
        return category;
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    private void onOk() {
        if (!validate()) return;
        if (category == null) category = new Category();
        category.setName(nameField.getText().trim());
        category.setDescription(descriptionField.getText().trim());
        try {
            category.setBasePriceFactor(Double.parseDouble(basePriceFactorField.getText().trim()));
            category.setWeekendMultiplier(Double.parseDouble(weekendMultiplierField.getText().trim()));
            category.setDefaultLateFeePerDay(Double.parseDouble(lateFeePerDayField.getText().trim()));
        } catch (NumberFormatException e) {
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("Validation");
            a.setHeaderText("Invalid Input");
            a.setContentText("Price factors and fees must be valid numbers.");
            a.showAndWait();
            return;
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
        if (nameField.getText().trim().isEmpty()) {
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("Validation");
            a.setHeaderText("Required Field");
            a.setContentText("Category name is required.");
            a.showAndWait();
            return false;
        }
        return true;
    }
}
