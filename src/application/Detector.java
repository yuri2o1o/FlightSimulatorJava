package application;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;

public class Detector extends HBox {
	public Detector() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Detector.fxml"));        
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(Main.view);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}