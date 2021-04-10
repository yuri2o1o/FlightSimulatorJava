package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;
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

//class the functions as an API for communication with the FlightGear flight simulator
public class SimulatorAPI {
	public Config conf;
	private static final int defcommdelay = 100; //the default delay (in ms) for communication with the simulator
	private SocketIO simin = null;
	
	private Process simulator;
	private Thread datainthread;
	
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
			try { Thread.sleep((int)(defcommdelay*(1/conf.playback_speed_multiplayer))); } catch (InterruptedException e) { continue; }
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
	
	/*
	 * Function to start the FlightGear simulator
	 */
	private void startSimulator() throws UnknownHostException, IOException, InterruptedException {
		copyPlaybackToSimulator();
		//start the simulator process in it's own directory, with arguments to open sockets for communication to it
		ProcessBuilder pb = new ProcessBuilder(conf.simulator_path + "/bin/fgfs.exe", "--generic=socket,in,2,127.0.0.1," + conf.simulator_output_port + ",tcp,playback_small", "--generic=socket,out,2,127.0.0.1," + conf.simulator_input_port + ",tcp,playback_small", "--fdm=null");
		pb.directory(new File(conf.simulator_path + "/bin"));
		simulator = pb.start();
	}
	
	public SimulatorAPI(String configxmlpath) throws FileNotFoundException {
		conf = Config.readConfigFromXML("config.xml");
		initFlightDataFromXML();
	}
	
	/*
	 * Init function that waits for simulator connection and starts the updateSimDataAgent
	 */
	public void init() throws UnknownHostException, IOException, InterruptedException {
		startSimulator(); //start the simulator
		
		//open server socket, wait for simulator to finish starting, than start the update thread
		simin = new SocketIO(conf.simulator_input_port);
		Thread.sleep(conf.init_sleep_seconds*1000);
		datainthread = new Thread(()->updateSimDataAgent());
		datainthread.start();
	}
	
	/*
	 * Finalizes the API (releases sockets and threads)
	 */
	public void finalize() {
		datainthread.stop();
		simin.close();
		simulator.destroy();
	}
	
	/*
	 * Utility function used to send flight data from a flight data CSV to the simulator
	 * in: filename - the name of the CSV file to send
	 * out (via socket to simulator): the given CSV's data
	 */
	public void sendFileToSimulator(String filename) throws UnknownHostException, IOException, InterruptedException {
		//open socket to simulator out port
		SocketIO simout = new SocketIO("localhost", conf.simulator_output_port);
		BufferedReader in = new BufferedReader(new FileReader(filename));
		
		//read each line from the file and send it to the simulator (via socket)
		String line;
		while((line = in.readLine()) != null) {
			simout.writeln(line);
			Thread.sleep((int)(defcommdelay*(1/conf.playback_speed_multiplayer)));
		}
		
		in.close();
		simout.close();
	}
	
	/*
	 * Function to get a flight parameter by name (as written in the playback XML)
	 * in: name - the name of the parameter
	 * out: float - the current (real-time) value of the parameter
	 */
	public float getFlightParameter(String name) {
		for (FlightParam param : flightdata)
			if (param.name.equals(name))
				return param.value;
		return 0;
	}
}
