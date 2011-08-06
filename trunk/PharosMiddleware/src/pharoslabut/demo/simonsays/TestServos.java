package pharoslabut.demo.simonsays;

import pharoslabut.logger.*;

/**
 * This provides a basic test of the servos for panning and tilting the camera.
 * 
 * @author Chien-Liang Fok
 */
public class TestServos implements MCUConstants {
	
	FileLogger flogger = null;
	MCUInterface mcu;
	
	/**
	 * The constructor.
	 * 
	 * @param portName The comm port on which the MCU is attached.
	 */
	public TestServos(String portName, String fileName) {
		// listPorts();
		
		if (fileName != null) 
			flogger = new FileLogger(fileName);
		
		try {
			mcu = new MCUInterface(portName, flogger);
			doTest();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//System.out.println("Done performing test...");
		//System.exit(0);
	}
	
	private void doTest() {
		double MIN_PAN_ANGLE = -30;
		double CENTER_PAN_ANGLE = 0;
		double MAX_PAN_ANGLE = 30;
		
		double MIN_TILT_ANGLE = -18;
		double CENTER_TILT_ANGLE = 0;
		double MAX_TILT_ANGLE = 25;
		
		//double STEP_SIZE = 0.5;
		
		
		mcu.setCameraPan(CENTER_PAN_ANGLE);
		mcu.setCameraTilt(CENTER_TILT_ANGLE);
		
		while (true) {
			// pan to the right...
			System.out.println("Panning to the right...");
			mcu.setCameraPan(MIN_PAN_ANGLE);
			//pause(500);
			
			System.out.println("Panning to the left...");
			mcu.setCameraPan(MAX_PAN_ANGLE);
			//pause(1000);
			
			System.out.println("Panning to Center...");
			mcu.setCameraPan(CENTER_PAN_ANGLE);
			//pause(1000);
			
			System.out.println("Tilting down...");
			mcu.setCameraTilt(MIN_TILT_ANGLE);
			//pause(500);
			
			System.out.println("Tilting up...");
			mcu.setCameraTilt(MAX_TILT_ANGLE);
			//pause(1000);
			
			System.out.println("Tilting to Center...");
			mcu.setCameraTilt(CENTER_TILT_ANGLE);
			//pause(1000);
		}
	}
	
	private void pause(int milliseconds) {
		synchronized(this) {
			try {
				this.wait(milliseconds);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	

	




	
	private static void print(String msg) {
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println("PharosClient: " + msg);
	}
	
	private static void usage() {
		print("Usage: pharoslabut.demo.irobotcam.TestServos <options>");
		print("Where <options> include:");
		print("\t-comm <comm port>: The comm port on which the MCU is attached (default: /dev/ttyS0)");
		print("\t-file <file name>: The name of the file in which to save log data.");
		print("\t-debug: enable debug mode");
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String portName = "/dev/ttyS0";
		String fileName = null;
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-comm")) {
					portName = args[++i];
				}
				else if (args[i].equals("-file") ) {
					fileName = args[++i];
				}
				else if (args[i].equals("-debug") || args[i].equals("-d")) {
					System.setProperty ("PharosMiddleware.debug", "true");
				}
				else {
					System.setProperty ("PharosMiddleware.debug", "true");
					usage();
					System.exit(1);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			usage();
			System.exit(1);
		}
		
		print("Port: " + portName);
		print("File: " + fileName);
		print("Debug: " + ((System.getProperty ("PharosMiddleware.debug") != null) ? true : false));
		
		new TestServos(portName, fileName);
	}

}
