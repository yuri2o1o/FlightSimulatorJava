package application;

import java.io.IOException;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;

//import plugin.AnomalyDetectionAlgorithm;

public class Main extends Application {
	//main globals
	public static SimulatorAPI simcomm;
	public static Config conf;
	public static Stage primaryStage;
	public static Scene scene;
	public static AnomalyDetectionAlgorithm plugin;
	public static View view = new View();
	
	//javafx globals
	public static boolean isTimeSliding = false;
	public static boolean isSpeedSliding = false;
	public static boolean paramselected = false;

	/*
	 starts the FlightGear program using its API, implemented from the SimulatorAPI interface 
	 */
	@Override
	public void start(Stage nprimaryStage) throws IOException {
		primaryStage = nprimaryStage;
		
		//start simulator
		System.out.println("Connecting to flight simulator...");
		try {
			conf = Config.readConfigFromXML("config.xml");
			simcomm = new FlightGearAPI();
			simcomm.start();
		} catch (IOException | InterruptedException e1) {
			new Alert(Alert.AlertType.ERROR, "CRITICAL ERROR: Could not read config / playback XML / Invalid simulator path in config").showAndWait();
			return;
		}

		System.out.println("Connected.");

		//start JavaFX scene
		System.out.println("Opening scene...");
		try {
			BorderPane root = (BorderPane)FXMLLoader.load(getClass().getResource("Layout.fxml"));
			scene = new Scene(root,600,400);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			view.bind();
			primaryStage.setScene(scene);
			primaryStage.setResizable(false);
			primaryStage.show();
		} catch(Exception e) {
			new Alert(Alert.AlertType.ERROR, "CRITICAL ERROR: Could not open scene").showAndWait();
			return;
		}
	}

	//closing connection from the API to the simulator
	@Override
	public void stop(){
		System.out.println("Stage is closing, finalizing...");
	    try { simcomm.finalize(); } catch(Exception e) {}; //finalize when we close the stage
	}
	
	public static void main(String[] args) throws Exception {
		launch(args); //start program as JavaFX project
	}
}
