package application;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;

public class Gauges extends HBox {
	public Gauges() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Gauges.fxml"));        
        fxmlLoader.setRoot(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
