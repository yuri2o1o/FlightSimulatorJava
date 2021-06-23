package application;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

//Interface for flight simulator communication
//defines all functionality needed for a class to communicate with a flight simulator
//in our case, the class FlightGearAPI implements this interface to connect and communicate to the FlightGear program
public interface SimulatorAPI {
	public void start() throws UnknownHostException, IOException, InterruptedException;
	public void finalize();
	public void loadFlightDataFromCSV(String filename) throws UnknownHostException, IOException;
	public float getFlightParameter(String paramname);
	public void setSimulationSpeed(float speedmuliplayer);
	public void sendFlightDataToSimulator();
	public void setCurrentFlightTime(int currenttimems);
	public int getFlightLength();
	public int getCurrentFlightTime();
	public String[] getFlightData();
	public List<String> getFlightDataList();
	public int getFlightDataIndexByMsTime(int mstime);
	public int getFlightParameterIndex(String paramname);
	public void startFlight();
}