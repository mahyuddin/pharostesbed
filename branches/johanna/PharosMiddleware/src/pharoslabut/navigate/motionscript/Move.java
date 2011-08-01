package pharoslabut.navigate.motionscript;

import pharoslabut.navigate.Location;

/**
 * An instruction to move the robot.
 * 
 * @author Chien-Liang Fok
 */
public class Move extends Instruction {

	private static final long serialVersionUID = -2950728105674846546L;

	/**
	 * The destination location.
	 */
	private Location dest;
	
	/**
	 * The speed at which to go towards the way point
	 */
	private double speed;
	
	/**
	 * The constructor.
	 * 
	 * @param dest The destination location.
	 * @param speed The speed in meters per second at which to move.
	 */
	public Move(Location dest, double speed) {
		this.dest = dest;
		this.speed = speed;
	}
	
	public Location getDest() {
		return dest;
	}
	
	public double getSpeed() {
		return speed;
	}
	
	@Override
	public InstructionType getType() {
		return InstructionType.MOVE;
	}

	@Override
	public boolean isCompatibleWith(Instruction instr) {
		
		// The only instructions not compatible with MOVE are PAUSE, MOVE, and SCOOT.
		switch(instr.getType()) {
		case PAUSE: 
			return false;
		case SCOOT:
			return false;
		case MOVE:
			return false;
		default:
			return true;
		}
	}
	
	public String toString() {
		return "MOVE to " + dest + " at " + speed + "m/s";
	}

}
