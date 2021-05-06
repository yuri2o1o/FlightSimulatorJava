package simpack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

//Class that contains the functions as an API for communication with the FlightGear flight simulator
public class FlightGearAPI implements SimulatorAPI {
	public Config conf;
	public static final int defcommdelay = 50; //the default delay (in ms) for communication with the simulator
	private SocketIO simin = null;
	
	private Process simulator;
	private Thread datainthread;
	
	private FlightSimulationDataHandler datahandler;
	private Thread dataoutthread;
	
	//simulation data (updates in real time - readonly)
	private List<FlightParam> flightdata = new ArrayList<>();
	
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
		File playback = new File(conf.simulator_playback);
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
		File sourceFile = new File(conf.simulator_playback);
		File destFile = new File(conf.simulator_path + "/data/protocol/playback_small.xml");
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
	
	public FlightGearAPI(Config nconf) throws IOException {
		conf = nconf;
		initFlightDataFromXML();
		copyPlaybackToSimulator();
	}
	
	/*
	 * Init function that waits for simulator connection and starts the updateSimDataAgent
	 */
	@Override
	public void start() throws UnknownHostException, IOException, InterruptedException {
		//start the simulator
		simulator = new FlightGearProcess(conf.simulator_path, conf.simulator_input_port, conf.simulator_output_port);
		
		//open server socket, wait for simulator to finish starting, than start the update thread
		simin = new SocketIO(conf.simulator_input_port);
		Thread.sleep(conf.init_sleep_seconds*1000);
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
	}
	
	/*
	 * Function to load CSV flight data to the system
	 * in: filename - the name of the CSV file to send
	 */
	@Override
	public void loadFlightDataFromCSV(String filename) throws UnknownHostException, IOException {
		//open socket to simulator out port
		SocketIO simout = new SocketIO("localhost", conf.simulator_output_port);
		
		//read all lines from the CSV and instantiate datahandler
		datahandler = new FlightSimulationDataHandler(simout, Files.readString(Path.of(filename)).split("\n"), conf.playback_sample_rate_ms, conf.playback_speed_multiplayer);
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
	
	@Override
	public void setSimulationSpeed(float speedmuliplayer) {
		datahandler.setFlightSpeed(speedmuliplayer);
	}
	
	@Override
	public void setCurrentFlightTime(long currenttimems) {
		datahandler.setCurrentFlightTime(currenttimems);
	}
	
	@Override
	public long getFlightLength() {
		return datahandler.getFlightLength();
	}
	
	@Override
	public long getCurrentFlightTime() {
		return datahandler.getCurrentFlightTime();
	}
}
