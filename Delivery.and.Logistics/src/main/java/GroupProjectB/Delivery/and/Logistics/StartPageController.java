package GroupProjectB.Delivery.and.Logistics;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class StartPageController extends Application {

    // ── JavaFX Entry Point ────────────────────────────────────
    // This is now your app's MAIN class — run this to launch
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("StartPage.fxml"));
        Scene scene = new Scene(root, 800, 500);
        primaryStage.setTitle("Delivery & Logistics");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    // ── Driver card clicked ───────────────────────────────────
    @FXML
    private void handleDriver(javafx.scene.input.MouseEvent event) {
        loadPage("/DriverLogin.fxml", 800, 500, event);
    }

    // ── Admin card clicked ────────────────────────────────────
    @FXML
    private void handleAdmin(javafx.scene.input.MouseEvent event) {
        loadPage("/AdminLogin.fxml", 800, 500, event);
    }

    // ── Navigate to a page ────────────────────────────────────
    private void loadPage(String fxmlFile, double w, double h,
                          javafx.scene.input.MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            Stage stage = (Stage) ((VBox) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, w, h));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText(null);
            alert.setContentText("Could not load: " + fxmlFile);
            alert.showAndWait();
        }
    }
}
