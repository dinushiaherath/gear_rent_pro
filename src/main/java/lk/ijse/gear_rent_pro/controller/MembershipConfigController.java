package lk.ijse.gear_rent_pro.controller;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import lk.ijse.gear_rent_pro.model.MembershipConfig;
import lk.ijse.gear_rent_pro.service.MembershipConfigService;
import lk.ijse.gear_rent_pro.service.impl.MembershipConfigServiceImpl;

public class MembershipConfigController {

    @FXML private TableView<MembershipConfig> table;
    @FXML private TableColumn<MembershipConfig, String> colLevel;
    @FXML private TableColumn<MembershipConfig, Double> colDiscount;

    private final MembershipConfigService service = new MembershipConfigServiceImpl();

    @FXML
    public void initialize() {
        colLevel.setCellValueFactory(data -> javafx.beans.property.SimpleStringProperty.stringExpression(javafx.beans.binding.Bindings.createStringBinding(() -> data.getValue().getLevel().toString())));

        colDiscount.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("discountPercent"));
        colDiscount.setCellFactory(tc -> new javafx.scene.control.cell.TextFieldTableCell<>(new javafx.util.converter.DoubleStringConverter()))
                ;
        colDiscount.setOnEditCommit(ev -> {
            MembershipConfig cfg = ev.getRowValue();
            cfg.setDiscountPercent(ev.getNewValue());
        });

        table.setEditable(true);

        loadConfigs();
    }

    private void loadConfigs() {
        try {
            List<MembershipConfig> configs = service.findAll();
            ObservableList<MembershipConfig> list = FXCollections.observableArrayList(configs);
            table.setItems(list);
        } catch (Exception e) {
            showError("Error", "Failed to load membership configs: " + e.getMessage());
        }
    }

    @FXML
    public void saveAll() {
        try {
            for (MembershipConfig cfg : table.getItems()) {
                service.saveOrUpdate(cfg);
            }
            showAlert("Saved", "Membership discounts saved successfully.", "");
        } catch (Exception e) {
            showError("Error", "Failed to save configs: " + e.getMessage());
        }
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
