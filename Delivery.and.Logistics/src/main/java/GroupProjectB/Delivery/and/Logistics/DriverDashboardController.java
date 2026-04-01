package GroupProjectB.Delivery.and.Logistics;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class DriverDashboardController implements Initializable {

    // ── FXML Fields ──────────────────────────────────────────
    @FXML private Label  welcomeLabel;
    @FXML private Label  deliveryIdValue;
    @FXML private Label  orderRefValue;
    @FXML private Label  addressValue;
    @FXML private Label  etaValue;
    @FXML private Label  statusValue;
    @FXML private Canvas mapCanvas;
    @FXML private Button startDeliveryBtn;  // ← now wired up

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        deliveryIdValue.setText("640275");
        orderRefValue.setText("ORD894");
        addressValue.setText("19 Baker Street, London");
        etaValue.setText("17:00");
        statusValue.setText("Queued");

        drawMap();
    }

    public void setDriverName(String name) {
        welcomeLabel.setText("Welcome " + name);
    }

    // ── Handle Start Delivery ─────────────────────────────────
    @FXML
    private void handleStartDelivery() {
        // Update status label
        statusValue.setText("In Progress");
        statusValue.setStyle("-fx-text-fill: #2979ff; -fx-font-weight: bold;");

        // Lock the button — make it dark, unclickable, and visually distinct
        startDeliveryBtn.setDisable(true);
        startDeliveryBtn.setText("Delivery In Progress");
        startDeliveryBtn.setStyle(
            "-fx-background-color: #555555;" +
            "-fx-text-fill: #aaaaaa;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 5px;" +
            "-fx-cursor: default;"
        );

        // TODO: Update status in your DB
        // String sql = "UPDATE deliveries SET status='In Progress' WHERE Delivery_ID=?";
        // PreparedStatement ps = conn.prepareStatement(sql);
        // ps.setString(1, deliveryIdValue.getText());
        // ps.executeUpdate();

        showAlert(AlertType.INFORMATION, "Delivery Started",
            "Delivery #" + deliveryIdValue.getText() + " is now in progress.");
    }

    // ── Handle Close ─────────────────────────────────────────
    @FXML
    private void handleClose() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/DriverLogin.fxml"));
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 500));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Could not return to login.");
        }
    }

    // ── Map Drawing ───────────────────────────────────────────
    private void drawMap() {
        GraphicsContext gc = mapCanvas.getGraphicsContext2D();
        double w = mapCanvas.getWidth();
        double h = mapCanvas.getHeight();

        gc.setFill(Color.web("#f0f2f5"));
        gc.fillRect(0, 0, w, h);

        gc.setStroke(Color.web("#dde1e7"));
        gc.setLineWidth(1);
        int cols = 8, rows = 6;
        double cellW = w / cols;
        double cellH = h / rows;

        for (int i = 0; i <= cols; i++) gc.strokeLine(i * cellW, 0, i * cellW, h);
        for (int j = 0; j <= rows; j++) gc.strokeLine(0, j * cellH, w, j * cellH);

        double originX = w * 0.28, originY = h * 0.62;
        double destX   = w * 0.62, destY   = h * 0.38;

        gc.setStroke(Color.web("#2979ff"));
        gc.setLineWidth(2);
        gc.setLineDashes(8, 5);
        gc.strokeLine(originX, originY, destX, destY);
        gc.setLineDashes(0);

        drawPin(gc, originX, originY, Color.web("#2979ff"), Color.WHITE);
        drawPinTeardrop(gc, destX, destY, Color.web("#e53935"));
    }

    private void drawPin(GraphicsContext gc, double x, double y, Color fill, Color innerFill) {
        double r = 10;
        gc.setFill(fill);
        gc.fillOval(x - r, y - r, r * 2, r * 2);
        gc.setFill(innerFill);
        gc.fillOval(x - r * 0.45, y - r * 0.45, r * 0.9, r * 0.9);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1.5);
        gc.strokeOval(x - r, y - r, r * 2, r * 2);
    }

    private void drawPinTeardrop(GraphicsContext gc, double x, double y, Color color) {
        double r = 10;
        gc.setFill(color);
        gc.fillOval(x - r, y - r * 1.4, r * 2, r * 2);
        double[] px = {x - 4, x + 4, x};
        double[] py = {y - 2, y - 2, y + 10};
        gc.fillPolygon(px, py, 3);
        gc.setFill(Color.WHITE);
        gc.fillOval(x - r * 0.4, y - r * 1.8, r * 0.8, r * 0.8);
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}