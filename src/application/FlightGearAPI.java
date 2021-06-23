package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javafx.application.Platform;
import javafx.scene.control.Alert;

//Class that contains the functions as an API for communication with the FlightGear flight simulator
public class FlightGearAPI extends Observable implements SimulatorAPI {
	public static final int defcommdelay = 50; //the default delay (in ms) for communication with the simulator
	private SocketIO simin = null;

	private Process simulator;//proxy process to communicate with the FlightGear simulator
	private Thread datainthread;

	private FlightSimulationDataHandler datahandler;//variable that handles the data between the simulator and our program
	private Thread dataoutthread;

	//simulation data (updates in real time - readonly)
	private  List<FlightParam> flightdata = new ArrayList<>();
	
	private Timer flightimer = new Timer();

	// Added for the controller to HS
	public List<String> getFlightDataList()
	{
		 List<String> flightdata = new ArrayList<>();
		 for(int i=0; i<this.flightdata.size();i++)
			 flightdata.add(this.flightdata.get(i).name);

		 return flightdata;
	}
	
	//starts the flight by loading it from the CSV file and sending the data to the simulator
	public void startFlight() 
	{ 
		System.out.println("Starting flight emulation...");
		//use API to send flight data
		try {
			loadFlightDataFromCSV(Main.conf.flight_data_csv);
			sendFlightDataToSimulator();
		} catch (IOException e) {
			new Alert(Alert.AlertType.ERROR, "ERROR: Could not send flight data XML to simulator").showAndWait();
			return;
		}
		
		//used for state updates, in order to reduce lag, just change every 100 ms, instead of every time there is an actual change
		flightimer.scheduleAtFixedRate(new TimerTask() {
	        @Override
	        public void run() {
	        	setChanged();
	        	notifyObservers();
	        }
	    }, 0, 100);
	}

