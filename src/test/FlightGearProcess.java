package test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

//A proxy process for the FlightGear flight simulator
public class FlightGearProcess extends Process {
	Process flightgear;
	
	/*
	 * Starts the FlightGear process in it's own directory, with arguments to open sockets for communication to it
	 * in: path - the base path to the FlightGear simulator
	 *     inport - the port we will read flight data from
	 *     outport - the port we will send flight data to
	 */
	public FlightGearProcess(String path, int inport, int outport) throws IOException {
		ProcessBuilder pb = new ProcessBuilder(path + "/bin/fgfs.exe", "--generic=socket,in," + (FlightGearAPI.defcommdelay / 10) + ",127.0.0.1," + outport + ",tcp,playback_small", "--generic=socket,out,"  + (FlightGearAPI.defcommdelay / 10) + ",127.0.0.1," + inport + ",tcp,playback_small", "--fdm=null");
		pb.directory(new File(path + "/bin"));
		flightgear = pb.start();
	}
	
	//proxy functions
	@Override
	public OutputStream getOutputStream() {
		return flightgear.getOutputStream();
	}

	@Override
	public InputStream getInputStream() {
		return flightgear.getInputStream();
	}

	@Override
	public InputStream getErrorStream() {
		return flightgear.getErrorStream();
	}

	@Override
	public int waitFor() throws InterruptedException {
		return flightgear.waitFor();
	}

	@Override
	public int exitValue() {
		return flightgear.exitValue();
	}

	@Override
	public void destroy() {
		flightgear.destroy();
	}
	
}
