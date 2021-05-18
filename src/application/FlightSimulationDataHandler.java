package application;

public class FlightSimulationDataHandler {
	private SocketIO simout;
	private String[] flightdata;
	
	private long flightlenms = 0; //the length of the flight in ms
	private long currenttimems = 0; //the current flight time in ms
	private float timecorrection = 0; //inner value used to convert times to match the sample rate
	private int timejumpms = FlightGearAPI.defcommdelay; //the amount of ms to skip each iteration (used for flight speed change)
	
	public FlightSimulationDataHandler(SocketIO nsimout, String[] nflightdata, int samplerate, float speedmultiplayer) {
		simout = nsimout;
		flightdata = nflightdata;
		timecorrection = (float)FlightGearAPI.defcommdelay / (float)samplerate;
		timejumpms *= speedmultiplayer * timecorrection;
		flightlenms = flightdata.length * samplerate;
	}
	
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
	
	public void setFlightSpeed(float speedmultiplayer) {
		timejumpms = (int)(FlightGearAPI.defcommdelay * (speedmultiplayer*timecorrection));
	}
	
	public long getFlightLength() {
		return flightlenms;
	}
	
	public void setCurrentFlightTime(long ncurrenttimems) {
		currenttimems = ncurrenttimems;
	}
	
	public long getCurrentFlightTime() {
		return currenttimems;
	}
	
	public String[] getFlightData() {
		return flightdata;
	}
	
	public void close() { simout.close(); }
}
