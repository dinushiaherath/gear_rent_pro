package lk.ijse.gear_rent_pro.controller;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lk.ijse.gear_rent_pro.model.Branch;
import lk.ijse.gear_rent_pro.model.Category;
import lk.ijse.gear_rent_pro.model.Equipment;
import lk.ijse.gear_rent_pro.model.EquipmentStatus;
import lk.ijse.gear_rent_pro.model.UserRole;
import lk.ijse.gear_rent_pro.service.BranchService;
import lk.ijse.gear_rent_pro.service.CategoryService;
import lk.ijse.gear_rent_pro.service.EquipmentService;
import lk.ijse.gear_rent_pro.service.impl.BranchServiceImpl;
import lk.ijse.gear_rent_pro.service.impl.CategoryServiceImpl;
import lk.ijse.gear_rent_pro.service.impl.EquipmentServiceImpl;
import lk.ijse.gear_rent_pro.util.SessionContext;

public class EquipmentController {

    @FXML
    private TableView<Equipment> equipmentTable;
    @FXML
    private TableColumn<Equipment, String> equipmentIdCol;
    @FXML
    private TableColumn<Equipment, String> brandModelCol;
    @FXML
    private TableColumn<Equipment, Integer> categoryCol;
    @FXML
    private TableColumn<Equipment, Double> dailyPriceCol;
    @FXML
    private TableColumn<Equipment, EquipmentStatus> statusCol;
    @FXML
    private TableColumn<Equipment, Integer> branchCol;
    @FXML
    private javafx.scene.control.ComboBox<Branch> branchFilterCombo;
    @FXML
    private TextField brandModelSearchField;
    @FXML
    private ComboBox<Category> categoryFilterCombo;
    @FXML
    private ComboBox<EquipmentStatus> statusFilterCombo;

    private final EquipmentService equipmentService = new EquipmentServiceImpl();
    private final CategoryService categoryService = new CategoryServiceImpl();
    private final BranchService branchService = new BranchServiceImpl();

