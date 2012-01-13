/**
 * This is a simple test program that commands the robot to rotate in place
 * to the left and then to the right.
 */

package pharoslabut.demo.simonsays.scratch;

import pharoslabut.demo.simonsays.CreateRobotInterface;

public class SimpleTest {

// When interfacing with CreateRobotInterface, use "localhost" on port 6665.
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("*** Starting ***");
		
		System.out.println("Creating interface");
		CreateRobotInterface robot = new CreateRobotInterface("localhost", 6665);
		
		System.out.println("Turning left");
		robot.turn(Math.PI / 4);
		
		System.out.println("Turning right");
		robot.turn(-Math.PI / 4);
		
		System.out.println("*** Done ***");
	}
}
