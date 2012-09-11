package pharoslabut.logger.analyzer.tcpdump;

import pharoslabut.logger.FileLogger;

/**
 * Encapsulates data from a single line in a tcpdump log file.
 * 
 * @author Chien-Liang Fok
 */
public class TCPDumpRecord {
	/**
	 * The timestamp of the packet.
	 */
	long timestamp;
	
	/**
	 * The time synchronization function time for the frame in us.
	 * @see http://www.radiotap.org/defined-fields/TSFT
	 */
	long tsft;
	
	/**
	 * The data rate at which the frame was received.
	 */
	double bitRate;
	
	/**
	 * The radio frequency used.
	 */
	int frequency;
	
	/**
	 * The received signal strength indicator in dB.
	 */
	int rssi;
	
	/**
	 * The basic service set identifier.
	 * @see http://en.wikipedia.org/wiki/Service_set_%28802.11_network%29#Basic_service_set_identifier_.28BSSID.29
	 */
	long bssid;
	
	/**
	 * The destination address.
	 */
	long destAddr;
	
	/**
	 * The source address.
	 */
	long sourceAddr;
	
	/**
	 * The name of the wireless network that the packet belongs to.
	 */
	String networkName;
	
	private FileLogger flogger;
	
	/**
	 * The constructor.
	 * 
	 * @param line A line from the tcpdump log file.
	 */
	public TCPDumpRecord(String line) throws InvalidFormatException {
		String[] tokens = line.split("\\s+");
		
		// Print the tokens for debugging...
//		for (int i=0; i < tokens.length; i++) {
//			log(i + ": " + tokens[i], flogger);
//		}
		
		// Add some checks to ensure line's format is as expected...
		if (!tokens[2].equals("tsft")) throw new InvalidFormatException("Token 2 not tsft (is " + tokens[2] + ")");
		if (!tokens[4].equals("Mb/s")) throw new InvalidFormatException("Token 3 not Mb/s (is " + tokens[4] + ")");
		if (!tokens[6].equals("MHz")) throw new InvalidFormatException("Token 6 not MHz (is " + tokens[6] + ")");
		if (!tokens[9].equals("signal")) throw new InvalidFormatException("Token 9 not signal (is " + tokens[9] + ")");
		if (!tokens[10].equals("antenna")) throw new InvalidFormatException("Token 10 not antenna (is " + tokens[10] + ")");
		if (!tokens[14].startsWith("BSSID")) throw new InvalidFormatException("Token 14 does not start with BSSID (is " + tokens[14] + ")");
		if (!tokens[15].startsWith("DA")) throw new InvalidFormatException("Token 15 does not start with DA (is " + tokens[15] + ")");
		if (!tokens[16].startsWith("SA")) throw new InvalidFormatException("Token 16 does not start with SA (is " + tokens[16] + ")");
		if (!tokens[17].equals("Beacon")) throw new InvalidFormatException("Token 17 not Beacon (is " + tokens[17] + ")");
		
		// Save the values in the line into local variables.
		timestamp = (long)(Double.valueOf(tokens[0]) * 1000);
		tsft = Long.valueOf(tokens[1].substring(0, tokens[1].indexOf("us")));
		bitRate = Double.valueOf(tokens[3]);
		frequency = Integer.valueOf(tokens[5]);
		rssi = Integer.valueOf(tokens[8].substring(0, tokens[8].length()-2));
		bssid = colonAddrToLong(tokens[14]);
		destAddr = colonAddrToLong(tokens[15]);
		sourceAddr = colonAddrToLong(tokens[16]);
		networkName = tokens[18].substring(1, tokens[18].length()-1); // remove the parenthesis surrounding the name
	}
	
	public String getNetworkName() {
		return networkName;
	}
	
	public long getSourceAddr() {
		return sourceAddr;
	}
	
	public long getDestAddr() {
		return destAddr;
	}
	
	public long getTimeStamp() {
		return timestamp;
	}
	
	public int getRSSI() {
		return rssi;
	}
	
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append(timestamp + "\t");
		result.append(tsft + "us_tsft\t");
		result.append(bitRate + "Mb/s\t");
		result.append(frequency + "MHz\t");
		result.append(rssi + "dB\t");
		result.append("BSSID:" + Long.toHexString(bssid) + "\t");
		result.append("DestAddr:" + Long.toHexString(destAddr) + "\t");
		result.append("SourceAddr:" + Long.toHexString(sourceAddr) + "\t");
		result.append(networkName + "\t");
		return result.toString();
	}
	
	/**
	 * Converts a string of the following format into a long:
	 * [name]:[byte]:[byte]:[byte]:[byte]:[byte]:[byte]
	 * 
	 * @param addr The address string.
	 * @return The long representation of the bytes.
	 */
	public static long colonAddrToLong(String addr) {
		// First split the string by the colons...
		String[] tokens = addr.split(":");
		
		long result = 0;
		for (int i=0; i < 6; i++) {
			int shiftAmt = 8 * (5-i);
			long val = Long.valueOf(tokens[i+1], 16);
			result += val << shiftAmt;
//			log("colonAddrToInt: " + i + " result = " + Long.toHexString(result) + ", val=" + Long.toHexString(val) + ", shiftAmt=" + shiftAmt, flogger);
		}
		
//		log("colonAddrToInt: Converted " + addr + " to " + Long.toHexString(result), flogger);
		
		return result;
	}
	
	private void log(String msg, FileLogger flogger) {
		System.out.println(msg);
		if (flogger != null)
			flogger.log(msg);
	}
	
	/**
	 * Sets the file logger for saving debug messages into a file.
	 * 
	 * @param flogger The file logger to use to save debug messages.
	 */
	public void setFileLogger(FileLogger flogger) {
		this.flogger = flogger;
	}
}
