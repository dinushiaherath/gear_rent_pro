package lk.ijse.gear_rent_pro.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

public class BranchManagerDashboardController {

    @FXML
    private AnchorPane contentPane;

    @FXML
    private BorderPane borderPane;
    
    @FXML
    private AnchorPane sideMenuPane;

    private SideMenuManagerController sideMenuController;

    @FXML
    public void initialize() {
        // Load side menu first and set parent controller
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lk/ijse/ui/side-menu/manager.fxml"));
            Pane sideMenu = loader.load();
            sideMenuController = loader.getController();
            if (sideMenuController != null) {
                sideMenuController.setParentController(this);
            }

            // Add side menu to the left pane
            sideMenuPane.getChildren().add(sideMenu);
            AnchorPane.setTopAnchor(sideMenu, 0.0);
            AnchorPane.setBottomAnchor(sideMenu, 0.0);
            AnchorPane.setLeftAnchor(sideMenu, 0.0);
            AnchorPane.setRightAnchor(sideMenu, 0.0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Load default view
        loadView("equipments/view.fxml");
    }

    public void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lk/ijse/ui/" + fxmlFile));
            Pane view = loader.load();
            contentPane.getChildren().clear();
            contentPane.getChildren().add(view);

            // Set anchors to fill the pane
            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