    @FXML
    public void initialize() {
        equipmentIdCol.setCellValueFactory(new PropertyValueFactory<>("equipmentId"));
        brandModelCol.setCellValueFactory(c -> {
            Equipment e = c.getValue();
            return new javafx.beans.property.ReadOnlyStringWrapper(e.getBrand() + " / " + e.getModel());
        });
        dailyPriceCol.setCellValueFactory(new PropertyValueFactory<>("baseDailyPrice"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        // category and branch columns will show friendly names
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("categoryId"));
        categoryCol.setCellFactory(column -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                    return;
                }
                try {
                    Category c = categoryService.findById(item);
                    setText(c != null ? c.getName() : "");
                } catch (Exception e) {
                    setText("");
                }
            }
        });

        branchCol.setCellValueFactory(new PropertyValueFactory<>("branchId"));
        branchCol.setCellFactory(column -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                    return;
                }
                try {
                    Branch b = branchService.findById(item);
                    setText(b != null ? b.getName() + " (" + b.getCode() + ")" : "");
                } catch (Exception e) {
                    setText("");
                }
            }
        });

        // Setup branch filter for admins
        var user = SessionContext.getInstance().getCurrentUser();
        try {
            if (user != null && user.getRole() == UserRole.ADMIN) {
                var branches = branchService.findAll();
                branchFilterCombo.setItems(FXCollections.observableArrayList(branches));
                branchFilterCombo.getItems().add(0, null);
                Supplier<javafx.scene.control.ListCell<Branch>> branchCellSupplier = () -> new javafx.scene.control.ListCell<Branch>() {
                    @Override
                    protected void updateItem(Branch item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? "All Branches" : item.getName());
                    }
                };
                branchFilterCombo.setCellFactory(param -> branchCellSupplier.get());
                branchFilterCombo.setButtonCell(branchCellSupplier.get());
                branchFilterCombo.valueProperty().addListener((obs, old, nw) -> refreshEquipmentTable());
            } else if (user != null && user.getBranchId() != null) {
                Branch b = branchService.findById(user.getBranchId());
                if (b != null) {
                    branchFilterCombo.setItems(FXCollections.observableArrayList(b));
                    branchFilterCombo.setValue(b);
                    branchFilterCombo.setDisable(true);
                }
            }
        } catch (Exception e) {
            // ignore branch load failures and continue
        }

        // Setup category filter
        try {
            var categories = categoryService.findAll();
            categoryFilterCombo.setItems(FXCollections.observableArrayList(categories));
            categoryFilterCombo.getItems().add(0, null);
            Supplier<javafx.scene.control.ListCell<Category>> categoryCellSupplier = () -> new javafx.scene.control.ListCell<Category>() {
                @Override
                protected void updateItem(Category item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "All Categories" : item.getName());
                }
            };
            categoryFilterCombo.setCellFactory(param -> categoryCellSupplier.get());
            categoryFilterCombo.setButtonCell(categoryCellSupplier.get());
            categoryFilterCombo.valueProperty().addListener((obs, old, nw) -> refreshEquipmentTable());
        } catch (Exception e) {
        }

        // Setup status filter
        statusFilterCombo.setItems(FXCollections.observableArrayList(EquipmentStatus.values()));
        statusFilterCombo.getItems().add(0, null);
        statusFilterCombo.setPromptText("All Statuses");
        statusFilterCombo.valueProperty().addListener((obs, old, nw) -> refreshEquipmentTable());

        // Setup brand/model search
        if (brandModelSearchField != null) {
            brandModelSearchField.textProperty().addListener((obs, old, nw) -> refreshEquipmentTable());
        }

        refreshEquipmentTable();
    }

    @FXML
    public void showAddEquipmentForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lk/ijse/ui/equipments/form.fxml"));
            Parent root = loader.load();
            EquipmentFormController ctrl = loader.getController();
            Stage dialog = new Stage();
            ctrl.setDialogStage(dialog);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));
            dialog.setTitle("Add Equipment");
            dialog.showAndWait();
            if (ctrl.isSaved()) {
                equipmentService.create(ctrl.getEquipment());
                refreshEquipmentTable();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open form", e.getMessage());
        }
    }

    @FXML
    public void editEquipment() {
        Equipment selected = equipmentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Validation", "No selection", "Please select equipment to edit.");
            return;
        }

        // ensure user can edit this equipment
        var user = SessionContext.getInstance().getCurrentUser();
        if (user != null && user.getRole() != UserRole.ADMIN && user.getBranchId() != null && user.getBranchId() != selected.getBranchId()) {
            showAlert("Permission Denied", "Cannot edit", "You can only edit equipment from your branch.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lk/ijse/ui/equipments/form.fxml"));
            Parent root = loader.load();
            EquipmentFormController ctrl = loader.getController();
            ctrl.setEquipment(selected);
            Stage dialog = new Stage();
            ctrl.setDialogStage(dialog);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));
            dialog.setTitle("Edit Equipment");
            dialog.showAndWait();
            if (ctrl.isSaved()) {
                equipmentService.update(ctrl.getEquipment());
                refreshEquipmentTable();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open form", e.getMessage());
        }
    }

    @FXML
    public void deleteEquipment() {
        Equipment selected = equipmentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Validation", "No selection", "Please select equipment to delete.");
            return;
        }

        var user = SessionContext.getInstance().getCurrentUser();
        if (user != null && user.getRole() != UserRole.ADMIN && user.getBranchId() != null && user.getBranchId() != selected.getBranchId()) {
            showAlert("Permission Denied", "Cannot delete", "You can only delete equipment from your branch.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete selected equipment?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm");
        confirm.showAndWait().ifPresent(b -> {
            if (b == ButtonType.YES) {
                try {
                    equipmentService.delete(selected.getId());
                    refreshEquipmentTable();
                } catch (Exception e) {
                    showAlert("Error", "Delete failed", e.getMessage());
                }
            }
        });
    }

    @FXML
    public void refreshEquipmentTable() {
        try {
            List<Equipment> all = equipmentService.findAll();

            // Filter by branch
            Branch sel = null;
            try {
                sel = branchFilterCombo != null ? branchFilterCombo.getValue() : null;
            } catch (Exception ignored) {
            }
            if (sel != null) {
                final int selId = sel.getId();
                all = all.stream().filter(e -> e.getBranchId() == selId).collect(Collectors.toList());
            }

            // Filter by category
            Category selectedCategory = null;
            try {
                selectedCategory = categoryFilterCombo != null ? categoryFilterCombo.getValue() : null;
            } catch (Exception ignored) {
            }
            if (selectedCategory != null) {
                final int catId = selectedCategory.getId();
                all = all.stream().filter(e -> e.getCategoryId() == catId).collect(Collectors.toList());
            }

            // Filter by status
            EquipmentStatus selectedStatus = null;
            try {
                selectedStatus = statusFilterCombo != null ? statusFilterCombo.getValue() : null;
            } catch (Exception ignored) {
            }
            if (selectedStatus != null) {
                final EquipmentStatus status = selectedStatus;
                all = all.stream().filter(e -> e.getStatus() == status).collect(Collectors.toList());
            }

            // Search by brand/model
            String searchText = null;
            try {
                searchText = brandModelSearchField != null ? brandModelSearchField.getText().trim() : "";
            } catch (Exception ignored) {
            }
            if (searchText != null && !searchText.isEmpty()) {
                final String search = searchText.toLowerCase();
                all = all.stream().filter(e
                        -> (e.getBrand() != null && e.getBrand().toLowerCase().contains(search))
                        || (e.getModel() != null && e.getModel().toLowerCase().contains(search))
                ).collect(Collectors.toList());
            }

            ObservableList<Equipment> data = FXCollections.observableArrayList(all);
            equipmentTable.setItems(data);
        } catch (Exception e) {
            showAlert("Error", "Failed to load equipment", e.getMessage());
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
