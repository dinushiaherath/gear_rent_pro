package lk.ijse.gear_rent_pro.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

public class AdminDashboardController {
    
    @FXML
    private AnchorPane contentPane;
    
    @FXML
    private AnchorPane sideMenuPane;
    
    @FXML
    private BorderPane borderPane;
    
    @FXML
    public void initialize() {
        // Load side menu first and set parent controller
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lk/ijse/ui/side-menu/admin.fxml"));
            Parent sideMenu = loader.load();
            SideMenuAdminController sideMenuController = loader.getController();
            
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
        loadView("branches/view.fxml");
    }
    
    public void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lk/ijse/ui/" + fxmlFile));
            Parent view = loader.load();
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
