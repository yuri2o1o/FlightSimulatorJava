package application;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;

public class Gauges extends HBox {
	//the constructor loads the gauges components and live data to the JavaFX GUI in its place
	public Gauges() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Gauges.fxml"));        
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(Main.view);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
