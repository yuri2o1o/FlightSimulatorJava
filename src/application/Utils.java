package application;

import java.io.File;
import java.net.MalformedURLException;

import java.net.URL;
import java.net.URLClassLoader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

import plugin.AnomalyDetectionAlgorithm;

public class Utils {
	/*
	 * Parses a time in ms to an HH:MM:SS format
	 * in: ms - the time in ms
	 * out: String - the parsed HH:MM:SS string
	 */
	public static String msToTimeString(long ms) {
		SimpleDateFormat df = (new SimpleDateFormat("HH:mm:ss"));
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		return df.format((new Date(ms)));
	}
	//gets the right component from javaFX according to id given
	public static Node getNodeByID(String id) {
		return Main.scene.lookup("#" + id);
	}
	//sets all javaFX components' (buttons, sliders etc.) option for input to the boolean val given
	public static void setDisabALL(boolean disabAll) {
		getNodeByID("playButton").setDisable(disabAll);
		getNodeByID("pauseButton").setDisable(disabAll);
		getNodeByID("stopButton").setDisable(disabAll);

		getNodeByID("superSlowButton").setDisable(disabAll);
		getNodeByID("slowButton").setDisable(disabAll);
		getNodeByID("fastButton").setDisable(disabAll);
		getNodeByID("superFastButton").setDisable(disabAll);

		getNodeByID("speedMultSlider").setDisable(disabAll);
		getNodeByID("speedMultTextfield").setDisable(disabAll);

		getNodeByID("currentFlightTimeLabel").setDisable(disabAll);
		getNodeByID("currentFlightTimeSlider").setDisable(disabAll);
		getNodeByID("totalFlightTimeLabel").setDisable(disabAll);

		getNodeByID("parameterListView").setDisable(disabAll);
		getNodeByID("classListView").setDisable(disabAll);

		//change color for joystick
		if (disabAll)
			((Circle)getNodeByID("joystickCircle")).setFill(Paint.valueOf("#9ea9b2"));
		else
			((Circle)getNodeByID("joystickCircle")).setFill(Paint.valueOf("#7ebcee"));
	}
	
	//loads the anomaly detection algorithm as a plugin to the program and activates its functionality -learnNormal, detect
	public static void loadPlugin(String classname) {
		// load class directory
		URLClassLoader urlClassLoader = null;
		try {
			urlClassLoader = URLClassLoader.newInstance(new URL[] {
			 new URL("file://" + System.getProperty("user.dir") + "\\bin")
			});
		} catch (MalformedURLException e) {
			new Alert(Alert.AlertType.ERROR, "ERROR: Could not load detection plugin directory").showAndWait();
			return;
		}


		Class<AnomalyDetectionAlgorithm> plugin = null;
		try {
			plugin = (Class<AnomalyDetectionAlgorithm>)(urlClassLoader.loadClass("plugin." + classname));
		} catch (ClassNotFoundException e) {
			new Alert(Alert.AlertType.ERROR, "ERROR: Could not load detection plugin").showAndWait();
			return;
		}

		try {
			Main.plugin = plugin.newInstance();
			Main.plugin.learnNormal(new File("reg_flight.csv"));
			Main.plugin.detect(new File(Main.conf.flight_data_csv));
		} catch (InstantiationException | IllegalAccessException e) {
			new Alert(Alert.AlertType.ERROR, "ERROR: Detection plugin failure").showAndWait();
			return;
		}
	}
}
