package lk.ijse;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppInitializer extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent loginView = FXMLLoader.load(getClass().getResource("/lk/ijse/login-view.fxml"));
        stage.setTitle("Gear Rent Pro");
        Scene scene = new Scene(loginView);
        scene.getStylesheets().add(getClass().getResource("/lk/ijse/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
