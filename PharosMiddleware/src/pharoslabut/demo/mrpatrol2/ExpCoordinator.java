package pharoslabut.demo.mrpatrol2;

import java.util.Iterator;

import pharoslabut.demo.mrpatrol2.config.ExpConfig;
import pharoslabut.demo.mrpatrol2.config.RobotExpSettings;
import pharoslabut.demo.mrpatrol2.msgs.LoadExpSettingsMsg;
import pharoslabut.demo.mrpatrol2.msgs.StartExpMsg;
import pharoslabut.io.SetTimeMsg;
import pharoslabut.io.TCPMessageSender;
import pharoslabut.logger.Logger;

/**
 * Coordinates the initialization of a multi-robot patrol 2 (MRP2) experiment.
 * 
 * @author Chien-Liang Fok
 * @see IndoorMRPatrolServer
 */
public class ExpCoordinator {
	
	/**
     * The connection to the MRPatrol2Server(s).
     */
    private TCPMessageSender sender = TCPMessageSender.getSender();
    
    /**
     * The constructor.
     * 
     * @param expConfigFileName The name of the file containing the experiment configuration.
     * @see pharoslabut.demo.mrpatrol2.config.ExpConfig
     */
	public ExpCoordinator(String expConfigFileName) {
		doExp(expConfigFileName);
	}
	
	private void doExp(String expConfigFileName) {
		ExpConfig expConfig = new ExpConfig(expConfigFileName);
		
		try {
			
			Logger.log("Setting local time on each robot...");
			Iterator<RobotExpSettings> itr = expConfig.getRobotItr();
			while (itr.hasNext()) {
				RobotExpSettings currRobot = itr.next();
				Logger.log("\tSending SetTimeMsg to " + currRobot.getName());
				
				SetTimeMsg msg = new SetTimeMsg();
				sender.sendMessage(currRobot.getIP(), currRobot.getPort(), msg);
			}
			
			Logger.log("Sending each robot the multi-robot patrol 2 (MRP2) specifications...");
			itr = expConfig.getRobotItr();
			while (itr.hasNext()) {
				RobotExpSettings currRobot = itr.next();
				Logger.log("\tSending patrol specification to robot " + currRobot.getName());
				
				LoadExpSettingsMsg msg = 
					new LoadExpSettingsMsg(expConfig);
				sender.sendMessage(currRobot.getIP(), currRobot.getPort(), msg);
			}
			
			// Pause to ensure each robot receives the experiment settings.
			// This is to prevent out-of-order messages.
			int startTime = 5;
			Logger.log("Starting experiment in " + startTime + "...");
			while (startTime-- > 0) {
				synchronized(this) { 
					try {
						wait(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if (startTime > 0) Logger.log(startTime + "...");
			}
			
			
			// Send each robot the start experiment command.
			StartExpMsg sem = new StartExpMsg();
			itr = expConfig.getRobotItr();
			while (itr.hasNext()) {
				RobotExpSettings currRobot = itr.next();
				Logger.log("Sending StartExpMsg to robot " + currRobot.getName() + "...");
				sender.sendMessage(currRobot.getIP(), currRobot.getPort(), sem);
			}
		} catch(Exception e) {
			Logger.logErr("Problem while communicating with the robots...");
			e.printStackTrace();
		}
	}
	
	private static void print(String msg) {
		System.out.println(msg);
	}
	
	private static void usage() {
		print("Usage: " + ExpCoordinator.class.getName() + " <options>\n");
		print("Where <options> include:");
		print("\t-file <experiment configuration file name>: The name of the file containing the experiment configuration (required)");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		String expConfigFileName = null;
		
		try {
			for (int i=0; i < args.length; i++) {
		
				if (args[i].equals("-file")) {
					expConfigFileName = args[++i];
				}
				else if (args[i].equals("-debug") || args[i].equals("-d")) {
					System.setProperty ("PharosMiddleware.debug", "true");
				}
				else if (args[i].equals("-h")) {
					usage();
					System.exit(1);
				} else {
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
		print("Debug: " + ((System.getProperty ("PharosMiddleware.debug") != null) ? true : false));
		
		new ExpCoordinator(expConfigFileName);
	}
}
