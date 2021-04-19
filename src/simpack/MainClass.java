package simpack;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class MainClass {
	public static void main(String[] args) {
		//start simulator
		System.out.println("Connecting to flight simulator...");
		SimulatorAPI simcomm;
		try {
			simcomm = new FlightGearAPI("config.xml");
			simcomm.start();
		} catch (IOException | InterruptedException e1) {
			System.out.println("ERROR: Could not read config / playback XML");
			return;
		}
		
		System.out.println("Connected.");
		System.out.println("Starting flight emulation...");
		//use API to send flight data
		try {
			simcomm.loadFlightDataFromCSV("reg_flight.csv");
			System.out.println("Flight length is: " + Utils.msToTimeString(simcomm.getFlightLength()));
			simcomm.sendFlightDataToSimulator();
		} catch (IOException e) {
			System.out.println("ERROR: Could not send flight data XML to simulator");
		}
		
		try { Thread.sleep(3*1000); } catch (InterruptedException e) {} //sleep 3 seconds before start so we can read the output
		
		System.out.println("Recieving data:");
		//test the API by constantly printing our altitude, meanwhile testing changing the simulation speed
		while (simcomm.getFlightParameter("altitude-ft") < 200)
			System.out.println(simcomm.getFlightParameter("altitude-ft"));
		
		//increase speed and test the API
		simcomm.setSimulationSpeed(5);
		while (simcomm.getFlightParameter("altitude-ft") < 600)
			System.out.println(simcomm.getFlightParameter("altitude-ft"));
		
		//decrease speed and test the API
		simcomm.setSimulationSpeed(0.2f);
		while (simcomm.getFlightParameter("altitude-ft") < 650)
			System.out.println(simcomm.getFlightParameter("altitude-ft"));
		
		simcomm.setSimulationSpeed(1); //reset time to normal
		simcomm.setCurrentFlightTime(180*1000); //fast forward to 3:00
		while (simcomm.getFlightParameter("altitude-ft") > 150)
			System.out.println(simcomm.getFlightParameter("altitude-ft"));
		
		//close everything
		simcomm.finalize();
	}
}