package pharoslabut.experiment;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Pings each robot to make sure it is alive.
 * 
 * @author Chien-Liang Fok
 *
 */
public class RobotPingTest {

	public RobotPingTest(String expConfigFileName) {
		ExpConfig expConfig = ExpConfigReader.readExpConfig(expConfigFileName);
		try {
			// Ping each of the robots
			for (int i=0; i < expConfig.numRobots(); i++) {
				RobotExpSettings currRobot = expConfig.getRobot(i);
				log("Pinging robot " + currRobot.getName());
				
				 try {
			            Runtime rt = Runtime.getRuntime();
			            String pingCmd = "ping " + currRobot.getIPAddress().getHostAddress();
			            log("Ping command: " + pingCmd);
			            
			            Process pr = rt.exec(pingCmd);

			            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));

			            String line=null;
			            boolean done = false;
			            
			            int lineno = 0;
			            int numTimeouts = 0;
			            while((line=input.readLine()) != null && !done) {
			                if (line.contains("timeout"))
			                	numTimeouts++;
			                else if (line.contains("64 bytes from")){
			                	log(currRobot.getName() + " OK");
			                	done = true;
			                }
			                
			                if (numTimeouts == 4) {
			                	System.err.println("Unable to ping robot " + currRobot.getName() + " (" + currRobot.getIPAddress() + ")");
			                	done = true;
			                }
			            }

			            pr.destroy();
			            //int exitVal = pr.waitFor();
			            //System.out.println("Exited with error code "+exitVal);
			        } catch(Exception e) {
			        	String eMsg = "Unable to run ping: " + e.toString();
			            System.err.println(eMsg);
			            System.exit(1);
			        }
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void log(String msg) {
		//if (System.getProperty ("PharosMiddleware.debug") != null) 
			System.out.println("RobotPingTest: " + msg);
	}
	
	private static void print(String msg) {
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println("RobotPingTest: " + msg);
	}
	
	private static void usage() {
		print("Usage: pharoslabut.experiment.RobotPingTest <options>\n");
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
		
		new RobotPingTest(expConfigFileName);
	}
}
