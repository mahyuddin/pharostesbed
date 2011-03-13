package pharoslabut.navigate.motionscript;

import java.util.Vector;

/**
 * This contains a list of instructions that control the movement and wireless
 * communication of the robot.
 * 
 * @author Chien-Liang Fok
 * @see pharoslabut.navigate.motionscript.Instruction
 */
public class MotionScript implements java.io.Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5210604518776542996L;
	
	private Vector<Instruction> instructions = new Vector<Instruction>();;
	
	/**
	 * The constructor.  Creates an empty motion script.
	 */
	public MotionScript() {}
	
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
	public void addInstruction(Instruction i) {
		instructions.add(i);
	}
	
	/**
	 * An accessor to the number of instructions in this motion script.
	 * 
	 * @return  the number of instructions in this motion script.
	 */
	public int numInstructions() {
		return instructions.size();
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
