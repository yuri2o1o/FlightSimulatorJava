package test;

import java.io.IOException;

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
		//use API to send flight data (via thread)
		Thread flightdataouthread = new Thread(()->{
			try {
				simcomm.sendFileToSimulator("reg_flight.csv");
			} catch (IOException | InterruptedException e) {
				System.out.println("ERROR: Could not send flight data XML to simulator");
			}
		});
		flightdataouthread.start();
		
		System.out.println("Recieving data:");
		//test the API by constantly printing our altitude, meanwhile testing changing the simulation speed
		while (simcomm.getFlightParameter("altitude-ft") < 250)
			System.out.println(simcomm.getFlightParameter("altitude-ft"));
		
		simcomm.setSimulationSpeed(5);
		while (simcomm.getFlightParameter("altitude-ft") < 500)
			System.out.println(simcomm.getFlightParameter("altitude-ft"));
		
		simcomm.setSimulationSpeed(0.2f);
		while (simcomm.getFlightParameter("altitude-ft") < 550)
			System.out.println(simcomm.getFlightParameter("altitude-ft"));
		
		//close everything
		flightdataouthread.stop();
		simcomm.finalize();
	}
}