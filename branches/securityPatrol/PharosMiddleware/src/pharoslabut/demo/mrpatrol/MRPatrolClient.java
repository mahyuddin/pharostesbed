package pharoslabut.demo.mrpatrol;
import pharoslabut.experiment.ExpConfig;
import pharoslabut.experiment.ExpConfigReader;
import pharoslabut.experiment.ExpType;
import pharoslabut.experiment.RobotExpSettings;
import pharoslabut.io.*;

public class MRPatrolClient {
	/**
     * The connection to the PharosExpServer.
     */
    private TCPMessageSender sender = TCPMessageSender.getSender();
    
    public MRPatrolClient(String expConfigFileName) {
		doExp(expConfigFileName);
	}
    
    
	private void doExp(String expConfigFileName) {
		
		// ExpConfig contains a vector of RobotExpSettings, which contain a string scriptFileName,
		// which defines the behaviors of the robot.
		ExpConfig expConfig = ExpConfigReader.readExpConfig(expConfigFileName);
		try {
			
//			log("Setting local time on each robot...");
//			for (int i=0; i < expConfig.numRobots(); i++) {
//				RobotExpSettings currRobot = expConfig.getRobot(i);
//				log("\tSending SetTimeMsg to " + currRobot.getName());
//				
//				SetTimeMsg msg = new SetTimeMsg();
//				sender.sendMessage(currRobot.getIPAddress(), currRobot.getPort(), msg);
//			}
			
			log("Sending each robot their motion scripts...");
			for (int i=0; i < expConfig.numRobots(); i++) {
				RobotExpSettings currRobot = expConfig.getRobot(i);
				log("\tSending configuration data to robot " + currRobot.getName());
				
				
				// read file currRobot.getMotionScript() into String fileVar 
				// package fileVar inside message "msg"
				MRPConfigMsg myMsg = new MRPConfigMsg(currRobot.getMotionScript());
				
				
				
				// 
//				MotionScript script = MotionScriptReader.readTraceFile();
//				MotionScriptMsg msg = new MotionScriptMsg(script);
				log("\tConfiguration file:"+myMsg.GetConfigData()+"\t data type:"+myMsg.getType());
				sender.sendMessage(currRobot.getIPAddress(), currRobot.getPort(), myMsg);
			}
			
			// Pause to ensure each robot receives their motion script.
			// This is to prevent out-of-order messages.
			int startTime = 5;
			log("Starting experiment in " + startTime + "...");
			while (startTime-- > 0) {
				synchronized(this) { 
					try {
						wait(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if (startTime > 0) log(startTime + "...");
			}
			
			
			// Send each robot the start experiment command.
			int delay = 0;
			for (int i=0; i < expConfig.numRobots(); i++) {
				RobotExpSettings currRobot = expConfig.getRobot(i);
				StartExpMsg sem = new StartExpMsg(expConfig.getExpName(), ExpType.RUN_BEHAVIOR_GPS, delay);
				
				log("Sending StartExpMsg to robot " + currRobot.getName() + "...");
				sender.sendMessage(currRobot.getIPAddress(), currRobot.getPort(), sem);
				
				// Update the delay between each robot.
				delay += expConfig.getStartInterval();
			}
		} catch(Exception e) {
			logErr("Problem while communicating with the robots...");
			e.printStackTrace();
		}
	}
	
	private void logErr(String msg) {
		System.err.println("MrPatrolClient: " + msg);
	}
	
	private void log(String msg) {
		if (System.getProperty ("PharosMiddleware.debug") != null) 
			System.out.println("MrPatrolClient: " + msg);
	}
	
	private static void print(String msg) {
		System.out.println(msg);
	}
	
	private static void usage() {
		print("Usage: pharoslabut.demo.mrpatrol.MRPatrolServer <options>\n");
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
/*				else if (args[i].equals("-behaveconf")){
					behaveConfigFile = args[++i];
				}
*/				else if (args[i].equals("-debug") || args[i].equals("-d")) {
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
		
		new MRPatrolClient(expConfigFileName);
	}
}

