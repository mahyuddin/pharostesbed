package pharoslabut.experiment;

import pharoslabut.exceptions.PharosException;
import pharoslabut.io.*;
import pharoslabut.logger.Logger;

/**
 * This is a client to the PharosExpServer that sends a StopExpMsg to each robot in an experiment.
 * 
 * @author Chien-Liang Fok
 * @see pharoslabut.io.StopExpMsg
 * @see PharosExpServer
 */
public class PharosStopExpClient {
	
	/**
     * The connection to the PharosExpServer.
     */
    private TCPMessageSender sender = TCPMessageSender.getSender();
    
    /**
     * The constructor.
     * 
     * @param expConfigFileName The name of the file containing the experiment configuration.
     * @see pharoslabut.experiment.ExpConfig
     */
	public PharosStopExpClient(String expConfigFileName) {
		ExpConfig expConfig = ExpConfigReader.readExpConfig(expConfigFileName);
		
		StopExpMsg stopMsg = new StopExpMsg();
		
		// Send each robot a stop experiment message...
		for (int i=0; i < expConfig.numRobots(); i++) {
			RobotExpSettings currRobot = expConfig.getRobot(i);
			Logger.log("Sending StopExpMsg to robot " + currRobot.getName());
			try {
				sender.sendMessage(currRobot.getIPAddress(), currRobot.getPort(), stopMsg);
			} catch (PharosException e) {
				Logger.logErr("Failed to send stop message to robot: " + currRobot.getName());
				e.printStackTrace();
			}
		}
	}
//	
//	private void logErr(String msg) {
//		System.err.println("PharosStopExpClient: " + msg);
//	}
//	
//	private void log(String msg) {
//		if (System.getProperty ("PharosMiddleware.debug") != null) 
//			System.out.println("PharosStopExpClient: " + msg);
//	}
	
	private static void printErr(String msg) {
		System.err.println(msg);
	}
	
	private static void print(String msg) {
		System.out.println(msg);
	}
	
	private static void usage() {
		print("Usage: " + PharosStopExpClient.class.getName() + " <options>\n");
		print("Where <options> include:");
		print("\t-expConfig <experiment configuration file name>: The name of the file containing the experiment configuration (required)");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		String expConfig = null;
		
		try {
			for (int i=0; i < args.length; i++) {
		
				if (args[i].equals("-expConfig")) {
					expConfig = args[++i];
				}
				else if (args[i].equals("-debug") || args[i].equals("-d")) {
					System.setProperty ("PharosMiddleware.debug", "true");
				}
				else {
					printErr("Unknown parameter: " + args[i]);
					usage();
					System.exit(1);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			usage();
			System.exit(1);
		}
		
		if (expConfig == null) {
			printErr("ERROR: Exp config file not set.");
			usage();
			System.exit(1);
		}
		
		print("Exp Config: " + expConfig);
		print("Debug: " + ((System.getProperty ("PharosMiddleware.debug") != null) ? true : false));
		
		new PharosStopExpClient(expConfig);
	}
}
