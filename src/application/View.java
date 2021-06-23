package application;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class View implements Observer {
	ViewModel vm;
	
	//FlightControls
	Button openButton;
	Label totalFlightTimeLabel;
	Slider currentFlightTimeSlider;
	Label currentFlightTimeLabel;
	TextField speedMultTextfield;
	Slider speedMultSlider;
	
	//Joystick
	Circle joystickCircle;
	Slider rudderSlider;
	Slider throttleSlider;
	
	//Gauges
	Label altitudeLabel;
	Label airspeedLabel;
	Label headingLabel;
	Label rollLabel;
	Label pitchLabel;
	Label yawLabel;
	
	//Detector
	ListView parameterListView;
	ListView classListView;
	LineChart paramGraph1;
	NumberAxis paramCategoryAxis1;
	LineChart paramGraph2;
	NumberAxis paramCategoryAxis2;
	Canvas anomalyCanvas;
	
	//binds the program's vals with the javaFX GUI's components
	public void bind() {
		//FlightControls
		openButton = (Button)Utils.getNodeByID("openButton");
		totalFlightTimeLabel = (Label)Utils.getNodeByID("totalFlightTimeLabel");
		currentFlightTimeSlider = (Slider)Utils.getNodeByID("currentFlightTimeSlider");
		currentFlightTimeLabel = (Label)Utils.getNodeByID("currentFlightTimeLabel");
		speedMultTextfield = (TextField)Utils.getNodeByID("speedMultTextfield");
		speedMultSlider = (Slider)Utils.getNodeByID("speedMultSlider");
		
		//Joystick
		joystickCircle = (Circle)Utils.getNodeByID("joystickCircle");
		rudderSlider = (Slider)Utils.getNodeByID("rudderSlider");
		throttleSlider = (Slider)Utils.getNodeByID("throttleSlider");
		
		//Gauges
		altitudeLabel = (Label)Utils.getNodeByID("altitudeLabel");
		airspeedLabel = (Label)Utils.getNodeByID("airspeedLabel");
		headingLabel = (Label)Utils.getNodeByID("headingLabel");
		rollLabel = (Label)Utils.getNodeByID("rollLabel");
		pitchLabel = (Label)Utils.getNodeByID("pitchLabel");
		yawLabel = (Label)Utils.getNodeByID("yawLabel");
		
		//Detector
		parameterListView = (ListView)Utils.getNodeByID("parameterListView");
		classListView = (ListView)Utils.getNodeByID("classListView");
		paramGraph1 = (LineChart)Utils.getNodeByID("paramGraph1");
		paramCategoryAxis1 = (NumberAxis)Utils.getNodeByID("paramCategoryAxis1");
		paramGraph2 = (LineChart)Utils.getNodeByID("paramGraph2");
		paramCategoryAxis2 = (NumberAxis)Utils.getNodeByID("paramCategoryAxis2");
		anomalyCanvas = (Canvas)Utils.getNodeByID("anomalyCanvas");
		
		Utils.setDisabALL(true); //disable all buttons on startup before open
		//so line charts don't display dots
		paramGraph1.setCreateSymbols(false);
		paramGraph2.setCreateSymbols(false);
		
		vm = new ViewModel();
		vm.addObserver(this);
	}
	
	//starts connection between the simulator, Model's variables and GUI's components
	public void init() {
		//start flight emulation
		vm.startFlight();

		//init GUI
		Utils.setDisabALL(false); //enable all other buttons
		openButton.setDisable(true); //disable open button after first use
		totalFlightTimeLabel.setText(Utils.msToTimeString(Main.simcomm.getFlightLength())); //update total flight time label
		currentFlightTimeSlider.setMax(Main.simcomm.getFlightLength()); //update flight time slider

		//set slider events
		onMouseMovedSpeedSlider();
		
		//bind properties
		
		//flight controls
		currentFlightTimeSlider.valueProperty().bind(vm.currentFlightTime);
		currentFlightTimeLabel.textProperty().bind(vm.currentFlightTimeString);
		
		//joystick
		rudderSlider.valueProperty().bind(vm.rudderSlide);
		throttleSlider.valueProperty().bind(vm.throttleSlide);
		joystickCircle.translateXProperty().bind(vm.aileronTrans);
		joystickCircle.translateYProperty().bind(vm.elevatorTrans);
		
		//gauges
		altitudeLabel.textProperty().bind(vm.altitudeLabel);
		airspeedLabel.textProperty().bind(vm.airspeedLabel);
		headingLabel.textProperty().bind(vm.headingLabel);
		rollLabel.textProperty().bind(vm.rollLabel);
		pitchLabel.textProperty().bind(vm.pitchLabel);
		yawLabel.textProperty().bind(vm.yawLabel);
		
	    //set graphs updater
	    Timer graphtimer = new Timer();
	    graphtimer.scheduleAtFixedRate(new TimerTask() {
	        @Override
	        public void run() {
	        	Platform.runLater(new Runnable() {
        			@Override public void run() {
        				if (!Main.paramselected) //don't run if graphs are uninitiated
        					return;
        				
        				//erase graph data - so we can also support time jumps
        				paramGraph1.getData().clear();
        				paramGraph2.getData().clear();
        				anomalyCanvas.getGraphicsContext2D().clearRect(0, 0, anomalyCanvas.getWidth(), anomalyCanvas.getHeight());

    					//create series for param graphs (start from 00:00:05 and move one second for each graph node)
    					XYChart.Series series = new XYChart.Series();
        				for (int time = 5000; time < Main.simcomm.getCurrentFlightTime(); time += 1000)
        					try { series.getData().add(new XYChart.Data(time, Float.parseFloat(Main.simcomm.getFlightData()[Main.simcomm.getFlightDataIndexByMsTime(time)].split(",")[parameterListView.getSelectionModel().getSelectedIndex()]))); } catch (Exception e) { continue; }
        				paramGraph1.getData().add(series); //assign series
    					series = new XYChart.Series();
        				for (int time = 5000; time < Main.simcomm.getCurrentFlightTime(); time += 1000)
        					try { series.getData().add(new XYChart.Data(time, Float.parseFloat(Main.simcomm.getFlightData()[Main.simcomm.getFlightDataIndexByMsTime(time)].split(",")[Main.simcomm.getFlightParameterIndex((Main.plugin.getCorrelated(parameterListView.getSelectionModel().getSelectedItem().toString())))]))); } catch (Exception e) { continue; }
        				paramGraph2.getData().add(series); //assign series
        				
        				//draw anomaly graph on canvas via plugin
        				Main.plugin.drawOnGraph(anomalyCanvas, parameterListView.getSelectionModel().getSelectedItem().toString(), Main.simcomm.getFlightDataIndexByMsTime(Main.simcomm.getCurrentFlightTime()));
    				}
	        	});
	        }
	    }, 0, 100);
	    
	    //set lists-view values
	    List<String> flightdata = new ArrayList<>();
		flightdata = Main.simcomm.getFlightDataList();
		parameterListView.getItems().addAll(flightdata);
		classListView.getItems().addAll(new String[] { "SimpleAnomalyDetector", "ZScoreAlgo", "HybridAlgorithm" });
		classListView.getSelectionModel().select(0);
		onMouseClickedClassListView(); //update detection plugin to default
	}
	
	//opens flight csv file to read from and sets its settings
	public void onClickOpen() {
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
		
		init();
	}
	
	//changes flight's speed and the right GUI's components accordingly
	public void changeSpeedAndUpdateGUI(float speedmult) {
		speedmult = Float.parseFloat((new DecimalFormat("0.00")).format(speedmult)); //round to 2 decimal places
		speedMultTextfield.setText(speedmult + ""); //update speed text field
		speedMultSlider.setValue((int)(speedmult * 100)); //update speed slider
		Main.simcomm.setSimulationSpeed(speedmult); //set simulation speed
	}
	
	//play the flight
	public void onClickPlay()
	{
		changeSpeedAndUpdateGUI(1); //play by setting speed to 1
	}

	//pauses the flight
	public void onClickPause()
	{
		changeSpeedAndUpdateGUI(0); //pause by setting speed to 0
	}

	//stops the flight - resets it to its start
	public void onClickStop()
	{
		//stop by setting speed and current time to 0
		onClickPause();
		Main.simcomm.setCurrentFlightTime(0);
	}
	
	//slows down speed to half
	public void onClickSlow()
	{
		changeSpeedAndUpdateGUI(0.5f); //decrease speed
	}

	//double slows the speed to quarter
	public void onClickSuperSlow()
	{
		changeSpeedAndUpdateGUI(0.25f); //dramatically decrease speed
	}

	//increases speed to 1.75 times the original
	public void onClickFast()
	{
		changeSpeedAndUpdateGUI(1.75f); //increase speed
	}

	//increases speed to 4 times the original
	public void onClickSuperFast()
	{
		changeSpeedAndUpdateGUI(4f); //dramatically increase speed
	}
	
	//changes speed according to user input
	public void onTextChangedMultField() {
		try {
			//parse the text and update the simulation speed
			float speedmult = Float.parseFloat(speedMultTextfield.getText());
			if (speedmult < 0 || speedmult > 5)
				throw new Exception("Invalid speed value");
			changeSpeedAndUpdateGUI(speedmult);
		} catch(Exception e) { new Alert(Alert.AlertType.ERROR, "Invalid speed multiplayer value - must be a number between 0 and 5").showAndWait(); changeSpeedAndUpdateGUI(1); } //display error and change speed back to 1
	}


	//change label when sliding, but only change flight time when we drop the slider
	public void onMousePressedTimeSlider() {
		currentFlightTimeSlider.valueProperty().unbind();
		Main.isTimeSliding = true;
	}
	
	//updates the flight time in this function, after user presses the slider on function above
	public void onMouseReleasedTimeSlider() {
		Main.simcomm.setCurrentFlightTime((int)currentFlightTimeSlider.getValue()); //set flight time
		currentFlightTimeSlider.valueProperty().bind(vm.currentFlightTime);
		Main.isTimeSliding = false;
	}


	//change label when sliding, but only change flight time when we drop the slider
	public void onMousePressedSpeedSlider() {
		Main.isSpeedSliding = true;
	}
	//listens to the user's slider movements to know what value to update to
	public void onMouseMovedSpeedSlider() {
		speedMultSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
            	if (!Main.isSpeedSliding)
        			return;
        		speedMultTextfield.setText(Float.parseFloat((new DecimalFormat("0.00")).format(speedMultSlider.getValue()/100)) + ""); //round slider data and set it to speed field
            }
	    });
	}
	
	//updates when slider is released
	public void onMouseReleasedSpeedSlider() {
		changeSpeedAndUpdateGUI(((float)(speedMultSlider.getValue()))/100); //set flight speed
		Main.isSpeedSliding = false;
	}
	
	public void onMouseClickedParameterListView() {
		//update category axis label
		Main.paramselected = true;
		paramCategoryAxis1.setLabel((String)(parameterListView.getSelectionModel().getSelectedItems().get(0)));
		paramCategoryAxis2.setLabel(Main.plugin.getCorrelated(parameterListView.getSelectionModel().getSelectedItem().toString()));
	}
	
	public void onMouseClickedClassListView() {
		//update current plugin
		Utils.loadPlugin(classListView.getSelectionModel().getSelectedItem().toString());
	}

	@FXML
	public void exitApplication(ActionEvent event) {
	   Platform.exit(); //used to enable stop function in Main
	}
	
	@Override
	public void update(Observable o, Object arg) {}
}
