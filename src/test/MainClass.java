package test;

import java.io.IOException;

public class MainClass {
	public static void main(String[] args) {
		//start simulator
		System.out.println("Connecting to flight simulator...");
		SimulatorAPI simcomm;
		try {
			simcomm = new SimulatorAPI("config.xml");
			simcomm.init();
		} catch (IOException | InterruptedException e1) {
			e1.printStackTrace();
			return;
		}
		
		System.out.println("Connected.");
		System.out.println("Starting flight emulation...");
		//use API to send flight data (via thread)
		Thread flightdataouthread = new Thread(()->{
			try {
				simcomm.sendFileToSimulator("reg_flight.csv");
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		});
		flightdataouthread.start();
		
		System.out.println("Recieving data:");
		//test the API by constantly printing our altitude, meanwhile testing changing the simulation speed
		while (simcomm.getFlightParameter("altitude-ft") < 250)
			System.out.println(simcomm.getFlightParameter("altitude-ft"));
		
		simcomm.conf.playback_speed_multiplayer = 5;
		while (simcomm.getFlightParameter("altitude-ft") < 500)
			System.out.println(simcomm.getFlightParameter("altitude-ft"));
		
		simcomm.conf.playback_speed_multiplayer = 0.2f;
		while (simcomm.getFlightParameter("altitude-ft") < 550)
			System.out.println(simcomm.getFlightParameter("altitude-ft"));
		
		//close everything
		flightdataouthread.stop();
		simcomm.finalize();
	}
}