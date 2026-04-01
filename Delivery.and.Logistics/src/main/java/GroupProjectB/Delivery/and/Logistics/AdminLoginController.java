package GroupProjectB.Delivery.and.Logistics;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminLoginController extends Application {

    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;

    private static final String DB_URL = "jdbc:sqlite:DRIVERS.db";

    // ── JavaFX entry point ────────────────────────────────────
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/AdminLogin.fxml"));
        Scene scene = new Scene(root, 800, 500);
        primaryStage.setTitle("Admin Login");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    // ── Handle Login ──────────────────────────────────────────
    @FXML
    private void handleLogin() throws ClassNotFoundException {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(AlertType.WARNING, "Missing Fields",
                "Please enter both username and password.");
            return;
        }

        boolean success = checkAdminCredentials(username, password);

        if (success) {
            loadPage("AdminDashboard.fxml", username, 1000, 580);
        } else {
            showAlert(AlertType.ERROR, "Login Failed",
                "Invalid username or password.");
            passwordField.clear();
        }
    }

    @FXML
    private void handleBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/StartPage.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 500));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Handle Forgot Password ────────────────────────────────
    @FXML
    private void handleForgotPassword() {
        showAlert(AlertType.INFORMATION, "Forgot Password",
            "Please contact your system administrator to reset your password.");
    }

    // ── SQL: verify admin credentials ─────────────────────────
    private boolean checkAdminCredentials(String username, String password) throws ClassNotFoundException {
        String sql = "SELECT * FROM Admins WHERE username = ? AND password = ?";

        System.out.println("Connecting ...:)");

        try {
            Class.forName("org.sqlite.JDBC");

            // ✅ FIXED: try-with-resources closes Connection,
            //           PreparedStatement and ResultSet automatically
            try (Connection c = DriverManager.getConnection(DB_URL);
                 PreparedStatement stmt = c.prepareStatement(sql)) {

                stmt.setString(1, username);
                stmt.setString(2, password);

                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }

            } // ← everything closed here, DB lock released before dashboard loads

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Database Error",
                "Could not connect to the database.");
            return false;
        }
    }

    // ── Navigate to Admin Dashboard ───────────────────────────
    private void loadPage(String fxmlFile, String adminName, double w, double h) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminDashboard.fxml"));
            Parent root = loader.load();

            AdminDashboardController controller = loader.getController();
            controller.setAdminName(adminName);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root, w, h));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Navigation Error",
                "Could not load: " + fxmlFile);
        }
    }

    // ── Helper ────────────────────────────────────────────────
    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}