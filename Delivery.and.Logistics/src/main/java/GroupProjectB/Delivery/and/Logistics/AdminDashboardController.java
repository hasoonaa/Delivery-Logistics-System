package GroupProjectB.Delivery.and.Logistics;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    // ── Sidebar ───────────────────────────────────────────────
    @FXML private Label welcomeLabel;

    // ── Toolbar ───────────────────────────────────────────────
    @FXML private TextField  searchField;
    @FXML private MenuButton statusMenu;
    @FXML private MenuButton driverMenu;
    @FXML private Button     newDeliveryBtn;

    // ── Table ─────────────────────────────────────────────────
    @FXML private TableView<Delivery>            deliveriesTable;
    @FXML private TableColumn<Delivery, Integer> colDeliveryId;
    @FXML private TableColumn<Delivery, String>  colOrderRef;
    @FXML private TableColumn<Delivery, String>  colAddress;
    @FXML private TableColumn<Delivery, String>  colDriver;
    @FXML private TableColumn<Delivery, String>  colStatus;
    @FXML private TableColumn<Delivery, String>  colEta;
    @FXML private TableColumn<Delivery, String>  colActions;

    // ── Activity log ──────────────────────────────────────────
    @FXML private VBox activityList;

    // ── Data ──────────────────────────────────────────────────
    private final ObservableList<Delivery> allDeliveries = FXCollections.observableArrayList();
    private FilteredList<Delivery> filteredDeliveries;

    // ── DB ────────────────────────────────────────────────────
    private static final String DB_URL = "JDBC:sqlite:DRIVERS.db";

    // ── Driver roster ─────────────────────────────────────────
    private static final List<String> DRIVERS = List.of(
        "Unassigned",
        "Ahmed  - DRV001",
        "Sara   - DRV002",
        "Omar   - DRV003",
        "Layla  - DRV004",
        "Tariq  - DRV005",
        "Nadia  - DRV006",
        "Yusuf  - DRV007"
    );

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    // ── Filters ───────────────────────────────────────────────
    private String activeStatusFilter = null;
    private String activeDriverFilter = null;

    // ── Logged-in admin name ──────────────────────────────────
    private String currentAdminUsername = "";   // ← initialised to empty string

    // ═════════════════════════════════════════════════════════
    // INIT
    // ═════════════════════════════════════════════════════════
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        try { loadDeliveriesFromDB(); } catch (ClassNotFoundException e) { e.printStackTrace(); }
        setupSearch();
    }

    // ── NOW saves the name into currentAdminUsername ──────────
    public void setAdminName(String name) {
        currentAdminUsername = name;                          // ← THIS was the missing line
        welcomeLabel.setText("Welcome back " + name + "!");
    }

    private Connection getConnection() throws Exception {
        Class.forName("org.sqlite.JDBC");
        return DriverManager.getConnection(DB_URL);
    }

    // ═════════════════════════════════════════════════════════
    // TABLE SETUP
    // ═════════════════════════════════════════════════════════
    private void setupTableColumns() {
        colDeliveryId.setCellValueFactory(new PropertyValueFactory<>("deliveryId"));
        colOrderRef  .setCellValueFactory(new PropertyValueFactory<>("orderRef"));
        colAddress   .setCellValueFactory(new PropertyValueFactory<>("address"));
        colDriver    .setCellValueFactory(new PropertyValueFactory<>("driver"));
        colStatus    .setCellValueFactory(new PropertyValueFactory<>("status"));
        colEta       .setCellValueFactory(new PropertyValueFactory<>("eta"));

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn   = new Button("View Details");
            private final Button assignBtn = new Button("Assign Driver");
            private final Button deleteBtn = new Button("Delete");
            private final VBox   box       = new VBox(4, viewBtn, assignBtn, deleteBtn);

            {
                viewBtn  .getStyleClass().add("action-view-btn");
                assignBtn.getStyleClass().add("action-assign-btn");
                deleteBtn.getStyleClass().add("action-delete-btn");

                viewBtn.setOnAction(e -> {
                    Delivery d = getTableView().getItems().get(getIndex());
                    showAlert(Alert.AlertType.INFORMATION, "Delivery Details",
                        "ID: "      + d.getDeliveryId()
                        + "\nRef: "     + d.getOrderRef()
                        + "\nAddress: " + d.getAddress()
                        + "\nDriver: "  + d.getDriver()
                        + "\nStatus: "  + d.getStatus()
                        + "\nETA: "     + d.getEta());
                });

                assignBtn.setOnAction(e -> {
                    Delivery d = getTableView().getItems().get(getIndex());
                    showAssignDriverDialog(d);
                });

                deleteBtn.setOnAction(e -> {
                    Delivery d = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete Delivery");
                    confirm.setHeaderText(null);
                    confirm.setContentText("Delete Delivery #" + d.getDeliveryId()
                        + " (" + d.getOrderRef() + ")?");
                    confirm.initOwner((Stage) deliveriesTable.getScene().getWindow());
                    confirm.showAndWait().ifPresent(r -> {
                        if (r == ButtonType.OK) deleteDelivery(d);
                    });
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Delivery d = getTableView().getItems().get(getIndex());
                assignBtn.setVisible("Unassigned".equals(d.getDriver()));
                setGraphic(box);
            }
        });
    }

    // ═════════════════════════════════════════════════════════
    // DELETE DELIVERY
    // ═════════════════════════════════════════════════════════
    private void deleteDelivery(Delivery d) {
        try (Connection c = getConnection()) {
            PreparedStatement ps = c.prepareStatement(
                "DELETE FROM deliveries WHERE Delivery_ID = ?");
            ps.setInt(1, d.getDeliveryId());
            ps.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "DB Error", "Could not delete delivery.");
            return;
        }
        allDeliveries.remove(d);
        addActivityEntry("🗑 Delivery #" + d.getDeliveryId()
            + " (" + d.getOrderRef() + ") deleted");
        showAlert(Alert.AlertType.INFORMATION, "Deleted",
            "Delivery #" + d.getDeliveryId() + " deleted.");
    }

    // ═════════════════════════════════════════════════════════
    // ASSIGN DRIVER DIALOG
    // ═════════════════════════════════════════════════════════
    private void showAssignDriverDialog(Delivery d) {
        Stage owner = (Stage) deliveriesTable.getScene().getWindow();

        Dialog<String> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Assign Driver");
        dialog.setHeaderText("Delivery #" + d.getDeliveryId() + "  —  " + d.getOrderRef());

        ButtonType assignType = new ButtonType("Assign", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(assignType, ButtonType.CANCEL);

        ComboBox<String> picker = new ComboBox<>();
        picker.getItems().addAll(DRIVERS);
        picker.setValue(DRIVERS.get(0));
        picker.setPrefWidth(240);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(12);
        grid.setPadding(new Insets(20));
        Label lbl = new Label("Select Driver:");
        lbl.getStyleClass().add("field-label");
        grid.add(lbl, 0, 0);
        grid.add(picker, 1, 0);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> btn == assignType ? picker.getValue() : null);

        dialog.showAndWait().ifPresent(selectedDriver -> {
            try (Connection c = getConnection()) {
                PreparedStatement ps = c.prepareStatement(
                    "UPDATE deliveries SET driver = ? WHERE Delivery_ID = ?");
                ps.setString(1, selectedDriver);
                ps.setInt(2, d.getDeliveryId());
                ps.executeUpdate();
            } catch (Exception ex) { ex.printStackTrace(); }

            allDeliveries.clear();
            try { loadDeliveriesFromDB(); }
            catch (ClassNotFoundException ex) { ex.printStackTrace(); }

            addActivityEntry("👤 " + selectedDriver + " assigned to #" + d.getDeliveryId());
            showAlert(Alert.AlertType.INFORMATION, "Assigned",
                selectedDriver + " assigned to Delivery #" + d.getDeliveryId());
        });
    }

    // ═════════════════════════════════════════════════════════
    // NEW DELIVERY DIALOG
    // ═════════════════════════════════════════════════════════
    @FXML
    private void handleNewDelivery() {
        Stage owner = (Stage) newDeliveryBtn.getScene().getWindow();

        Dialog<Delivery> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("New Delivery");
        dialog.setHeaderText("Create a new delivery order");

        ButtonType createType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createType, ButtonType.CANCEL);

        TextField deliveryIdField = new TextField();
        deliveryIdField.setPromptText("e.g. 123456");
        deliveryIdField.getStyleClass().add("input-field");

        TextField orderRefField = new TextField();
        orderRefField.setPromptText("e.g. ORD999");
        orderRefField.getStyleClass().add("input-field");

        TextField addressField = new TextField();
        addressField.setPromptText("e.g. 45 High Street, London");
        addressField.getStyleClass().add("input-field");

        TextField etaField = new TextField();
        etaField.setPromptText("e.g. 2026-05-01 14:00  (optional)");
        etaField.getStyleClass().add("input-field");

        ComboBox<String> driverPicker = new ComboBox<>();
        driverPicker.getItems().addAll(DRIVERS);
        driverPicker.setValue("Unassigned");
        driverPicker.setPrefWidth(240);

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(14);
        grid.setPadding(new Insets(20, 24, 10, 24));

        String[] lblTexts = {"Delivery ID *", "Order Ref *", "Address *", "ETA (optional)", "Driver (optional)"};
        for (int i = 0; i < lblTexts.length; i++) {
            Label l = new Label(lblTexts[i]);
            l.getStyleClass().add("field-label");
            grid.add(l, 0, i);
        }
        grid.add(deliveryIdField, 1, 0);
        grid.add(orderRefField,   1, 1);
        grid.add(addressField,    1, 2);
        grid.add(etaField,        1, 3);
        grid.add(driverPicker,    1, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(460);

        Button createBtn = (Button) dialog.getDialogPane().lookupButton(createType);
        createBtn.setDisable(true);
        createBtn.getStyleClass().add("login-btn");
        Runnable check = () -> createBtn.setDisable(
            deliveryIdField.getText().trim().isEmpty()
            || orderRefField.getText().trim().isEmpty()
            || addressField.getText().trim().isEmpty()
        );
        deliveryIdField.textProperty().addListener((o, ov, nv) -> check.run());
        orderRefField  .textProperty().addListener((o, ov, nv) -> check.run());
        addressField   .textProperty().addListener((o, ov, nv) -> check.run());

        dialog.setResultConverter(btn -> {
            if (btn != createType) return null;
            try {
                return new Delivery(
                    Integer.parseInt(deliveryIdField.getText().trim()),
                    orderRefField.getText().trim(),
                    addressField.getText().trim(),
                    driverPicker.getValue(),
                    "Pending",
                    etaField.getText().trim().isEmpty() ? "TBD" : etaField.getText().trim()
                );
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Delivery ID must be a number.");
                return null;
            }
        });

        dialog.showAndWait().ifPresent(nd -> {
            try (Connection c = getConnection()) {
                PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO deliveries (Delivery_ID, Order_Ref, address, driver, status, eta) VALUES (?,?,?,?,?,?)");
                ps.setInt(1,    nd.getDeliveryId());
                ps.setString(2, nd.getOrderRef());
                ps.setString(3, nd.getAddress());
                ps.setString(4, nd.getDriver());
                ps.setString(5, nd.getStatus());
                ps.setString(6, nd.getEta());
                ps.executeUpdate();
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.WARNING, "DB Warning",
                    "Delivery added to view but could not be saved to database.");
            }

            allDeliveries.add(nd);
            addActivityEntry("📦 New delivery #" + nd.getDeliveryId()
                + " (" + nd.getOrderRef() + ") created"
                + ("Unassigned".equals(nd.getDriver()) ? " — no driver yet" : " → " + nd.getDriver()));
            showAlert(Alert.AlertType.INFORMATION, "Delivery Created",
                "Delivery #" + nd.getDeliveryId() + " added successfully.");
        });
    }

    // ═════════════════════════════════════════════════════════
    // DB LOAD
    // ═════════════════════════════════════════════════════════
    private void loadDeliveriesFromDB() throws ClassNotFoundException {
        String sql = "SELECT Delivery_ID, Order_Ref, address, driver, status, eta FROM deliveries";
        try (Connection c = getConnection();
             ResultSet rs = c.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                allDeliveries.add(new Delivery(
                    rs.getInt("Delivery_ID"),
                    rs.getString("Order_Ref"),
                    rs.getString("address"),
                    rs.getString("driver"),
                    rs.getString("status"),
                    rs.getString("eta")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            loadDemoData();
        }
        filteredDeliveries = new FilteredList<>(allDeliveries, p -> true);
        deliveriesTable.setItems(filteredDeliveries);
    }

    private void loadDemoData() {
        allDeliveries.addAll(
            new Delivery(458721, "ORD465", "12 Baker Street, London",    "Unassigned",     "Pending",         "2025-12-12 15:00"),
            new Delivery(857468, "ORD768", "78 King Street, Bristol",    "Ahmed - DRV001", "In Transit",      "2025-12-02 16:30"),
            new Delivery(482917, "ORD465", "19 Baker Street, London",    "Unassigned",     "Pending",         "2026-01-01 19:00"),
            new Delivery(640275, "ORD894", "9 Queen Street, London",     "Unassigned",     "Pending",         "2026-01-02 12:00"),
            new Delivery(734809, "ORD028", "24 Bellevue Road, Southend", "Sara - DRV002",  "Out for Delivery","2026-01-05 16:00"),
            new Delivery(465710, "ORD630", "33 High Street, Leeds",      "Omar - DRV003",  "Out for Delivery","2026-02-14 22:00")
        );
    }

    // ═════════════════════════════════════════════════════════
    // SEARCH & FILTERS
    // ═════════════════════════════════════════════════════════
    private void setupSearch() {
        searchField.textProperty().addListener((obs, o, n) -> applyFilter());
    }

    private void applyFilter() {
        String s = searchField.getText().toLowerCase();
        filteredDeliveries.setPredicate(d -> {
            boolean ms  = s.isEmpty()
                || String.valueOf(d.getDeliveryId()).contains(s)
                || d.getOrderRef().toLowerCase().contains(s)
                || d.getAddress().toLowerCase().contains(s)
                || d.getDriver().toLowerCase().contains(s);
            boolean mst = activeStatusFilter == null
                || d.getStatus().equalsIgnoreCase(activeStatusFilter);
            boolean md  = activeDriverFilter == null
                || d.getDriver().toLowerCase().contains(activeDriverFilter.toLowerCase());
            return ms && mst && md;
        });
    }

    @FXML private void filterAll()            { activeStatusFilter = null;              statusMenu.setText("Status ▾");          applyFilter(); }
    @FXML private void filterPending()        { activeStatusFilter = "Pending";          statusMenu.setText("Pending ▾");         applyFilter(); }
    @FXML private void filterInTransit()      { activeStatusFilter = "In Transit";       statusMenu.setText("In Transit ▾");      applyFilter(); }
    @FXML private void filterOutForDelivery() { activeStatusFilter = "Out for Delivery"; statusMenu.setText("Out for Delivery ▾");applyFilter(); }
    @FXML private void filterDelivered()      { activeStatusFilter = "Delivered";        statusMenu.setText("Delivered ▾");       applyFilter(); }
    @FXML private void filterFailed()         { activeStatusFilter = "Failed";           statusMenu.setText("Failed ▾");          applyFilter(); }

    @FXML private void filterUnassigned() { activeDriverFilter = "Unassigned"; driverMenu.setText("Unassigned ▾"); applyFilter(); }
    @FXML private void filterAhmed()      { activeDriverFilter = "Ahmed";      driverMenu.setText("Ahmed ▾");      applyFilter(); }
    @FXML private void filterSara()       { activeDriverFilter = "Sara";       driverMenu.setText("Sara ▾");       applyFilter(); }
    @FXML private void filterOmar()       { activeDriverFilter = "Omar";       driverMenu.setText("Omar ▾");       applyFilter(); }

    @FXML private void sortById()      { deliveriesTable.getSortOrder().clear(); colDeliveryId.setSortType(TableColumn.SortType.ASCENDING);  deliveriesTable.getSortOrder().add(colDeliveryId); deliveriesTable.sort(); }
    @FXML private void sortByIdDesc()  { deliveriesTable.getSortOrder().clear(); colDeliveryId.setSortType(TableColumn.SortType.DESCENDING); deliveriesTable.getSortOrder().add(colDeliveryId); deliveriesTable.sort(); }
    @FXML private void sortByEta()     { deliveriesTable.getSortOrder().clear(); colEta.setSortType(TableColumn.SortType.ASCENDING);         deliveriesTable.getSortOrder().add(colEta);        deliveriesTable.sort(); }
    @FXML private void sortByEtaDesc() { deliveriesTable.getSortOrder().clear(); colEta.setSortType(TableColumn.SortType.DESCENDING);        deliveriesTable.getSortOrder().add(colEta);        deliveriesTable.sort(); }

    // ═════════════════════════════════════════════════════════
    // ACTIVITY LOG
    // ═════════════════════════════════════════════════════════
    private void addActivityEntry(String text) {
        String time = LocalDateTime.now().format(TIME_FMT);
        Label entry = new Label(time + " — " + text);
        entry.getStyleClass().add("activity-entry");
        entry.setWrapText(true);
        activityList.getChildren().add(0, entry);
    }

    // ═════════════════════════════════════════════════════════
    // SIDEBAR NAV
    // ═════════════════════════════════════════════════════════
    @FXML
    private void handleDeliveryMap() {
        Stage owner = (Stage) welcomeLabel.getScene().getWindow();

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Driver Access Required");
        dialog.setHeaderText("Driver verification required. Enter the driver's username and password to proceed.");

        ButtonType continueType = new ButtonType("Continue", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(continueType, ButtonType.CANCEL);

        TextField     usernameField = new TextField();
        usernameField.setPromptText("Driver username");
        usernameField.getStyleClass().add("input-field");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Driver password");
        passwordField.getStyleClass().add("input-field");

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(14);
        grid.setPadding(new Insets(20, 24, 10, 24));

        Label u = new Label("Username:"); u.getStyleClass().add("field-label");
        Label p = new Label("Password:"); p.getStyleClass().add("field-label");
        grid.add(u, 0, 0); grid.add(usernameField, 1, 0);
        grid.add(p, 0, 1); grid.add(passwordField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(420);

        Button continueBtn = (Button) dialog.getDialogPane().lookupButton(continueType);
        continueBtn.setDisable(true);
        continueBtn.getStyleClass().add("login-btn");
        Runnable check = () -> continueBtn.setDisable(
            usernameField.getText().trim().isEmpty()
            || passwordField.getText().trim().isEmpty()
        );
        usernameField.textProperty().addListener((o, ov, nv) -> check.run());
        passwordField.textProperty().addListener((o, ov, nv) -> check.run());

        dialog.showAndWait().ifPresent(btn -> {
            if (btn != continueType) return;

            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            boolean valid = false;
            String driverName = "";

            try (Connection c = getConnection()) {
                PreparedStatement ps = c.prepareStatement(
                    "SELECT * FROM drivers WHERE username = ? AND password = ?");
                ps.setString(1, username);
                ps.setString(2, password);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    valid = true;
                    try { driverName = rs.getString("name"); }
                    catch (Exception ex) { driverName = username; }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "DB Error",
                    "Could not verify driver credentials.");
                return;
            }

            if (valid) {
                try {
                    FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/DriverDashboard.fxml"));
                    Parent root = loader.load();
                    DriverDashboardController controller = loader.getController();
                    controller.setDriverName(driverName.isEmpty() ? username : driverName);
                    Stage stage = (Stage) welcomeLabel.getScene().getWindow();
                    stage.setScene(new Scene(root, 900, 520));
                    stage.show();
                    addActivityEntry("🗺 Admin accessed map as: " + username);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Could not load Delivery Map.");
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Access Denied",
                    "Incorrect driver username or password.");
            }
        });
    }

    @FXML
    private void handleSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/AdminSettings.fxml"));
            Parent root = loader.load();
            AdminSettingsController controller = loader.getController();
            controller.setAdminName(currentAdminUsername);
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 580));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load Settings.");
        }
    }

    @FXML
    private void handleTestDelivery() {
        showAlert(Alert.AlertType.INFORMATION, "Test Delivery", "Test delivery triggered.");
        addActivityEntry("🧪 TEST delivery created");
    }

    @FXML
    private void handleLogout() {
        try {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Logged Out");
            alert.setHeaderText(null);
            alert.setContentText("You've been logged out!");
            alert.showAndWait();

            Parent root = FXMLLoader.load(getClass().getResource("/StartPage.fxml"));
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 500));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load Start Page.");
        }
    }

    // ═════════════════════════════════════════════════════════
    // HELPERS
    // ═════════════════════════════════════════════════════════
    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }

    // ═════════════════════════════════════════════════════════
    // DELIVERY MODEL
    // ═════════════════════════════════════════════════════════
    public static class Delivery {
        private final int deliveryId;
        private final String orderRef, address, driver, status, eta;

        public Delivery(int deliveryId, String orderRef, String address,
                        String driver, String status, String eta) {
            this.deliveryId = deliveryId; this.orderRef = orderRef;
            this.address    = address;    this.driver   = driver;
            this.status     = status;     this.eta      = eta;
        }

        public int    getDeliveryId() { return deliveryId; }
        public String getOrderRef()   { return orderRef;   }
        public String getAddress()    { return address;    }
        public String getDriver()     { return driver;     }
        public String getStatus()     { return status;     }
        public String getEta()        { return eta;        }
    }
}