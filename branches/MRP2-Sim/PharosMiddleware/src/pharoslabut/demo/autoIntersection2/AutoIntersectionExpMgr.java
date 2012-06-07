package pharoslabut.demo.autoIntersection2;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import pharoslabut.exceptions.PharosException;
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
    
    private Vector<RobotExpSetting> robots = new Vector<RobotExpSetting>();
    
    /**
     * The constructor.
     * 
     */
	public AutoIntersectionExpMgr() {
		try {
			robots.add(new RobotExpSetting("Czechvar", InetAddress.getByName("10.11.12.14"), 7776));
			robots.add(new RobotExpSetting("Ziegen", InetAddress.getByName("10.11.12.18"), 7776));
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
			System.exit(1);
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
		StartExpMsg sem = new StartExpMsg();

		Enumeration<RobotExpSetting> itr = robots.elements();
		while (itr.hasMoreElements()) {
			RobotExpSetting currRobot = itr.nextElement();
			Logger.log("Sending StartExpMsg to vehicle " + currRobot.getName() + "...");
			try {
				sender.sendMessage(currRobot.getAddr(), currRobot.getPort(), sem);
			} catch (PharosException e) {
				e.printStackTrace();
			}
		}

		// Pause 5 seconds to ensure messages were all sent
		Logger.log("Pausing 5 seconds to ensure all messages were sent.");
		synchronized(this) {
			try {
				this.wait(5000);
			} catch (InterruptedException e) {				
				e.printStackTrace();
			}
		}
		Logger.log("Shutting down...");
	}

	class RobotExpSetting {
		private String name;
		private InetAddress address;
		private int port;
		
		public RobotExpSetting(String name, InetAddress address, int port) {
			this.name = name;
			this.address = address;
			this.port = port;
		}
		
		public String getName() {
			return name;
		}
		
		public InetAddress getAddr() {
			return address;
		}
		
		public int getPort() {
			return port;
		}
	}
		
	
	private static void print(String msg) {
		System.out.println(msg);
	}
	
	private static void usage() {
		print("Usage: " + AutoIntersectionExpMgr.class.getName() + " <options>\n");
		print("Where <options> include:");
//		print("\t-file <experiment configuration file name>: The name of the file containing the experiment configuration (required)");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
//		String expConfigFileName = null;
		
		try {
			for (int i=0; i < args.length; i++) {
		
//				if (args[i].equals("-file")) {
//					expConfigFileName = args[++i];
//				}
//				else 
				if (args[i].equals("-debug") || args[i].equals("-d")) {
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
		
//		if (expConfigFileName == null) {
//			usage();
//			System.exit(1);
//		}
//		
//		print("Exp Config: " + expConfigFileName);
		print("Debug: " + ((System.getProperty ("PharosMiddleware.debug") != null) ? true : false));
		
		new AutoIntersectionExpMgr(); //expConfigFileName);
	}
}