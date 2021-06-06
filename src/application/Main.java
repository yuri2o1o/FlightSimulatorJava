package application;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import test.AnomalyDetectionAlgorithm;
import test.AnomalyReport;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
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





		/*
		System.out.println("HELLo");
		String input = null,className = null;
		System.out.println("enter path to annomaly detection algorithms");
		BufferedReader in=new BufferedReader(new InputStreamReader(System.in));
		try {
			input=in.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // get user input
		System.out.println("enter the algorithms name");
		try {
			className=in.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// load class directory
		URLClassLoader urlClassLoader = null;
		try {
			urlClassLoader = URLClassLoader.newInstance(new URL[] {
			 new URL("file://"+input)
			});
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		Class<AnomalyDetectionAlgorithm> d = null;
		try {
			d = (Class<AnomalyDetectionAlgorithm>) urlClassLoader.loadClass(className);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		 try {
			AnomalyDetectionAlgorithm a = d.newInstance();
			File reg = new File(Main.conf.flight_data_csv);
			File ano = new File("C:\\Users\\User\\workspace\\FlightSimulatorJava\\anomaly_flight.csv");
			a.learnNormal(reg);
			List<AnomalyReport> list =  a.detect(ano);

			 XYChart.Series series = new XYChart.Series();
			 for(int i=0; i<list.size();i++)
			 {
				 series.getData().add(new XYChart.Data(list.get(i).x, list.get(i).y));
			 }

				System.out.println("proonce");
				((LineChart)Utils.getNodeByID("anomalyGraph")).getData().add(series);


			// List<AnomalyReport> list =  a.detect(ano);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
}
