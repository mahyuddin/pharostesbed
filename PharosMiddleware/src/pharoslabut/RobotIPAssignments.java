package pharoslabut;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import pharoslabut.exceptions.PharosException;
import pharoslabut.radioMeter.cc2420.TelosBeaconException;

/**
 * Defines the last octal of each robot's wireless ad hoc IP address.
 * Provides methods for getting the robot's ID and name.
 * 
 * @author Chien-Liang Fok
 */
public class RobotIPAssignments implements java.io.Serializable {

	private static final long serialVersionUID = 5183888203934282642L;
	
	public static final int ADAMS = 16;
	public static final int ADVENTINUS = 28;
	public static final int BIGFOOT = 36;
	public static final int CHIMAY = 23;
	public static final int CZECHVAR = 14;
	public static final int FATTIRE = 12;
	public static final int FRAMBOISE = 31;
	public static final int GUINNESS = 20;
	public static final int HARP = 30;
	public static final int HOEGAARDEN = 22;
	public static final int KONA = 27;
	public static final int LONESTAR = 11;
	public static final int LIVEOAK = 26;
	public static final int MANNY = 13;
	public static final int MARDESOUS = 24;
	public static final int NEGRAMONDELO = 34;
	public static final int NEWCASTLE = 29;
	public static final int PORTERHOUSE = 19;
	public static final int PYRAMID = 33;
	public static final int REDHOOK = 15;
	public static final int SAINTARNOLD = 10;
	public static final int SALVATOR = 21;
	public static final int SKAGWAY = 37;
	public static final int SHINER = 17;
	public static final int SPATEN = 35;
	public static final int TSINGTAO = 38;
	public static final int WYNKOOP = 25;
	public static final int XUEHUA = 32;
	public static final int ZIEGEN = 18;
	
	public static final int TEST_ROBOT5 = 5;
	public static final int TEST_ROBOT4 = 4;
	public static final int TEST_ROBOT3 = 3;
	public static final int TEST_ROBOT2 = 2;
	public static final int TEST_ROBOT1 = 1;
	
	
	/**
	 * Returns the robot's ID, which is the last octal of the robot's IP address, given its name.
	 * 
	 * @param name The name of the robot
	 * @return the last octal of the robot's IP address
	 * @throws PharosException if the robot's ID cannot be found
	 */
	public static int getRobotID(String name) throws PharosException {
		Field[] fields;
		try {
			fields = Class.forName("pharoslabut.RobotIPAssignments").getDeclaredFields();
			
			// For each field in the robot IP assignment table...
			for (int i = 0; i < fields.length; i++) {
				Field currField = fields[i];
				String currFieldName = currField.getName().toLowerCase();
				
				// If the name of the log file contains the name of the robot...
				if (name.toLowerCase().equals(currFieldName)) {
					
					// Find and return the last octal of the IP address...
					return currField.getInt(null);
				}
			}
				
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		throw new PharosException("ERROR: RobotIPAssignments.getRobotIP: Unable to find ID for robot " + name);
	}
	
	/**
	 * Returns the robot's name given its ID.
	 * 
	 * @param id The id of the robot, which is the last octal of the robot's IP address
	 * @return the name of the robot
	 * @throws PharosException if the robot's name cannot be found
	 */
	public static String getRobotName(int id) throws PharosException {
		Field[] fields;
		try {
			fields = Class.forName("pharoslabut.RobotIPAssignments").getDeclaredFields();
			
			// For each field in the robot IP assignment table...
			for (int i = 0; i < fields.length; i++) {
				Field currField = fields[i];
				if (id == currField.getInt(null)) {
					return currField.getName();
				}
			}
				
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		throw new PharosException("ERROR: Unable to find name of robot with ID " + id);
	}
	
	/**
	 * Returns the wireless ad hoc IP address of this robot.  This IP address
	 * is of the form 10.11.12.*.
	 * 
	 * @return The wireless ad hoc IP address of the robot.
	 * @throws PharosException if the IP address is unknown
	 */
    public static String getAdHocIP() throws PharosException {

    	Enumeration<NetworkInterface> ifEnum;
		try {
			ifEnum = NetworkInterface.getNetworkInterfaces();
			while (ifEnum.hasMoreElements()) {
				NetworkInterface ni = ifEnum.nextElement();
				//System.out.println("network interface name = \"" + ni.getName() + "\"");
				Enumeration<InetAddress> ipEnum = ni.getInetAddresses();
				while (ipEnum.hasMoreElements()) {
					InetAddress addr = ipEnum.nextElement();
					//System.out.println("\tip address=" + addr.getHostAddress());
					if (addr.getHostAddress().contains("10.11.12")) {
						String result = addr.getHostAddress();
						//System.out.println("Found! Network interface \"" + result + "\"");
						return result;
					}
					
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		throw new PharosException("Could not find ad hoc IP address!");
    }
	
	/**
	 * Determines the ID of this robot.  The ID is the last octal in
	 * the wireless ad hoc network IP address.  The form of this IP is 
	 * 10.11.12.xx where "xx" is the ID.
	 * 
	 * @return The mote ID.
	 * @throws TelosBeaconException When the ID could not be determined.
	 */
	public static int getID() throws PharosException {
		int addr = 0;
		String ipAddr = getAdHocIP();
		
		//System.out.println("ipAddr = " + ipAddr);
		String[] addrTokens = ipAddr.split("\\.");
		if (addrTokens.length == 4)
			addr = Integer.valueOf(addrTokens[3]);
		else {
			String eMsg = "Unable to determine ID (addrTokens.length = " + addrTokens.length + ").";
			throw new PharosException(eMsg);
		}
		
		return addr;
	}
	
}
