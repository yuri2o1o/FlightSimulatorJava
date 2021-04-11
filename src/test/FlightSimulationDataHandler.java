package test;

public class FlightSimulationDataHandler {
	private SocketIO simout;
	private String[] flightdata;
	
	private long flightlenms = 0; //the length of the flight in ms
	private long currenttimems = 0; //the current flight time in ms
	private int timejumpms = FlightGearAPI.defcommdelay; //the amount of ms to skip each iteration (used for flight speed change)
	
	public FlightSimulationDataHandler(SocketIO nsimout, String[] nflightdata, float speedmultiplayer) {
		simout = nsimout;
		flightdata = nflightdata;
		timejumpms /= (1/speedmultiplayer);
		flightlenms = flightdata.length * FlightGearAPI.defcommdelay;
	}
	
	/*
	 * Agent function used to send flightdata to the simulator (should be ran as thread)
	 * in: filename - the name of the CSV file to send
	 * out (via socket to simulator): the given CSV's data
	 */
	public void sendFlightDataToSimulatorAgent() throws InterruptedException {
		for (; currenttimems < flightlenms; currenttimems += timejumpms) {
			simout.writeln(flightdata[(int)(currenttimems/FlightGearAPI.defcommdelay)]);
			Thread.sleep(FlightGearAPI.defcommdelay);
		}
	}
	
	public void setFlightSpeed(float speedmultiplayer) {
		timejumpms = (int)(FlightGearAPI.defcommdelay / (1/speedmultiplayer));
	}
	
	public long getFlightLength() {
		return flightlenms;
	}
	
	public void setCurrentFlightTime(long ncurrenttimems) {
		currenttimems = ncurrenttimems;
	}
	
	public void close() { simout.close(); }
}
