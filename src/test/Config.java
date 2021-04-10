package test;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class Config {
	//global parameters
	public String simulator_path;
	public String simulator_playback;
	
	//argument parameters
	public float playback_speed_multiplayer;
	public int simulator_input_port;
	public int simulator_output_port;
	
	//specific parameters
	public int init_sleep_seconds;
	
	/*
	 * Reads a Config from an XML using XMLDecoder
	 * in: xmlpath - the path to the xml file
	 * out: Config - the read Config
	 */
	public static Config readConfigFromXML(String xmlpath) throws FileNotFoundException {
		XMLDecoder decoder = new XMLDecoder(new FileInputStream(xmlpath));
		return (Config) decoder.readObject();
	}
	
	/*
	 * Writes a Config to an XML using XMLEncoder
	 * in: conf - the Config object to be saved to an XML
	 * 	   xmlpath - the path to the xml file
	 */
	public static void saveConfigToXML(Config conf, String xmlpath) throws FileNotFoundException {
		XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(xmlpath)));
		encoder.writeObject(conf);
		encoder.close();
	}
}
