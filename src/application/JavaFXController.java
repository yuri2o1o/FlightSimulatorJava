package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import test.AnomalyDetectionAlgorithm;
import test.AnomalyReport;
import test.ZScoreAlgo;

public class JavaFXController {
	public boolean isTimeSliding = false;
	public boolean isSpeedSliding = false;

	private boolean param1selected = false;
	private boolean param2selected = false;

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

	    //set param graphs updater
	    Timer paramtimer = new Timer();
	    gaugestimer.scheduleAtFixedRate(new TimerTask() {
	        @Override
	        public void run() {
	        	Platform.runLater(new Runnable() {
        			@Override public void run() {
        				//erase graph data - so we can also support time jumps
        				((LineChart)Utils.getNodeByID("paramGraph1")).getData().clear();
        				((LineChart)Utils.getNodeByID("paramGraph2")).getData().clear();

        				//create series for graph 1 (start from 00:00:05 and move one second for each graph node)
        				if (param1selected)
        				{
	        				XYChart.Series series = new XYChart.Series();
	        				for (int time = 5000; time < Main.simcomm.getCurrentFlightTime(); time += 1000)
	        					try { series.getData().add(new XYChart.Data(time, Float.parseFloat(Main.simcomm.getFlightData()[Main.simcomm.getFlightDataIndexByMsTime(time)].split(",")[((ListView)Utils.getNodeByID("parameterListView1")).getSelectionModel().getSelectedIndex()]))); } catch (Exception e) { continue; }
	        				((LineChart)Utils.getNodeByID("paramGraph1")).getData().add(series); //assign series
        				}

        				//create series for graph 2
        				if (param2selected)
        				{
        					 /*
        					 XYChart.Series series = new XYChart.Series();
	        				for (int time = 5000; time < Main.simcomm.getCurrentFlightTime(); time += 1000)
	        					try { series.getData().add(new XYChart.Data(time, Float.parseFloat(Main.simcomm.getFlightData()[Main.simcomm.getFlightDataIndexByMsTime(time)].split(",")[((ListView)Utils.getNodeByID("parameterListView2")).getSelectionModel().getSelectedIndex()]))); } catch (Exception e) { continue; }
	        				((LineChart)Utils.getNodeByID("paramGraph2")).getData().add(series); //assign series

	        				*/
        					System.out.println("breakMe");
        					String paramter = ((ListView)Utils.getNodeByID("parameterListView2")).getSelectionModel().getSelectedItem().toString();
        					String input = null,className = null;
        					System.out.println("enter path to annomaly detection algorithms");
        					input = "C:\\Users\\User\\workspace\\FlightSimulatorJava\\bin";
        					//className="test.SimpleAnomalyDetector";
        					className="test.SimpleAnomalyDetector";

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
        						List<AnomalyReport> reallist = new LinkedList();

        						ZScoreAlgo h = new ZScoreAlgo();
        						if(a.getClass()!= h.getClass())
        						{
        							for(int i=0; i<list.size();i++)
        							{
        								if(list.get(i).description.contains(paramter))
        									reallist.add(list.get(i));
        							}
        						}


        						 XYChart.Series series = new XYChart.Series();
        						 for(int i=0; i<reallist.size();i++)
        						 {
        							 series.getData().add(new XYChart.Data(reallist.get(i).x, reallist.get(i).y));
        						 }
        						 ((LineChart)Utils.getNodeByID("paramGraph2")).getData().add(series);


        						// List<AnomalyReport> list =  a.detect(ano);
        					} catch (InstantiationException e) {
        						// TODO Auto-generated catch block
        						e.printStackTrace();
        					} catch (IllegalAccessException e) {
        						// TODO Auto-generated catch block
        						e.printStackTrace();
        					}




        				}


        				///////////////////////////////////////////////////////////////

        				// check the big GRAPH

        				//anomalyGraph
        				//Utils.getNodeByID("anomalyGraph");
        				/* HAGIS
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
						}
        				HAGIS */








        			/*	AnomalyDetectionAlgorithm a = new ZScoreAlgo();
        				// File reg = new File("C:\\Users\\User\\workspace\\FlightSimulatorJava\\reg_flight.csv");
        				// File ano = new File("C:\\Users\\User\\workspace\\FlightSimulatorJava\\anomaly_flight.csv");

        				File reg = new File(Main.conf.flight_data_csv);
        				File ano = new File(Main.conf.flight_data_csv);


        				 a.learnNormal(reg);
        				 List<AnomalyReport> list = a.detect(ano);
        				 XYChart.Series series = new XYChart.Series();
        				 for(int i=0; i<list.size();i++)
        				 {
        					 series.getData().add(new XYChart.Data(list.get(i).x, list.get(i).y));
        				 }
        				 ((LineChart)Utils.getNodeByID("anomalyGraph")).getData().add(series); */



        				///////////////////////////////////////////////////////////////

    				}
	        	});
	        }
	    }, 0, 100);

	    //set lists-view values
	    List<String> flightdata = new ArrayList<>();
		flightdata = Main.simcomm.getFlightDataList();
		((ListView)Utils.getNodeByID("parameterListView1")).getItems().addAll(flightdata);
		((ListView)Utils.getNodeByID("parameterListView2")).getItems().addAll(flightdata);
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

	public void onMouseClickedParameterListView1() {
		//update category axis label
		param1selected = true;
		((NumberAxis)Utils.getNodeByID("paramCategoryAxis1")).setLabel((String)(((ListView)Utils.getNodeByID("parameterListView1")).getSelectionModel().getSelectedItems().get(0)));
	}

	public void onMouseClickedParameterListView2() {
		//update category axis label
		param2selected = true;
		((NumberAxis)Utils.getNodeByID("paramCategoryAxis2")).setLabel((String)(((ListView)Utils.getNodeByID("parameterListView2")).getSelectionModel().getSelectedItems().get(0)));
	}

	@FXML
	public void exitApplication(ActionEvent event) {
	   Platform.exit(); //used to enable stop function in Main
	}

}
