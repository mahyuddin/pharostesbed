package pharoslabut;

import java.lang.reflect.Field;

/**
 * Defines the last octal of each robot's wireless ad hoc IP address.
 * 
 * @author Chien-Liang Fok
 *
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
	
	
	/**
	 * Returns the robot's ID, which is the last octal of the robot's IP address, given its name.
	 * 
	 * @param name The name of the robot
	 * @return the last octal of the robot's IP address, or -1 if IP address is unknown.
	 */
	public static int getRobotID(String name) {
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
		
		System.err.println("ERROR: RobotIPAssignments.getRobotIP: Unable to find ID for robot " + name);
		new Exception().printStackTrace();
		return -1;
	}
	
	/**
	 * Returns the robot's name given its ID.
	 * 
	 * @param id The id of the robot, which is the last octal of the robot's IP address
	 * @return the name of the robot, or null if unknown.
	 */
	public static String getRobotName(int id) {
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
		
		System.err.println("ERROR: Unable to find name of robot with ID " + id);
		new Exception().printStackTrace();
		return null;
	}
	
}
