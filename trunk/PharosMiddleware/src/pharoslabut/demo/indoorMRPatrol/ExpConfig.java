package pharoslabut.demo.indoorMRPatrol;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.Vector;

/**
 * Contains the configuration of a simple multi-robot patrol.
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
	private ExpType expType = ExpType.UNCOORDINATED;
	
	/**
	 * The number of markers in the patrol path.
	 */
	private int numMarkers = 0;
	
	/**
	 * The distance between markers along the patrol path.
	 */
	private double markerDist = 0;
	
	/**
	 * Details of each robot in the team.
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
							if (expTypeStr.equals("UNCOORDINATED"))
								expType = ExpType.UNCOORDINATED;
							else if (expTypeStr.equals("LOOSELY"))
								expType = ExpType.LOOSELY;
							else if (expTypeStr.equals("LABELED"))
								expType = ExpType.LABELED;
							else if (expTypeStr.equals("BLOOMIER"))
								expType = ExpType.BLOOMIER;
							else {
								System.err.println("Unknown experiment type " + elem[1]);
								System.exit(1);	
							}
							
						} catch(Exception e) {
							e.printStackTrace();
							System.err.println("Syntax error on line " + lineno + " of experiment script " + fileName + ":\n" + line);
							System.exit(1);
						}
					} else if (line.contains("TEAM_MEMBER")) {
						try {
							String[] elem = line.split("[\\s]+");
							String name = elem[1];
							InetAddress ip = InetAddress.getByName(elem[2]);
							int port = Integer.valueOf(elem[3]);
							double startingLoc = Double.valueOf(elem[4]);
							RobotExpSettings rs = new RobotExpSettings(name, ip, port, startingLoc);
							
							team.add(rs);
						} catch(Exception e) {
							e.printStackTrace();
							System.err.println("Syntax error on line " + lineno + " of experiment script " + fileName + ":\n" + line);
							System.exit(1);
						}
					} else if (line.contains("NUM_MARKERS")) {
						String[] elem = line.split("[\\s]+");
						numMarkers = Integer.valueOf(elem[1]);
					} else if (line.contains("MARKER_SEPARATION")) {
						String[] elem = line.split("[\\s]+");
						markerDist = Double.valueOf(elem[1]);
					}
				}
				lineno++;
			}
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getExpName() {
		return expName;
	}
	
	public ExpType getExpType() {
		return expType;
	}
	
	public int getNumMarkers() {
		return numMarkers;
	}
	
	public double getMarkerDist() {
		return markerDist;
	}
	
	public int getNumRobots() {
		return team.size();
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
		sb.append("\tNumMarkers = " + numMarkers + "\n");
		sb.append("\tMarkerDist = " + markerDist + "\n");
		sb.append("\tRobots:\n");
		Iterator<RobotExpSettings> itr = team.iterator();
		while (itr.hasNext()) {
			sb.append("\t\t" + itr.next().toString() + "\n");
		}
		return sb.toString();
	}
}
