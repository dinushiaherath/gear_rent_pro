package lk.ijse.gear_rent_pro.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lk.ijse.gear_rent_pro.model.Branch;
import lk.ijse.gear_rent_pro.service.BranchService;
import lk.ijse.gear_rent_pro.service.impl.BranchServiceImpl;

public class BranchController {
    
    @FXML
    private TableView<Branch> branchTable;
    
    private final BranchService branchService = new BranchServiceImpl();
    
    @FXML
    public void initialize() {
        refreshBranchTable();
    }
    
    @FXML
    public void showAddBranchForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lk/ijse/ui/branches/form.fxml"));
            Parent root = loader.load();
            BranchFormController ctrl = loader.getController();
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Add Branch");
            dialog.setScene(new javafx.scene.Scene(root));
            ctrl.setDialogStage(dialog);
            dialog.showAndWait();

            if (ctrl.isSaved()) {
                Branch b = ctrl.getBranch();
                branchService.create(b);
                refreshBranchTable();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Add Branch", e.getMessage());
        }
    }
    
    @FXML
    public void editBranch() {
        Branch selected = branchTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Validation", "No selection", "Please select a branch to edit.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lk/ijse/ui/branches/form.fxml"));
            Parent root = loader.load();
            BranchFormController ctrl = loader.getController();
            ctrl.setBranch(selected);
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Edit Branch");
            dialog.setScene(new javafx.scene.Scene(root));
            ctrl.setDialogStage(dialog);
            dialog.showAndWait();

            if (ctrl.isSaved()) {
                Branch b = ctrl.getBranch();
                branchService.update(b);
                refreshBranchTable();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Edit Branch", e.getMessage());
        }
    }
    
    @FXML
    public void deleteBranch() {
        Branch selected = branchTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Validation", "No selection", "Please select a branch to delete.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete branch " + selected.getName() + "?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Delete");
        confirm.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.YES) {
                try {
                    boolean ok = branchService.delete(selected.getId());
                    if (ok) refreshBranchTable();
                    else showAlert("Error", "Delete Failed", "Failed to delete branch.");
                } catch (RuntimeException e) {
                    if (e.getCause() != null && e.getCause().toString().contains("SQLIntegrityConstraintViolationException")) {
                        showAlert("Cannot Delete", "Branch In Use", "Cannot delete branch because it has associated users or other related records.");
                    } else {
                        showAlert("Error", "Delete Failed", e.getMessage());
                    }
                }
            }
        });
    }
    
    @FXML
    public void refreshBranchTable() {
        try {
            ObservableList<Branch> data = FXCollections.observableArrayList(branchService.findAll());
            branchTable.setItems(data);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Refresh", e.getMessage());
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
