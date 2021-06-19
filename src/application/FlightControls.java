package application;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;

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
