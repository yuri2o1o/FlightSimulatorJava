package application;

import java.util.Observable;
import java.util.Observer;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ViewModel extends Observable implements Observer {
	FlightGearAPI m = (FlightGearAPI)Main.simcomm;
	
	//bindings
	
	//flight controls
	public IntegerProperty currentFlightTime = new SimpleIntegerProperty(0);
	public StringProperty currentFlightTimeString = new SimpleStringProperty("0");
	
	//joystick
	public FloatProperty rudderSlide = new SimpleFloatProperty(0);
	public FloatProperty throttleSlide = new SimpleFloatProperty(0);
	public FloatProperty aileronTrans = new SimpleFloatProperty(0);
	public FloatProperty elevatorTrans = new SimpleFloatProperty(0);
	
	//gauges
	public StringProperty altitudeLabel = new SimpleStringProperty("0");
	public StringProperty airspeedLabel = new SimpleStringProperty("0");
	public StringProperty headingLabel = new SimpleStringProperty("0");
	public StringProperty rollLabel = new SimpleStringProperty("0");
	public StringProperty pitchLabel = new SimpleStringProperty("0");
	public StringProperty yawLabel = new SimpleStringProperty("0");
	
	//adds the viewModel to observe the model (MVVM)
	public ViewModel() { m.addObserver(this); }
	
	//takes data from the Model and updates the data sent to the view accordingly
	@Override
	public void update(Observable o, Object arg) {
		if (o != m)
			return;
		
		//update bindings
		Platform.runLater(new Runnable() {
			@Override public void run() {
				//flight controls
				currentFlightTime.set(m.getCurrentFlightTime());
				currentFlightTimeString.set(Utils.msToTimeString((long)currentFlightTime.get()));
				
				//joystick
				float rudder = m.getFlightParameter("rudder");
				if (rudder > -999)
					rudderSlide.set(rudder);
				float throttle = m.getFlightParameter("throttle");
				if (throttle > -999)
					throttleSlide.set(throttle);
				//get flight parameter values and set them to the joystick circle's offset
				float aileron = m.getFlightParameter("aileron");
				float elevator = m.getFlightParameter("elevator");
				if (aileron > -999 && elevator > -999) {
					aileronTrans.set(aileron*150);
					elevatorTrans.set(elevator*-150);
				}
				
				//gauges
				altitudeLabel.set(m.getFlightParameter("altimeter_indicated-altitude-ft")+"");
				airspeedLabel.set(m.getFlightParameter("airspeed-indicator_indicated-speed-kt")+"");
				headingLabel.set(m.getFlightParameter("indicated-heading-deg")+"");
				rollLabel.set(m.getFlightParameter("attitude-indicator_indicated-roll-deg")+"");
				pitchLabel.set(m.getFlightParameter("attitude-indicator_internal-pitch-deg")+"");
				yawLabel.set(m.getFlightParameter("heading-deg")+"");
			}
		});
	}
	//starts flight in Model
	public void startFlight() { m.startFlight(); }
}
