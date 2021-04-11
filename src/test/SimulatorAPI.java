package test;

import java.io.IOException;
import java.net.UnknownHostException;

//Interface for flight simulator communication
public interface SimulatorAPI {
	public void start() throws UnknownHostException, IOException, InterruptedException;
	public void finalize();
	public void sendFileToSimulator(String filename) throws UnknownHostException, IOException, InterruptedException;
	public float getFlightParameter(String paramname);
	public void setSimulationSpeed(float speedmuliplayer);
}
