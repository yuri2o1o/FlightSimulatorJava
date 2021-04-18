package simpack;

import java.io.IOException;
import java.net.UnknownHostException;

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
}
