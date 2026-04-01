package GroupProjectB.Delivery.and.Logistics;

import java.io.IOException;
import java.sql.SQLException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;



public class App extends Application {

    @Override
    public void start(Stage stage) throws ClassNotFoundException, SQLException, IOException {
        
    	
    	System.out.println(App.class.getResource("/Home.fxml"));
        
    	FXMLLoader home = new FXMLLoader(
    			App.class.getResource("/StartPage.fxml"));
    	
        var scene = new Scene(home.load());
        stage.setScene(scene);
        stage.show();
        
        
        // Controller.createAccount();
    }

    public static void main(String[] args) {
        launch();
        
        
    }

}