package lk.ijse.gear_rent_pro.controller;

import java.util.function.Supplier;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lk.ijse.gear_rent_pro.model.Branch;
import lk.ijse.gear_rent_pro.model.User;
import lk.ijse.gear_rent_pro.model.UserRole;
import lk.ijse.gear_rent_pro.service.BranchService;
import lk.ijse.gear_rent_pro.service.UserService;
import lk.ijse.gear_rent_pro.service.impl.BranchServiceImpl;
import lk.ijse.gear_rent_pro.service.impl.UserServiceImpl;
import lk.ijse.gear_rent_pro.util.SessionContext;

public class UserController {

    @FXML
    private TableView<User> userTable;

    private final UserService userService = new UserServiceImpl();
    private final BranchService branchService = new BranchServiceImpl();
    @FXML
    private TextField usernameSearchField;
    @FXML
    private ComboBox<UserRole> roleFilterCombo;
    @FXML
    private ComboBox<Branch> branchFilterCombo;

    @FXML
    public void initialize() {
        setupBranchColumn();
        // initialize filters
        try {
            roleFilterCombo.setItems(FXCollections.observableArrayList(UserRole.values()));
            roleFilterCombo.getItems().add(0, null);
            roleFilterCombo.setPromptText("All Roles");
            ObservableList<Branch> branches = FXCollections.observableArrayList(branchService.findAll());
            
            branchFilterCombo.setItems(branches);
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

            usernameSearchField.textProperty().addListener((obs, o, n) -> refreshUserTable());
            roleFilterCombo.valueProperty().addListener((obs, o, n) -> refreshUserTable());
            branchFilterCombo.valueProperty().addListener((obs, o, n) -> refreshUserTable());
        } catch (Exception e) {
            // ignore filter init errors
        }
        refreshUserTable();
    }

    private void setupBranchColumn() {
        // Find the Branch column and set custom cell factory
        for (TableColumn<User, ?> col : userTable.getColumns()) {
            if ("Branch".equals(col.getText())) {
                @SuppressWarnings("unchecked")
                TableColumn<User, Integer> branchCol = (TableColumn<User, Integer>) col;
                branchCol.setCellFactory(param -> new TextFieldTableCell<User, Integer>() {
                    @Override
                    public void updateItem(Integer branchId, boolean empty) {
                        super.updateItem(branchId, empty);
                        if (empty) {
                            setText("");
                        } else if (branchId == null) {
                            setText("");
                        } else {
                            try {
                                Branch branch = branchService.findById(branchId);
                                if (branch != null) {
                                    setText(branch.getName() + " (" + branch.getCode() + ")");
                                } else {
                                    setText("Unknown");
                                }
                            } catch (Exception e) {
                                setText("Error");
                            }
                        }
                    }
                });
                break;
            }
        }
    }

    @FXML
    public void showAddUserForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lk/ijse/ui/users/form.fxml"));
            Parent root = loader.load();
            UserFormController ctrl = loader.getController();
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Add User");
            dialog.setScene(new javafx.scene.Scene(root));
            ctrl.setDialogStage(dialog);
            dialog.showAndWait();

            if (ctrl.isSaved()) {
                User u = ctrl.getUser();
                userService.create(u);
                refreshUserTable();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Add User", e.getMessage());
        }
    }

    @FXML
    public void editUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Validation", "No selection", "Please select a user to edit.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lk/ijse/ui/users/form.fxml"));
            Parent root = loader.load();
            UserFormController ctrl = loader.getController();
            ctrl.setUser(selected);
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Edit User");
            dialog.setScene(new javafx.scene.Scene(root));
            ctrl.setDialogStage(dialog);
            dialog.showAndWait();

            if (ctrl.isSaved()) {
                User u = ctrl.getUser();
                userService.update(u);
                refreshUserTable();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Edit User", e.getMessage());
        }
    }

    @FXML
    public void deleteUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Validation", "No selection", "Please select a user to delete.");
            return;
        }

        // Prevent deletion of admins
        if (selected.getRole() == UserRole.ADMIN) {
            showAlert("Cannot Delete", "Admin User", "Admin users cannot be deleted.");
            return;
        }

        // Prevent deletion of current user
        User currentUser = SessionContext.getInstance().getCurrentUser();
        if (currentUser != null && selected.getId() == currentUser.getId()) {
            showAlert("Cannot Delete", "Current User", "You cannot delete yourself.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete user " + selected.getUsername() + "?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Delete");
        confirm.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.YES) {
                try {
                    boolean ok = userService.delete(selected.getId());
                    if (ok) {
                        refreshUserTable();
                    } else {
                        showAlert("Error", "Delete Failed", "Failed to delete user.");
                    }
                } catch (RuntimeException e) {
                    if (e.getCause() != null && e.getCause().toString().contains("SQLIntegrityConstraintViolationException")) {
                        showAlert("Cannot Delete", "User In Use", "Cannot delete user because it has associated records.");
                    } else {
                        showAlert("Error", "Delete Failed", e.getMessage());
                    }
                }
            }
        });
    }

    @FXML
    public void refreshUserTable() {
        try {
            ObservableList<User> data = FXCollections.observableArrayList(userService.findAll());
            String search = usernameSearchField != null ? usernameSearchField.getText() : null;
            UserRole selRole = roleFilterCombo != null ? roleFilterCombo.getValue() : null;
            Branch selBranch = branchFilterCombo != null ? branchFilterCombo.getValue() : null;
            ObservableList<User> filtered = data.filtered(u -> {
                boolean ok = true;
                if (search != null && !search.isBlank()) {
                    ok = u.getUsername() != null && u.getUsername().toLowerCase().contains(search.toLowerCase());
                }
                if (ok && selRole != null) {
                    ok = selRole.equals(u.getRole());
                }
                if (ok && selBranch != null) {
                    ok = u.getBranchId() != null && selBranch.getId() == u.getBranchId();
                }
                return ok;
            });
            userTable.setItems(filtered);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Refresh", e.getMessage());
        }
    }

    @FXML
    public void clearFilters() {
        if (usernameSearchField != null) {
            usernameSearchField.clear();
        }
        if (roleFilterCombo != null) {
            roleFilterCombo.getSelectionModel().clearSelection();
        }
        if (branchFilterCombo != null) {
            branchFilterCombo.getSelectionModel().clearSelection();
        }
        refreshUserTable();
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
