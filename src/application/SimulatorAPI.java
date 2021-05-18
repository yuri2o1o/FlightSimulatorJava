package application;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

//Interface for flight simulator communication
public interface SimulatorAPI {
	public void start() throws UnknownHostException, IOException, InterruptedException;
	public void finalize();
	public void loadFlightDataFromCSV(String filename) throws UnknownHostException, IOException;
	public float getFlightParameter(String paramname);
	public void setSimulationSpeed(float speedmuliplayer);
	void sendFlightDataToSimulator();
	void setCurrentFlightTime(long currenttimems);
	long getFlightLength();
	long getCurrentFlightTime();
	public String[] getFlightData();
	public List<String> getFlightDataList();
	public int getFlightDataIndexByMsTime(int mstime);
}
