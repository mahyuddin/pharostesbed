package pharoslabut.demo.autoIntersection;

import java.util.Iterator;

import pharoslabut.demo.autoIntersection.msgs.LoadExpSettingsMsg;
import pharoslabut.demo.autoIntersection.msgs.StartAdHocExpMsg;
import pharoslabut.io.SetTimeMsg;
import pharoslabut.io.TCPMessageSender;
import pharoslabut.logger.Logger;

/**
 * This starts an autonomous intersection experiment.  It 
 * reads an experiment configuration file, then connects to each
 * vehicle participating in the experiment informing them of the experiment
 * settings and when to start the experiment.
 * 
 * @author Chien-Liang Fok
 */
public class AutoIntersectionExpMgr {
	/**
     * The connection to the AutoIntersectionClient(s).
     */
    private TCPMessageSender sender = TCPMessageSender.getSender();
    
    /**
     * The constructor.
     * 
     * @param expConfigFileName The name of the file containing the experiment configuration.
     * @see pharoslabut.experiment.ExpConfig
     * @see pharoslabut.navigate.motionscript.MotionScriptReader
     */
	public AutoIntersectionExpMgr(String expConfigFileName) {
		doExp(expConfigFileName);
	}
	
	private void doExp(String expConfigFileName) {
		ExpConfig expConfig = new ExpConfig(expConfigFileName);
		
		try {
			
			Logger.log("Setting local time on each vehicle...");
			Iterator<RobotExpSettings> itr = expConfig.getRobotItr();
			while (itr.hasNext()) {
				RobotExpSettings currRobot = itr.next();
				Logger.log("\tSending SetTimeMsg to " + currRobot.getName());
				
				SetTimeMsg msg = new SetTimeMsg();
				sender.sendMessage(currRobot.getIP(), currRobot.getPort(), msg);
			}
			
			Logger.log("Sending each vehicle the autonomous intersection its specifications...");
			itr = expConfig.getRobotItr();
			while (itr.hasNext()) {
				RobotExpSettings currRobot = itr.next();
				Logger.log("\tSending intersection experiment specifications to " + currRobot.getName());
				
				LoadExpSettingsMsg msg = 
					new LoadExpSettingsMsg(currRobot.getEntryPointID(), currRobot.getExitPointID());
				sender.sendMessage(currRobot.getIP(), currRobot.getPort(), msg);
			}
			
			// Pause to ensure each vehicle receives their experiment specifications
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
			
			
			// Send each vehicle a start experiment command.
			StartAdHocExpMsg sem = new StartAdHocExpMsg(expConfig.getExpName(),
					expConfig.getExpType());
			itr = expConfig.getRobotItr();
			while (itr.hasNext()) {
				RobotExpSettings currRobot = itr.next();
				Logger.log("Sending StartExpMsg to vehicle " + currRobot.getName() + "...");
				sender.sendMessage(currRobot.getIP(), currRobot.getPort(), sem);
			}
		} catch(Exception e) {
			Logger.logErr("Problem while communicating with the vehicles...");
			e.printStackTrace();
		}
	}
	
	private static void print(String msg) {
		System.out.println(msg);
	}
	
	private static void usage() {
		print("Usage: " + AutoIntersectionExpMgr.class.getName() + " <options>\n");
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
		print("Debug: " + ((System.getProperty ("PharosMiddleware.debug") != null) ? true : false));
		
		new AutoIntersectionExpMgr(expConfigFileName);
	}
}
