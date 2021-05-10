package application;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class JavaFXController {	
	public boolean isTimeSliding = false;
	public boolean isSpeedSliding = false;
	
	public void onClickOpen()
	{
		//choose Flight CSV file
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Flight CSV File");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("CSV Files", "*.csv"));
		File selectedFile = fileChooser.showOpenDialog(Main.primaryStage);
		if (selectedFile == null) {
			new Alert(Alert.AlertType.ERROR, "ERROR: Invalid Selected CSV File").showAndWait();
			return;
		}
		Main.conf.flight_data_csv = selectedFile.getAbsolutePath();
		Main.conf.playback_speed_multiplayer = 0; //start paused
		
		//start flight emulation
		System.out.println("Starting flight emulation...");
		//use API to send flight data
		try {
			Main.simcomm.loadFlightDataFromCSV(Main.conf.flight_data_csv);
			Main.simcomm.sendFlightDataToSimulator();
		} catch (IOException e) {
			new Alert(Alert.AlertType.ERROR, "ERROR: Could not send flight data XML to simulator").showAndWait();
			return;
		}
		
		//update GUI
		Utils.setDisabALL(false); //enable all other buttons
		Utils.getNodeByID("openButton").setDisable(true); //disable open button after first use
		((Label)Utils.getNodeByID("totalFlightTimeLabel")).setText(Utils.msToTimeString(Main.simcomm.getFlightLength())); //update total flight time label
		((Slider)Utils.getNodeByID("currentFlightTimeSlider")).setMax(Main.simcomm.getFlightLength()); //update flight time slider
		
		//set slider events
		onMouseMovedTimeSlider();
		onMouseMovedSpeedSlider();
		
		//set current time updater
		Timer timetimer = new Timer();
		timetimer.scheduleAtFixedRate(new TimerTask() {
	        @Override
	        public void run() {
	        	Platform.runLater(new Runnable() {
        			@Override public void run() {
        				//when we are not using the time slider, update it (and the label) as time goes on...
	        			if (isTimeSliding)
	        				return;
        				((Slider)Utils.getNodeByID("currentFlightTimeSlider")).setValue(Main.simcomm.getCurrentFlightTime());
        				((Label)Utils.getNodeByID("currentFlightTimeLabel")).setText(Utils.msToTimeString(Main.simcomm.getCurrentFlightTime()));
        				//reset the flight (stop button) when it finishes
        				if (Main.simcomm.getCurrentFlightTime() >= Main.simcomm.getFlightLength())
	        				onClickStop();
  		      		}
	        	});
	        }
	    }, 0, 100);
	    
	    //set joystick updater
	    Timer joysticktimer = new Timer();
	    joysticktimer.scheduleAtFixedRate(new TimerTask() {
	        @Override
	        public void run() {
	        	Platform.runLater(new Runnable() {
        			@Override public void run() {
        				//get flight parameter values and set them to joystick sliders
        				float rudder = Main.simcomm.getFlightParameter("rudder");
        				if (rudder > -999)
        					((Slider)Utils.getNodeByID("rudderSlider")).setValue(rudder);
        				float throttle = Main.simcomm.getFlightParameter("throttle");
        				if (throttle > -999)
        					((Slider)Utils.getNodeByID("throttleSlider")).setValue(throttle);
        				
        				//get flight parameter values and set them to the joystick circle's offset
        				float aileron = Main.simcomm.getFlightParameter("aileron");
        				float elevator = Main.simcomm.getFlightParameter("elevator");
        				if (aileron > -999 && elevator > -999) {
        					Circle joystick = (Circle)Utils.getNodeByID("joystickCircle");
        					joystick.setTranslateX(aileron*150);
        					joystick.setTranslateY(elevator*-150);
        				}
    				}
	        	});
	        }
	    }, 0, 100);
	    
	  //set gauges panel updater
	    Timer gaugestimer = new Timer();
	    gaugestimer.scheduleAtFixedRate(new TimerTask() {
	        @Override
	        public void run() {
	        	Platform.runLater(new Runnable() {
        			@Override public void run() {
        				//get flight parameter values and set them to gauges
        				((Label)Utils.getNodeByID("altitudeLabel")).setText(Main.simcomm.getFlightParameter("altimeter_indicated-altitude-ft")+"");
        				((Label)Utils.getNodeByID("airspeedLabel")).setText(Main.simcomm.getFlightParameter("airspeed-indicator_indicated-speed-kt")+"");
        				((Label)Utils.getNodeByID("headingLabel")).setText(Main.simcomm.getFlightParameter("indicated-heading-deg")+"");
        				((Label)Utils.getNodeByID("rollLabel")).setText(Main.simcomm.getFlightParameter("attitude-indicator_indicated-roll-deg")+"");
        				((Label)Utils.getNodeByID("pitchLabel")).setText(Main.simcomm.getFlightParameter("attitude-indicator_internal-pitch-deg")+"");
        				((Label)Utils.getNodeByID("yawLabel")).setText(Main.simcomm.getFlightParameter("side-slip-deg")+"");
    				}
	        	});
	        }
	    }, 0, 100);
	}

	public void changeSpeedAndUpdateGUI(float speedmult) {
		speedmult = Float.parseFloat((new DecimalFormat("0.00")).format(speedmult)); //round to 2 decimal places
		((TextField)Utils.getNodeByID("speedMultTextfield")).setText(speedmult + ""); //update speed text field
		((Slider)Utils.getNodeByID("speedMultSlider")).setValue((int)(speedmult * 100)); //update speed slider
		Main.simcomm.setSimulationSpeed(speedmult); //set simulation speed
	}
	
	public void onClickPlay()
	{
		changeSpeedAndUpdateGUI(1); //play by setting speed to 1
	}

	public void onClickPause()
	{
		changeSpeedAndUpdateGUI(0); //pause by setting speed to 0
	}

	public void onClickStop()
	{
		//stop by setting speed and current time to 0
		onClickPause();
		Main.simcomm.setCurrentFlightTime(0);
	}
	
	public void onClickSlow()
	{
		changeSpeedAndUpdateGUI(0.5f); //decrease speed
	}

	public void onClickSuperSlow()
	{
		changeSpeedAndUpdateGUI(0.25f); //dramatically decrease speed
	}

	public void onClickFast()
	{
		changeSpeedAndUpdateGUI(1.75f); //increase speed
	}

	public void onClickSuperFast()
	{
		changeSpeedAndUpdateGUI(4f); //dramatically increase speed
	}
	
	public void onTextChangedMultField() {	
		try { 
			//parse the text and update the simulation speed
			float speedmult = Float.parseFloat(((TextField)Utils.getNodeByID("speedMultTextfield")).getText());
			if (speedmult < 0 || speedmult > 5)
				throw new Exception("Invalid speed value");
			changeSpeedAndUpdateGUI(speedmult);
		} catch(Exception e) { new Alert(Alert.AlertType.ERROR, "Invalid speed multiplayer value - must be a number between 0 and 5").showAndWait(); changeSpeedAndUpdateGUI(1); } //display error and change speed back to 1
	}
	
	
	//change label when sliding, but only change flight time when we drop the slider
	public void onMousePressedTimeSlider() {
		isTimeSliding = true;
	}
	public void onMouseMovedTimeSlider() {
		((Slider)Utils.getNodeByID("currentFlightTimeSlider")).valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) { 
            	if (!isTimeSliding)
        			return;
        		((Label)Utils.getNodeByID("currentFlightTimeLabel")).setText(Utils.msToTimeString((long)((Slider)Utils.getNodeByID("currentFlightTimeSlider")).getValue())); //update label
            }
	    });
	}
	
	public void onMouseReleasedTimeSlider() {
		Main.simcomm.setCurrentFlightTime((long)((Slider)Utils.getNodeByID("currentFlightTimeSlider")).getValue()); //set flight time
		isTimeSliding = false;
	}
	
	
	//change label when sliding, but only change flight time when we drop the slider
	public void onMousePressedSpeedSlider() {
		isSpeedSliding = true;
	}
	public void onMouseMovedSpeedSlider() {
		((Slider)Utils.getNodeByID("speedMultSlider")).valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) { 
            	if (!isSpeedSliding)
        			return;
        		((TextField)Utils.getNodeByID("speedMultTextfield")).setText(Float.parseFloat((new DecimalFormat("0.00")).format(((Slider)Utils.getNodeByID("speedMultSlider")).getValue()/100)) + ""); //round slider data and set it to speed field
            }
	    });
	}
	
	public void onMouseReleasedSpeedSlider() {
		changeSpeedAndUpdateGUI(((float)(((Slider)Utils.getNodeByID("speedMultSlider")).getValue()))/100); //set flight speed
		isSpeedSliding = false;
	}
	
	@FXML
	public void exitApplication(ActionEvent event) {
	   Platform.exit(); //used to enable stop function in Main
	}
}
