package pharoslabut.demo.autoIntersection;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.Vector;

import pharoslabut.logger.Logger;

/**
 * Contains the configuration of an autonomous intersection experiment.
 * 
 * @author Chien-Liang Fok
 */
public class ExpConfig {
	/**
	 * The name of the experiment.
	 */
	private String expName = "Exp";

	/**
	 * The experiment type.
	 */
	private ExpType expType = ExpType.CENTRALIZED;
	
	/**
	 * The IP address of the server.  This is only used in centrally-managed intersections.
	 */
	private InetAddress serverIP = null;
	
	/**
	 * The port of the server.  This is only used in centrally-managed intersections.
	 */
	private int serverPort = -1;
	
	/**
	 * Details of each robot in the experiment.
	 */
	private Vector<RobotExpSettings> team = new Vector<RobotExpSettings>();
	
	/**
	 * The constructor.
	 * 
	 * @param fileName The name of the file containing the experiment specifications.
	 */
	public ExpConfig(String fileName) {
		readFile(fileName);
	}
	
	/**
	 * Reads in the file containing the experiment specifications.
	 * 
	 * @param fileName The name of the file containing the experiment specifications.
	 */
	private void readFile(String fileName) {
		BufferedReader input = null;
		try {
			input =  new BufferedReader(new FileReader(fileName));
		} catch (IOException ex){
			ex.printStackTrace();
			System.err.println("Unable to open " + fileName);
			System.exit(1);
		}
		
		try {
			String line = null;
			int lineno = 1;
			while (( line = input.readLine()) != null) {
				if (!line.equals("") && !line.startsWith("//")) {
					if (line.contains("EXP_NAME")) {
						String[] elem = line.split("[\\s]+");
						try {
							expName = elem[1];
						} catch(Exception e) {
							e.printStackTrace();
							System.err.println("Syntax error on line " + lineno + " of experiment script " + fileName + ":\n" + line);
							System.exit(1);
						}
					} else if (line.contains("EXP_TYPE")) {
						String[] elem = line.split("[\\s]+");
						try {
							String expTypeStr = elem[1].toUpperCase();
							if (expTypeStr.equals("CENTRALIZED"))
								expType = ExpType.CENTRALIZED;
							else if (expTypeStr.equals("ADHOC-SERIAL"))
								expType = ExpType.ADHOC_SERIAL;
							else if (expTypeStr.equals("ADHOC-PARALLEL"))
								expType = ExpType.ADHOC_PARALLEL;
							else {
								System.err.println("Unknown experiment type " + elem[1]);
								System.exit(1);	
							}
							Logger.log("Experiment type: " + expType);
							
						} catch(Exception e) {
							e.printStackTrace();
							System.err.println("Syntax error on line " + lineno + " of experiment script " + fileName + ":\n" + line);
							System.err.println("Unknown experiment type.");
							System.exit(1);
						}
					} else if (line.contains("SERVER_IP")) {
						String[] elem = line.split("[\\s]+");
						serverIP = InetAddress.getByName(elem[1]);
					} else if (line.contains("SERVER_PORT")) {
						String[] elem = line.split("[\\s]+");
						serverPort = Integer.valueOf(elem[1]);
					} else if (line.contains("VEHICLE")) {
						try {
							String[] elem = line.split("[\\s]+");
							String name = elem[1];
							InetAddress ip = InetAddress.getByName(elem[2]);
							int port = Integer.valueOf(elem[3]);
							String entryPoint = elem[4];
							String exitPoint = elem[5];
							RobotExpSettings rs = new RobotExpSettings(name, ip, port, entryPoint, exitPoint);
							
							team.add(rs);
						} catch(Exception e) {
							e.printStackTrace();
							System.err.println("Syntax error on line " + lineno + " of experiment script " + fileName + ":\n" + line);
							System.exit(1);
						}
					}
				}
				lineno++;
			}
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		// Perform some checks to ensure experiment configuration file is valid.
		if (expType == ExpType.CENTRALIZED) {
			if (serverIP == null) {
				System.err.println("Must specify server IP!");
				System.exit(1);
			}
			if (serverPort == -1) {
				System.err.println("Must specify server port!");
				System.exit(1);
			}
		}
		
		if (getNumRobots() == 0) {
			System.err.println("Must specify at least one robot!");
			System.exit(1);
		}
		
	}
	
	public String getExpName() {
		return expName;
	}
	
	public ExpType getExpType() {
		return expType;
	}
	
	public int getNumRobots() {
		return team.size();
	}
	
	public InetAddress getServerIP() {
		return serverIP;
	}
	
	public int getServerPort() {
		return serverPort;
	}
	
	public Iterator<RobotExpSettings> getRobotItr() {
		return team.iterator();
	}
	
	public Vector<RobotExpSettings> getTeam() {
		return team;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer(getClass().getName() + "\n");
		sb.append("\tExpName = " + expName + "\n");
		if (expType == ExpType.CENTRALIZED) {
			sb.append("\tServerIP = " + serverIP + "\n");
			sb.append("\tServerPort = " + serverPort + "\n");
		}
		sb.append("\tRobots:\n");
		Iterator<RobotExpSettings> itr = team.iterator();
		while (itr.hasNext()) {
			sb.append("\t\t" + itr.next().toString() + "\n");
		}
		return sb.toString();
	}
}
