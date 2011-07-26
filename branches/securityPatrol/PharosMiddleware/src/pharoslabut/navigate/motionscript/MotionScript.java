package pharoslabut.navigate.motionscript;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import pharoslabut.navigate.Location;

/**
 * Contains a list of instructions that control the movement and wireless
 * communication of the robot.
 * 
 * @author Chien-Liang Fok
 * @see pharoslabut.navigate.motionscript.Instruction
 */
public class MotionScript implements java.io.Serializable {
	private static final long serialVersionUID = -5210604518776542996L;
	
	private Vector<Instruction> instructions = new Vector<Instruction>();;
	
	/**
	 * The name of the file containing the motion script specification.
	 */
	private String fileName;
	
	/**
	 * The constructor.  Creates an empty motion script.
	 */
	public MotionScript(String fileName) {
		this.fileName = fileName;
		readMotionScript();
	}
	
	/**
	 * An accessor to the file containing the actual motion script.
	 * 
	 * @return the file containing the actual motion script.
	 */
	public String getFileName() {
		return fileName;
	}
	
	/**
	 * Returns in instruction at the specified index.  The index must be
	 * between zero and numInstructions();
	 * 
	 * @param indx The instruction index number.
	 * @return The instruction at the specified index.
	 */
	public Instruction getInstruction(int indx) {
		return instructions.get(indx);
	}
	
	/**
	 * Adds an instruction to this motion script.
	 * 
	 * @param i The instruction.
	 */
//	private void addInstruction(Instruction i) {
//		instructions.add(i);
//	}
	
	/**
	 * An accessor to the number of instructions in this motion script.
	 * 
	 * @return  the number of instructions in this motion script.
	 */
	public int numInstructions() {
		return instructions.size();
	}
	
	/**
	 * @return The number of way points in the motion script.
	 */
	public int numWayPoints() {
		int result = 0;
		Enumeration<Instruction> e = instructions.elements();
		while (e.hasMoreElements()) {
			Instruction currInst = e.nextElement();
			if (currInst.getType().equals(InstructionType.MOVE)) {
				result++;
			}
		}
		return result;
	}
	
	/**
	 * 
	 * @return the waypoints within this motion script, in the order traversed by the robot.
	 */
	public Location[] getWayPoints() {
		ArrayList<Location> result = new ArrayList<Location>();
		Enumeration<Instruction> e = instructions.elements();
		while (e.hasMoreElements()) {
			Instruction currInst = e.nextElement();
			if (currInst.getType().equals(InstructionType.MOVE)) {
				Move moveInstr = (Move)currInst;
				result.add(moveInstr.getDest());
			}
		}
		return result.toArray(new Location[result.size()]);
	}
	
	/**
	 * Reads in a motion script.  Initializes all local variables.
	 */
	private void readMotionScript() {
		try {
			BufferedReader input =  new BufferedReader(new FileReader(fileName));
			try {
				String line = null;
				int lineno = 1;
				while (( line = input.readLine()) != null) {
					if (!line.equals("")  && !line.startsWith("#")  && !line.startsWith("//")) {
						
						// Ignore comments...
						if (line.contains("//"))
							line = line.substring(0, line.indexOf("//"));
						
						try {
							if (line.contains("MOVE")) {
								String[] elem = line.split("[\\s]+");
								double latitude = Double.valueOf(elem[1]);
								double longitude = Double.valueOf(elem[2]);
								double speed = Double.valueOf(elem[3]);
								Location dest = new Location(latitude, longitude);
								instructions.add(new Move(dest, speed));
							}
							else if (line.contains("PAUSE")) {
								String[] elem = line.split("[\\s]+");
								long pauseTime = Long.valueOf(elem[1]);
								instructions.add(new Pause(pauseTime));
							}
							else if (line.contains("START_BCAST_TELOSB")) {
								String[] elem = line.split("[\\s]+");
								long minPauseTime = Long.valueOf(elem[1]);
								long maxPauseTime = Long.valueOf(elem[2]);
								short txPower = Short.valueOf(elem[3]);
								instructions.add(new StartBcastTelosB(minPauseTime, maxPauseTime, txPower));
							}
							else if (line.contains("START_BCAST_WIFI")) {
								String[] elem = line.split("[\\s]+");
								long minPauseTime = Long.valueOf(elem[1]);
								long maxPauseTime = Long.valueOf(elem[2]);
								short txPower = Short.valueOf(elem[3]);
								instructions.add(new StartBcastWiFi(minPauseTime, maxPauseTime, txPower));
							}
							else if (line.contains("STOP_BCAST_TELOSB")) {
								instructions.add(new StopBcastTelosB());
							}
							else if (line.contains("STOP_BCAST_WIFI")) {
								instructions.add(new StopBcastWifi());
							}
							else if (line.contains("WAIT_EXP_STOP")) {
								instructions.add(new WaitStopExp());
							}
							else if (line.contains("START_SCOOT")) {
								//String[] elem = line.split("[\\s]+");
								//int amount = Integer.valueOf(elem[1]);
								instructions.add(new Scoot());
							} else if (line.contains("RCV_TELOSB_BEACONS")) {
								String[] elem = line.split("[\\s]+");
								int amount = Integer.valueOf(elem[1]);
								instructions.add(new RcvTelosbBeacons(amount));	
							}
							else
								throw new UnknownInstructionException(line);
						} catch(Exception e) {
							e.printStackTrace();
							System.err.println("Error on line " + lineno + " of motion script " + fileName);
							System.exit(1);
						}
					}
					lineno++;
				}
			}
			finally {
				input.close();
			}
		}
		catch (IOException ex){
			ex.printStackTrace();
			// fatal Error
			System.exit(1);
		}
	}
	
	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append("Motion Script: Number of instructions = " + numInstructions() + "\n");
		for (int i=0; i < numInstructions(); i++) {
			buff.append((i+1) + ": " + instructions.get(i) + "\n");
		}
		return buff.toString();
	}
	
//	private class WayPoint implements java.io.Serializable {
//		private static final long serialVersionUID = -3971031374160666416L;
//		private Location loc;
//		private long pauseTime;
//		private double speed; // speed at which to go towards the way point
//		
//		public WayPoint(Location loc, long pauseTime, double speed) {
//			this.loc = loc;
//			this.pauseTime = pauseTime;
//			this.speed = speed;
//		}
//		
//		public Location getLoc() {
//			return loc;
//		}
//		
//		public long getPauseTime() {
//			return pauseTime;
//		}
//		
//		public double getSpeed() {
//			return speed;
//		}
//		
//		public String toString() {
//			return loc + ", speed=" + speed + ", pauseTime=" + pauseTime;
//		}
//	}
}
