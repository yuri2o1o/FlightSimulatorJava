package application;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;

/*
 this class loads the flight controls stats and live data into the JavaFX GUI
 */
public class FlightControls extends VBox {
	public FlightControls() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FlightControls.fxml"));        
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(Main.view);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
