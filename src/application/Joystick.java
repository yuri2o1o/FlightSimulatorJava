package application;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;

public class Joystick extends HBox {
	//the constructor loads the joystick live positions and movements to the JavaFX GUI according to data given from the program
	public Joystick() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Joystick.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(Main.view);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
