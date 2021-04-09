package test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Scanner;

//class the functions as an API for communication with the FlightGear flight simulator
public class SimulatorAPI {
	private static final String simulatorIp = "localhost";
	private static final int siminport = 6400;
	private static final int simoutport = 5400;
	private static final int simulationdelay = 100;
	private SocketIO simio = null;
	
	//simulation data (updates in real time - readonly)
	//flight controls
	public float aileron = 0;
	public float elevator = 0;
	public float rudder = 0; 
	
	//engines
	public float throttle1 = 0;
	public float throttle2 = 0;
	
	//position
	public float altitude = 0;
	
	//orientation
	public float roll = 0;
	public float pitch = 0;
	public float heading = 0;
	
	//velocities
	public float airspeed = 0;
	
	//prefixes
	private float[] paramdata = new float[10];
	private static final int[] prefixes = { 0, 1, 2, 6, 7, 16, 17, 18, 19, 21 };
	
	/*
	 * reads flight parameters from socket into paramdata
	 * out (via paramdata): float[] - the (hard-coded) flight parameters by order (also hard-coded)
	 */
	private void updateSimParamDataFromSocket() {
		String[] line = simio.readln().split(","); //read a parameter line from the socket and split it
		for (int i = 0; i < paramdata.length; i++)
			paramdata[i] = Float.parseFloat(line[prefixes[i]]); //parse the parameters by taking the offset (hard-coded, as ordered in playback FlightGear config XML)
	}
	
	/*
	 * for ease of access - assign paramdata values to easy-to-use public members (hard-coded)
	 */
	private void updateSimParameters() {
		//flight controls
		aileron = paramdata[0];
		elevator = paramdata[1];
		rudder = paramdata[2];
		
		//engines
		throttle1 = paramdata[3];
		throttle2 = paramdata[4];
		
		//position
		altitude = paramdata[5];
		
		//orientation
		roll = paramdata[6];
		pitch = paramdata[7];
		heading = paramdata[8];
		
		//velocities
		airspeed = paramdata[9];
	}
	
	/*
	 * agent function to handle real-time updates for the flight parameters (should be run as thread)
	 */
	private void updateSimDataAgent() {
		while (true) {
			updateSimParamDataFromSocket();
			updateSimParameters();
			try { Thread.sleep(simulationdelay); } catch (InterruptedException e) { continue; }
		}
	}
	
	/*
	 * init function that waits for simulator connection and starts the updateSimDataAgent
	 */
	public void init() throws UnknownHostException, IOException {
		simio = new SocketIO(siminport);
		new Thread(()->updateSimDataAgent()).start();
	}
	
	/*
	 * finalizes the API (releases sockets and data)
	 */
	public void finalize() {
		simio.close();
	}
	
	/*
	 * utility function used to send flight data from a flight data CSV to the simulator
	 * in: filename - the name of the CSV file to send
	 * out (via socket to simulator): the given CSV's data
	 */
	public static void sendFileToSimulator(String filename) throws UnknownHostException, IOException, InterruptedException {
		//open socket to simulator out port
		SocketIO comm = new SocketIO(simulatorIp, simoutport);
		BufferedReader in = new BufferedReader(new FileReader(filename));
		
		//read each line from the file and send it to the simulator (via socket)
		String line;
		while((line = in.readLine()) != null) {
			comm.writeln(line);
			Thread.sleep(simulationdelay);
		}
		
		in.close();
		comm.close();
	}
}
