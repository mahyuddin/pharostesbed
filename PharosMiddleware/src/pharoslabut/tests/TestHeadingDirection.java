package pharoslabut.tests;

import pharoslabut.MotionArbiter;
import pharoslabut.tasks.MotionTask;
import pharoslabut.tasks.Priority;
import playerclient3.PlayerClient;
import playerclient3.PlayerException;
import playerclient3.Position2DInterface;
import playerclient3.structures.PlayerConstants;

/**
 * Tests the semantics of the heading in a MotionTask.  It shows that
 * a positive heading value means "turn left" and a negative heading means
 * "turn right".
 * 
 * @author Chien-Liang Fok
 *
 */
public class TestHeadingDirection {

	public static final void main(String[] args) {
		String serverIP = "10.11.12.20";
		int serverPort = 6665;

		PlayerClient client = null;
		try {
			client = new PlayerClient(serverIP, serverPort);
		} catch(PlayerException e) {
			System.err.println("Error connecting to Player: ");
			System.err.println("    [ " + e.toString() + " ]");
			System.exit (1);
		}

		Position2DInterface motors = client.requestInterfacePosition2D(0, PlayerConstants.PLAYER_OPEN_MODE);

		if (motors == null) {
			System.err.println("motors is null");
			System.exit(1);
		}

		MotionArbiter motionArbiter = new MotionArbiter(motors);

		MotionTask mt1 = new MotionTask(Priority.SECOND, 0, 0.5); // turn left
		motionArbiter.submitTask(mt1);
		try {
			synchronized(mt1) {
				mt1.wait(1000*5);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		MotionTask mt3 = new MotionTask(Priority.SECOND, 0, -0.5); // turn right
		motionArbiter.submitTask(mt3);
		try {
			synchronized(mt3) {
				mt3.wait(1000*5);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		MotionTask mt2 = new MotionTask(Priority.SECOND, MotionTask.STOP_SPEED, MotionTask.STOP_HEADING);
		motionArbiter.submitTask(mt2);
		System.exit(0);
	}
}
