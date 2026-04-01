package GroupProjectB.Delivery.and.Logistics;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AdminSettingsController implements Initializable {

    // ── Sidebar ───────────────────────────────────────────────
    @FXML private Label welcomeLabel;

    // ── Account Settings ──────────────────────────────────────
    @FXML private TextField     usernameField;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;

    // ── Delete Driver ─────────────────────────────────────────
    @FXML private ComboBox<String> driverDeleteSelector;

    // ── DB ────────────────────────────────────────────────────
    private static final String DB_URL = "jdbc:sqlite:DRIVERS.db";

    // ── Logged-in admin username ──────────────────────────────
    private String currentAdminUsername = "";

    // ═════════════════════════════════════════════════════════
    // INIT
    // ═════════════════════════════════════════════════════════
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadDriversIntoSelector();
    }

    public void setAdminName(String name) {
        welcomeLabel.setText("Welcome back " + name + "!");
        currentAdminUsername = name;
        usernameField.setPromptText("Current: " + name);
    }

    private Connection getConnection() throws Exception {
        Class.forName("org.sqlite.JDBC");
        return DriverManager.getConnection(DB_URL);
    }

    // ═════════════════════════════════════════════════════════
    // LOAD DRIVERS INTO SELECTOR
    // ═════════════════════════════════════════════════════════
    private void loadDriversIntoSelector() {
        List<String> drivers = new ArrayList<>();
        try (Connection c = getConnection()) {
            ResultSet rs = c.createStatement().executeQuery(
                "SELECT username FROM drivers");
            while (rs.next()) {
                drivers.add(rs.getString("username"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            drivers = List.of(
                "Ahmed - DRV001", "Sara - DRV002",
                "Omar - DRV003",  "Layla - DRV004",
                "Tariq - DRV005", "Nadia - DRV006",
                "Yusuf - DRV007"
            );
        }
        driverDeleteSelector.setItems(FXCollections.observableArrayList(drivers));
        if (!drivers.isEmpty()) driverDeleteSelector.setValue(drivers.get(0));
    }

    // ═════════════════════════════════════════════════════════
    // CHANGE USERNAME
    // ═════════════════════════════════════════════════════════
    @FXML
    private void handleChangeUsername() {
        String newUsername = usernameField.getText().trim();

        if (newUsername.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Empty Field",
                "Please enter a new username.");
            return;
        }

        try (Connection c = getConnection()) {
            PreparedStatement ps = c.prepareStatement(
                "UPDATE admins SET username = ? WHERE username = ?");
            ps.setString(1, newUsername);
            ps.setString(2, currentAdminUsername);
            int rows = ps.executeUpdate();

            if (rows > 0) {
                currentAdminUsername = newUsername;
                welcomeLabel.setText("Welcome back " + newUsername + "!");
                usernameField.clear();
                usernameField.setPromptText("Current: " + newUsername);
                showAlert(Alert.AlertType.INFORMATION, "Username Updated",
                    "Your username has been changed to: " + newUsername);
            } else {
                showAlert(Alert.AlertType.ERROR, "Update Failed",
                    "Could not update username. Admin not found in DB.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "DB Error",
                "Could not connect to database.");
        }
    }

    // ═════════════════════════════════════════════════════════
    // CHANGE PASSWORD
    // ═════════════════════════════════════════════════════════
    @FXML
    private void handleChangePassword() {
        String currentPass = currentPasswordField.getText().trim();
        String newPass     = newPasswordField.getText().trim();

        if (currentPass.isEmpty() || newPass.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Empty Fields",
                "Please fill in both password fields.");
            return;
        }

        if (newPass.length() < 6) {
            showAlert(Alert.AlertType.WARNING, "Weak Password",
                "New password must be at least 6 characters.");
            return;
        }

        try (Connection c = getConnection()) {
            PreparedStatement check = c.prepareStatement(
                "SELECT * FROM admins WHERE username = ? AND password = ?");
            check.setString(1, currentAdminUsername);
            check.setString(2, currentPass);
            ResultSet rs = check.executeQuery();

            if (!rs.next()) {
                showAlert(Alert.AlertType.ERROR, "Incorrect Password",
                    "Your current password is incorrect.");
                currentPasswordField.clear();
                return;
            }

            PreparedStatement update = c.prepareStatement(
                "UPDATE admins SET password = ? WHERE username = ?");
            update.setString(1, newPass);
            update.setString(2, currentAdminUsername);
            update.executeUpdate();

            currentPasswordField.clear();
            newPasswordField.clear();
            showAlert(Alert.AlertType.INFORMATION, "Password Updated",
                "Your password has been changed successfully.");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "DB Error",
                "Could not connect to database.");
        }
    }

    // ═════════════════════════════════════════════════════════
    // DELETE DRIVER
    // ═════════════════════════════════════════════════════════
    @FXML
    private void handleDeleteDriver() {
        String selected = driverDeleteSelector.getValue();

        if (selected == null || selected.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Driver Selected",
                "Please select a driver to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Driver");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to permanently delete driver: "
            + selected + "?\n\nThis cannot be undone.");
        confirm.initOwner((Stage) welcomeLabel.getScene().getWindow());

        confirm.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) return;

            try (Connection c = getConnection()) {
                PreparedStatement ps = c.prepareStatement(
                    "DELETE FROM drivers WHERE username = ?");
                ps.setString(1, selected);
                int rows = ps.executeUpdate();

                if (rows > 0) {
                    driverDeleteSelector.getItems().remove(selected);
                    if (!driverDeleteSelector.getItems().isEmpty()) {
                        driverDeleteSelector.setValue(
                            driverDeleteSelector.getItems().get(0));
                    }
                    showAlert(Alert.AlertType.INFORMATION, "Driver Deleted",
                        "Driver " + selected + " has been removed.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Not Found",
                        "Driver not found in database.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "DB Error",
                    "Could not delete driver.");
            }
        });
    }

    // ═════════════════════════════════════════════════════════
    // SIDEBAR NAV
    // ═════════════════════════════════════════════════════════
    @FXML
    private void handleDeliveryMap() {
        handleBack();
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/AdminDashboard.fxml"));
            Parent root = loader.load();
            AdminDashboardController controller = loader.getController();
            controller.setAdminName(currentAdminUsername);
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 580));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                "Could not return to dashboard.");
        }
    }

    // ═════════════════════════════════════════════════════════
    // HELPER
    // ═════════════════════════════════════════════════════════
    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }
}