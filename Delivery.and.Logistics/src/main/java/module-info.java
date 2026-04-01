module GroupProjectB.Delivery.and.Logistics {
    requires javafx.controls;
	requires javafx.fxml;
	requires java.sql;
    exports GroupProjectB.Delivery.and.Logistics;
    opens GroupProjectB.Delivery.and.Logistics to javafx.fxml;
}
