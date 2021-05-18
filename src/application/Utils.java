package application;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

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
	
	public static Node getNodeByID(String id) {
		return Main.scene.lookup("#" + id);
	}
	
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
		
		getNodeByID("parameterListView1").setDisable(disabAll);
		getNodeByID("parameterListView2").setDisable(disabAll);
		
		//change color for joystick
		if (disabAll)
			((Circle)getNodeByID("joystickCircle")).setFill(Paint.valueOf("#9ea9b2"));
		else
			((Circle)getNodeByID("joystickCircle")).setFill(Paint.valueOf("#7ebcee"));
	}
}
