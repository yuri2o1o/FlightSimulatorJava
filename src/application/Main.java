package application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;


import javafx.application.Application;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;


public class Main extends Application {
	public static SimulatorAPI simcomm;
	public static Config conf;
	public static Stage primaryStage;
	public static Scene scene;

	@Override
	public void start(Stage nprimaryStage) {
		primaryStage = nprimaryStage;

		//start simulator
		System.out.println("Connecting to flight simulator...");
		try {
			conf = Config.readConfigFromXML("config.xml");
			simcomm = new FlightGearAPI();
			simcomm.start();
		} catch (IOException | InterruptedException e1) {
			new Alert(Alert.AlertType.ERROR, "ERROR: Could not read config / playback XML").showAndWait();
			return;
		}

		System.out.println("Connected.");

		//start JavaFX scene
		System.out.println("Opening scene...");
		try {
			BorderPane root = (BorderPane)FXMLLoader.load(getClass().getResource("Sample.fxml"));
			scene = new Scene(root,600,400);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.setResizable(false);
			primaryStage.show();
			Utils.setDisabALL(true); //disable all buttons on startup before open

			//so line charts don't display dots
			((LineChart)Utils.getNodeByID("paramGraph1")).setCreateSymbols(false);
			((LineChart)Utils.getNodeByID("paramGraph2")).setCreateSymbols(false);
		} catch(Exception e) {}
	}

	@Override
	public void stop(){
	    System.out.println("Stage is closing, finalizing...");
	    try { simcomm.finalize(); } catch(Exception e) {}; //finalize when we close the stage
	}

	public static void main(String[] args) throws Exception {
		launch(args); //start program as JavaFX project

		// The plugin
		String input,className;
		System.out.println("enter a class directory");
		BufferedReader in=new BufferedReader(new InputStreamReader(System.in));
		input=in.readLine(); // get user input
		System.out.println("enter the class name");
		className=in.readLine();
		in.close();
		// load class directory
		URLClassLoader urlClassLoader = URLClassLoader.newInstance(new URL[] {
		 new URL("file://"+input)
		});
		Class<?> c=urlClassLoader.loadClass(className);
	}
}
