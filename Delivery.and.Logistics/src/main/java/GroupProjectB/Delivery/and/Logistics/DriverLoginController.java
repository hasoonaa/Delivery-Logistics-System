package GroupProjectB.Delivery.and.Logistics;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class DriverLoginController {

    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;

    // ──────────────────────────────────────────
    // HANDLE SIGN IN BUTTON
    // ──────────────────────────────────────────
    @FXML
    private void handleSignIn() throws ClassNotFoundException {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(AlertType.WARNING, "Missing Fields",
                "Please enter both username and password.");
            return;
        }

        boolean success = checkDriverCredentials(username, password);

        if (success) {
            loadPage("DriverDashboard.fxml"); // ← replace with your actual driver dashboard fxml name
        } else {
            showAlert(AlertType.ERROR, "Login Failed",
                "Invalid username or password.");
            passwordField.clear();
        }
    }

    // ──────────────────────────────────────────
    // HANDLE FORGOT PASSWORD LINK
    // ──────────────────────────────────────────
    @FXML
    private void handleForgotPassword() {
        showAlert(AlertType.INFORMATION, "Forgot Password",
            "Please contact your administrator to reset your password.");
    }

    // ──────────────────────────────────────────
    // SQL: Check driver credentials
    // ──────────────────────────────────────────
    private boolean checkDriverCredentials(String username, String password) throws ClassNotFoundException {
        // TODO: Replace the SQL below with your actual table/column names
        // This assumes your table is called 'drivers' with columns 'username' and 'password'
        String sql = "SELECT * FROM Drivers WHERE username = ? AND password = ?";

        try {
        	
    		System.out.println("Connecting ...:)");
    		
    		Connection c = null;
    		
    		Class.forName("org.sqlite.JDBC");
    		 
    		c = DriverManager.getConnection("JDBC:sqlite:DRIVERS.db");
        	
            PreparedStatement stmt = c.prepareStatement(sql);

            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // true if a matching row was found

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Database Error",
                "Could not connect to the database.");
            return false;
        }
    }

    // ──────────────────────────────────────────
    // NAVIGATE to another FXML page
    // ──────────────────────────────────────────
    private void loadPage(String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/DriverDashboard.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root, 640, 400));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Navigation Error",
                "Could not load page: " + fxmlFile);
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

    // ──────────────────────────────────────────
    // HELPER: Show Alert Dialog
    // ──────────────────────────────────────────
    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
