package pharoslabut;

import pharoslabut.experiment.ExpConfig;
import pharoslabut.experiment.ExpConfigReader;
import pharoslabut.experiment.RobotExpSettings;
import pharoslabut.io.*;

/**
 * Sends a StopExpMsg to each robot in an experiment.
 * 
 * @author Chien-Liang Fok
 * @see pharoslabut.io.StopExpMsg
 */
public class StopExpClient {
	
    /**
     * The constructor.
     * 
     * @param expConfigFileName The name of the file containing the experiment configuration.
     * @see pharoslabut.experiment.ExpConfig
     */
	public StopExpClient(String expConfigFileName) {
		TCPMessageSender sender = new TCPMessageSender();
		ExpConfig expConfig = ExpConfigReader.readExpConfig(expConfigFileName);
		
		StopExpMsg stopMsg = new StopExpMsg();
		
		// Send each of the robots their motion script.
		for (int i=0; i < expConfig.numRobots(); i++) {
			RobotExpSettings currRobot = expConfig.getRobot(i);
			log("Sending StopExpMsg to robot " + currRobot.getName());
			sender.sendMessage(currRobot.getIPAddress(), currRobot.getPort(), stopMsg);
		}
	}
	
	private void log(String msg) {
		if (System.getProperty ("PharosMiddleware.debug") != null) 
			System.out.println("StopExperiment: " + msg);
	}
	
	private static void print(String msg) {
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println("StopExperiment: " + msg);
	}
	
	private static void usage() {
		print("Usage: pharoslabut.StopExpClient <options>\n");
		print("Where <options> include:");
		print("\t-file <experiment configuration file name>: The name of the file containing the experiment configuration (required)");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		String expConfigFileName = null;
		String mCastAddress = "230.1.2.3";
		int mCastPort = 6000;
		
		try {
			for (int i=0; i < args.length; i++) {
		
				if (args[i].equals("-file")) {
					expConfigFileName = args[++i];
				} else if (args[i].equals("-mCastAddress")) {
					mCastAddress = args[++i];
				}
				else if (args[i].equals("-mCastPort")) {
					mCastPort = Integer.valueOf(args[++i]);
				}
				else if (args[i].equals("-debug") || args[i].equals("-d")) {
					System.setProperty ("PharosMiddleware.debug", "true");
				}
				else {
					System.setProperty ("PharosMiddleware.debug", "true");
					print("Unknown parameter: " + args[i]);
					usage();
					System.exit(1);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			usage();
			System.exit(1);
		}
		
		if (expConfigFileName == null) {
			usage();
			System.exit(1);
		}
		
		print("Exp Config: " + expConfigFileName);
		print("Multicast Address: " + mCastAddress);
		print("Multicast Port: " + mCastPort);
		print("Debug: " + ((System.getProperty ("PharosMiddleware.debug") != null) ? true : false));
		
		new StopExpClient(expConfigFileName);
	}
}
