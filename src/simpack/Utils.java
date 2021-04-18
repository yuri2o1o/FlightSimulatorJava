package simpack;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Utils {
	/*
	 * Parses a time in ms to an HH:MM:SS format
	 * in: ms - the time in ms
	 * out: String - the parsed HH:MM:SS string
	 */
	public static String msToTimeString(long ms) {
		SimpleDateFormat df = (new SimpleDateFormat("HH:mm:ss"));
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		return df.format((new Date(ms)));
	}
}
