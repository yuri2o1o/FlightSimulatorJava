package application;

public class FlightSimulationDataHandler {
	private SocketIO simout;
	private String[] flightdata;
	
	private int flightlenms = 0; //the length of the flight in ms
	private int currenttimems = 0; //the current flight time in ms
	private float timecorrection = 0; //inner value used to convert times to match the sample rate
	private int timejumpms = FlightGearAPI.defcommdelay; //the amount of ms to skip each iteration (used for flight speed change)
	
	/*
	sets parameters - timejumpms is the new time to jump to after speed and time correction changes,
	and flightlenms is the duration of the flight
	*/
	public FlightSimulationDataHandler(SocketIO nsimout, String[] nflightdata, int samplerate, float speedmultiplayer) {
		simout = nsimout;
		flightdata = nflightdata;
		timecorrection = (float)FlightGearAPI.defcommdelay / (float)samplerate;
		timejumpms *= speedmultiplayer * timecorrection;
		flightlenms = flightdata.length * samplerate;
	}
	
	//returns index from the flight data according to given time in flight(in ms)
	public int getFlightDataIndexByMsTime(long mstime) {
		return (int)((mstime/FlightGearAPI.defcommdelay)*timecorrection);
	}
	
	/*
	 * Agent function used to send flightdata to the simulator (should be ran as thread)
	 * in: filename - the name of the CSV file to send
	 * out (via socket to simulator): the given CSV's data
	 */
	public void sendFlightDataToSimulatorAgent() throws InterruptedException {
		for (; currenttimems < flightlenms; currenttimems += timejumpms) {
			simout.writeln(flightdata[getFlightDataIndexByMsTime(currenttimems)]);
			Thread.sleep(FlightGearAPI.defcommdelay);
		}
	}
	
	//setters and getters- 
	public void setFlightSpeed(float speedmultiplayer) {
		timejumpms = (int)(FlightGearAPI.defcommdelay * (speedmultiplayer*timecorrection));//calculated by the same formula as used before
	}
	
	public int getFlightLength() {
		return flightlenms;
	}
	
	public void setCurrentFlightTime(int ncurrenttimems) {
		currenttimems = ncurrenttimems;
	}
	
	public int getCurrentFlightTime() {
		return currenttimems;
	}
	
	public String[] getFlightData() {
		return flightdata;
	}
	//closes the socket to the simulator
	public void close() { simout.close(); }
}