	/*
	 * Reads flight parameters from socket into flightdata
	 * out (via flightdata): float[] - the flight parameters by order (as written in playback XML)
	 */
	private void updateSimParamDataFromSocket() {
		String[] line = simin.readln().split(","); //read a parameter line from the socket and split it
		for (int i = 0; i < line.length; i++) {
			try {
				flightdata.get(i).value = Float.parseFloat(line[i]); //parse the parameters as ordered in playback XML
			} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) { flightdata.get(i).value = -9999; };
		}
	}


	/*
	 * Agent function to handle real-time updates for the flight parameters (should be run as thread)
	 */
	private void updateSimDataAgent() {
		while (true) {
			updateSimParamDataFromSocket();
			try { Thread.sleep(defcommdelay); } catch (InterruptedException e) { continue; }
		}
	}

	/*
	 * Function that initiates the flightdata list (via the playback XML)
	 */
	private void initFlightDataFromXML() {
		//read playback xml into Document and parse it
		File playback = new File(Main.conf.simulator_playback);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		Document doc;
		try {
			db = dbf.newDocumentBuilder();
			doc = db.parse(playback);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			return;
		}
		doc.getDocumentElement().normalize();

		//go over all chunks in output
		NodeList nodeList = ((Element)(doc.getElementsByTagName("output").item(0))).getElementsByTagName("chunk");
		for (int i = 0; i < nodeList.getLength(); i++)
		{
			Node node = nodeList.item(i);
			Element e = (Element) node;
			//for each chunk add a FlightParam to the list (with the chunk's name and an initial value)
			flightdata.add(new FlightParam(e.getElementsByTagName("name").item(0).getTextContent(), -9999));
		}
	}

	/*
	 * Function that copies a playback file from the project's directory to the simulators protocol path
	 */
	private void copyPlaybackToSimulator() throws IOException {
		//create the files
		File sourceFile = new File(Main.conf.simulator_playback);
		File destFile = new File(Main.conf.simulator_path + "/data/protocol/playback_small.xml");
		if (!sourceFile.exists())
	        return;
	    if (!destFile.exists())
	        destFile.createNewFile();

	    //transfer the data
	    FileChannel source = null;
	    FileChannel destination = null;
	    source = new FileInputStream(sourceFile).getChannel();
	    destination = new FileOutputStream(destFile).getChannel();
	    if (destination != null && source != null)
	        destination.transferFrom(source, 0, source.size());

	    //close the files
	    if (source != null)
	        source.close();
	    if (destination != null)
	        destination.close();
	}

	public FlightGearAPI() throws IOException {
		initFlightDataFromXML();
		copyPlaybackToSimulator();
	}

	/*
	 * Init function that waits for simulator connection and starts the updateSimDataAgent
	 */
	@Override
	public void start() throws UnknownHostException, IOException, InterruptedException {
		//start the simulator
		simulator = new FlightGearProcess(Main.conf.simulator_path, Main.conf.simulator_input_port, Main.conf.simulator_output_port);

		//open server socket, wait for simulator to finish starting, than start the update thread
		simin = new SocketIO(Main.conf.simulator_input_port);
		Thread.sleep(Main.conf.init_sleep_seconds*1000);
		datainthread = new Thread(()->updateSimDataAgent());
		datainthread.start();
	}

	/*
	 * Finalizes the API (releases sockets and threads)
	 */
	@Override
	public void finalize() {
		dataoutthread.stop();
		datahandler.close();
		datainthread.stop();
		simin.close();
		simulator.destroy();
		flightimer.cancel();
	}

	/*
	 * Function to load CSV flight data to the system
	 * in: filename - the name of the CSV file to send
	 */
	@Override
	public void loadFlightDataFromCSV(String filename) throws UnknownHostException, IOException {
		//open socket to simulator out port
		SocketIO simout = new SocketIO("localhost", Main.conf.simulator_output_port);

		//read all lines from the CSV and instantiate datahandler
		datahandler = new FlightSimulationDataHandler(simout, new String (Files.readAllBytes(Paths.get(filename))).split("\n"), Main.conf.playback_sample_rate_ms, Main.conf.playback_speed_multiplayer);
	}

	/*
	 * Function to send (preloaded) flight data to the simulator
	 */
	@Override
	public void sendFlightDataToSimulator() {
		dataoutthread = new Thread(()->{
			try {
				datahandler.sendFlightDataToSimulatorAgent();
			} catch (InterruptedException e) {
				return;
			}
		});
		dataoutthread.start();
	}



	/*
	 * Function to get a flight parameter by name (as written in the playback XML)
	 * in: paramname - the name of the parameter
	 * out: float - the current (real-time) value of the parameter
	 */
	@Override
	public float getFlightParameter(String paramname) {
		for (FlightParam param : flightdata)
			if (param.name.equals(paramname))
				return param.value;
		return 0;
	}
	
	/*
	 * Function to get a flight parameter's index by name (as written in the playback XML)
	 * in: paramname - the name of the parameter
	 * out: float - the index of the parameter from all flight data
	 */
	@Override
	public int getFlightParameterIndex(String paramname) {
		for (int i = 0; i < flightdata.size(); i++)
			if (flightdata.get(i).name.equals(paramname))
				return i;
		return -1;
	}
	
	//sets simulation's play speed 
	@Override
	public void setSimulationSpeed(float speedmuliplayer) {
		datahandler.setFlightSpeed(speedmuliplayer);
	}

	//sets current time in flight (to jump to different parts of the flight)
	@Override
	public void setCurrentFlightTime(int currenttimems) {
		datahandler.setCurrentFlightTime(currenttimems);
	}

	
	//returns duration of flight
	@Override
	public int getFlightLength() {
		return datahandler.getFlightLength();
	}

	//returns current time in the flight
	@Override
	public int getCurrentFlightTime() {
		return datahandler.getCurrentFlightTime();
	}

	//reeturns all flight data
	@Override
	public String[] getFlightData() {
		return datahandler.getFlightData();
	}

	//returns the right index from the data based on the time given(in ms)
	@Override
	public int getFlightDataIndexByMsTime(int mstime) {
		return datahandler.getFlightDataIndexByMsTime(mstime);
	}
}
