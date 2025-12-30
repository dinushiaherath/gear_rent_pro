package lk.ijse;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppInitializer extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent loginView = FXMLLoader.load(getClass().getResource("/lk/ijse/ui/login/view.fxml"));
        stage.setTitle("Gear Rent Pro");
        Scene scene = new Scene(loginView);
        try {
            String stylesheetUrl = getClass().getResource("/lk/ijse/styles/global.css").toExternalForm();
            scene.getStylesheets().add(stylesheetUrl);
        } catch (Exception e) {
            System.err.println("Warning: global.css not found: " + e.getMessage());
        }
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
