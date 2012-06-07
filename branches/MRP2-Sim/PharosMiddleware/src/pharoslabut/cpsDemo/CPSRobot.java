package pharoslabut.cpsDemo;


import static pharoslabut.cpsAssert.CPSAssertSensor.*;
import pharoslabut.cpsAssert.CPSAssertSensor;
import pharoslabut.cpsAssert.Inequality;
import pharoslabut.exceptions.NoNewDataException;
import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.MotionArbiter;


public class CPSRobot {

	/**
	 * This is for controlling the movements of the robot.
	 */
	private CreateRobotInterface ri;
	

	
	
	public CPSRobot(String pServerIP, int pServerPort, int port, String mcuPort, 
			String cricketFile, String cricketSerialPort, MotionArbiter.MotionType mobilityPlane) {
		
		ri = new CreateRobotInterface(pServerIP, pServerPort);
		
		new CPSAssertSensor(ri.getPlayerClient(), null, 
					true, null, null, cricketSerialPort, "cricketBeacons.txt",
					false, null, null, null, 
					false, null, null, null,
					true, null, 100, "/home/ut/localization.txt",
					false, null, null, null);
		
		sampleAssertionRoutine(ri);
	}
	
	
	
	private void sampleAssertionRoutine(CreateRobotInterface ri) {
		ri.move(1.0);
		
		pause(3000);
		
		try {
			AssertCameraLocalization(0.0, 1.0, Inequality.EQUAL_TO, Inequality.EQUAL_TO, 0.05, 0.05, true);
		} catch (NoNewDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Pauses the calling thread for the specified duration.
	 * 
	 * @param duration The time to pause the thread in milliseconds.
	 */
	private void pause(int duration) {
		synchronized(this) {
			try {
				wait(duration);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	
		
	
	private static void usage() {
		System.out.println("Usage: " + CPSRobot.class.getName() + " <options>\n");
		System.out.println("Where <options> include:");
		System.out.println("\t-pServer <ip address>: The IP address of the Player Server (default localhost)");
		System.out.println("\t-pPort <port number>: The Player Server's port number (default 6665)");
		System.out.println("\t-port <port number>: The Server's port number (default 8887)");
		System.out.println("\t-mcuPort <port name>: The serial port on which the MCU is attached (default /dev/ttyS0)");
		System.out.println("\t-cameraIP <camera IP address>: The IP address of the camera (default 192.168.0.20)");
		System.out.println("\t-mobilityPlane <traxxas|segway|create>: The type of mobility plane being used (default traxxas)");
		System.out.println("\t-log <file name>: name of file in which to save results (default CPSRobot.log)");
		System.out.println("\t-cricketFile <file name>: name of file where Cricket Beacon IDs and coordinates are stored (default cricketBeacons.txt)");
		System.out.println("\t-cricketPort <port number>: tty port where the Cricket Listener is connected (default /dev/ttyUSB1");
		System.out.println("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		int port = 8887;
		String pServerIP = "localhost";
		int pServerPort = 6665;
		String logFile = "CPSRobot.log";
		String mcuPort = "/dev/ttyS0";
		String cricketFile = "cricketBeacons.txt";
		String cricketSerialPort = "/dev/ttyUSB1";
		MotionArbiter.MotionType mobilityPlane = MotionArbiter.MotionType.MOTION_IROBOT_CREATE;
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-pServer")) {
					pServerIP = args[++i];
				}
				else if (args[i].equals("-pPort")) {
					pServerPort = Integer.valueOf(args[++i]);
				}
				else if (args[i].equals("-port")) {
					port = Integer.valueOf(args[++i]);
				}
				else if (args[i].equals("-log")) {
					logFile = args[++i];
				}
				else if (args[i].equals("-mcuPort")) {
					mcuPort = args[++i];
				}
				else if (args[i].equals("-cricketFile")) {
					cricketFile = args[++i];
				}
				else if (args[i].equals("-cricketPort")) {
					cricketSerialPort = args[++i];
				}
				else if (args[i].equals("-debug") || args[i].equals("-d")) {
					System.setProperty ("PharosMiddleware.debug", "true");
				}
				else if (args[i].equals("-mobilityPlane")) {
					String mp = args[++i].toLowerCase();
					if (mp.equals("traxxas"))
						mobilityPlane = MotionArbiter.MotionType.MOTION_TRAXXAS;
					else if (mp.equals("segway"))
						mobilityPlane = MotionArbiter.MotionType.MOTION_SEGWAY_RMP50;
					else if (mp.equals("create"))
						mobilityPlane = MotionArbiter.MotionType.MOTION_IROBOT_CREATE;
					else {
						System.err.println("Unknown mobility plane " + mp);
						usage();
						System.exit(1);
					}
				}
				else {
					usage();
					System.exit(1);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			usage();
			System.exit(1);
		}
		
		// Create the file logger if necessary...
		Logger.setFileLogger(new FileLogger(logFile));
		
		new CPSRobot(pServerIP, pServerPort, port, mcuPort, cricketFile, cricketSerialPort, mobilityPlane);
	}
}
