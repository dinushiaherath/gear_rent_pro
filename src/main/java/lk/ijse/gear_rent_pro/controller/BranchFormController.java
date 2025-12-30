package lk.ijse.gear_rent_pro.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lk.ijse.gear_rent_pro.model.Branch;

public class BranchFormController {

    @FXML
    private TextField codeField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField addressField;
    @FXML
    private TextField contactField;

    private Stage dialogStage;
    private boolean saved = false;
    private Branch branch;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
        if (branch != null) {
            codeField.setText(branch.getCode());
            nameField.setText(branch.getName());
            addressField.setText(branch.getAddress());
            contactField.setText(branch.getContact());
        }
    }

    public Branch getBranch() {
        return branch;
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    private void onOk() {
        if (!validate()) return;
        if (branch == null) branch = new Branch();
        branch.setCode(codeField.getText().trim());
        branch.setName(nameField.getText().trim());
        branch.setAddress(addressField.getText().trim());
        branch.setContact(contactField.getText().trim());
        saved = true;
        if (dialogStage != null) dialogStage.close();
    }

    @FXML
    private void onCancel() {
        saved = false;
        if (dialogStage != null) dialogStage.close();
    }

    private boolean validate() {
        if (codeField.getText().trim().isEmpty() || nameField.getText().trim().isEmpty()) {
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("Validation");
            a.setHeaderText("Required fields");
            a.setContentText("Code and Name are required.");
            a.showAndWait();
            return false;
        }
        return true;
    }
}
