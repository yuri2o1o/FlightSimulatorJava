package test;

import java.io.IOException;

public class MainClass {
	public static void main(String[] args) {
		//start simulator API
		System.out.println("Connecting to flight simulator...");
		SimulatorAPI simcomm = new SimulatorAPI();
		try {
			simcomm.init();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		System.out.println("Connected.");
		System.out.println("Starting flight emulation...");
		//use API to send flight data (via thread)
		new Thread(()->{
			try {
				SimulatorAPI.sendFileToSimulator("reg_flight.csv");
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}).start();
		
		System.out.println("Recieving data:");
		//test the API by constantly printing our altitude (should update every 100 ms)
		while (true)
			System.out.println(simcomm.altitude);
	}
}