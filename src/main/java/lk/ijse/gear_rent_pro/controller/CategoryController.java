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
import lk.ijse.gear_rent_pro.model.Category;
import lk.ijse.gear_rent_pro.service.CategoryService;
import lk.ijse.gear_rent_pro.service.impl.CategoryServiceImpl;

public class CategoryController {
    
    @FXML
    private TableView<Category> categoryTable;
    
    private final CategoryService categoryService = new CategoryServiceImpl();
    
    @FXML
    public void initialize() {
        refreshCategoryTable();
    }
    
    @FXML
    public void showAddCategoryForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lk/ijse/ui/categories/form.fxml"));
            Parent root = loader.load();
            CategoryFormController ctrl = loader.getController();
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Add Category");
            dialog.setScene(new javafx.scene.Scene(root));
            ctrl.setDialogStage(dialog);
            dialog.showAndWait();

            if (ctrl.isSaved()) {
                Category c = ctrl.getCategory();
                categoryService.create(c);
                refreshCategoryTable();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Add Category", e.getMessage());
        }
    }
    
    @FXML
    public void editCategory() {
        Category selected = categoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Validation", "No selection", "Please select a category to edit.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lk/ijse/ui/categories/form.fxml"));
            Parent root = loader.load();
            CategoryFormController ctrl = loader.getController();
            ctrl.setCategory(selected);
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Edit Category");
            dialog.setScene(new javafx.scene.Scene(root));
            ctrl.setDialogStage(dialog);
            dialog.showAndWait();

            if (ctrl.isSaved()) {
                Category c = ctrl.getCategory();
                categoryService.update(c);
                refreshCategoryTable();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Edit Category", e.getMessage());
        }
    }
    
    @FXML
    public void deleteCategory() {
        Category selected = categoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Validation", "No selection", "Please select a category to delete.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete category " + selected.getName() + "?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Delete");
        confirm.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.YES) {
                try {
                    boolean ok = categoryService.delete(selected.getId());
                    if (ok) refreshCategoryTable();
                    else showAlert("Error", "Delete Failed", "Failed to delete category.");
                } catch (RuntimeException e) {
                    if (e.getCause() != null && e.getCause().toString().contains("SQLIntegrityConstraintViolationException")) {
                        showAlert("Cannot Delete", "Category In Use", "Cannot delete category because it has associated equipment or other related records.");
                    } else {
                        showAlert("Error", "Delete Failed", e.getMessage());
                    }
                }
            }
        });
    }
    
    @FXML
    public void refreshCategoryTable() {
        try {
            ObservableList<Category> data = FXCollections.observableArrayList(categoryService.findAll());
            categoryTable.setItems(data);
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
