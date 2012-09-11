package pharoslabut.tests;

import pharoslabut.navigate.MotionArbiter;
import pharoslabut.navigate.MotionArbiter.MotionType;
import pharoslabut.tasks.MotionTask;
import pharoslabut.tasks.Priority;
import playerclient3.PlayerClient;
import playerclient3.PlayerException;
import playerclient3.Position2DInterface;
import playerclient3.structures.PlayerConstants;

/**
 * Tests the semantics of the heading in a MotionTask on the Traxxas mobility plane.  It shows that
 * a positive heading value means "turn left" and a negative heading means
 * "turn right".
 * 
 * @author Chien-Liang Fok
 *
 */
public class TestHeadingDirection {

	private static void usage() {
		System.err.println("Usage: " + TestHeadingDirection.class.getName() + " <options>\n");
		System.err.println("Where <options> include:");
		System.err.println("\t-server <ip address>: The IP address of the Player Server (default localhost)");
		System.err.println("\t-port <port number>: The Player Server's port number (default 6665)");
	}
	
	public static final void main(String[] args) {
		String serverIP = "10.11.12.1";
		int serverPort = 6665;
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-server")) {
					serverIP = args[++i];
				} 
				else if (args[i].equals("-port")) {
					serverPort = Integer.valueOf(args[++i]);
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

		System.setProperty ("PlayerClient.debug", "true");
		System.out.println("Launching TestHeadingDirection...");
		
		PlayerClient client = null;
		
		System.out.println("Connecting to player server " + serverIP + ":" + serverPort + "...");
		try {
			client = new PlayerClient(serverIP, serverPort);
		} catch(PlayerException e) {
			System.err.println("Error connecting to Player: ");
			System.err.println("    [ " + e.toString() + " ]");
			System.exit (1);
		}
		
		// For some reason, we need to get the Position2DInterface *before* changing the player server mode to PUSH 
		// or to make it run in threaded mode.  If we do not do this, the following method call will hang indefinately.
		System.out.println("Subscribing to Position2DInterface...");
		Position2DInterface motors = client.requestInterfacePosition2D(0 /* device index */, PlayerConstants.PLAYER_OPEN_MODE);
		
		if (motors == null) {
			System.err.println("Motors is null");
			System.exit(1);
		}
		
		// The runThreaded and change of data delivery mode can be done in reverse order
		// without breaking this test.
		System.out.println("Setting Player Client to run in continuous threaded mode...");
		client.runThreaded(-1, -1);
		
		System.out.println("Changing Player server mode to PUSH...");
		client.requestDataDeliveryMode(playerclient3.structures.PlayerConstants.PLAYER_DATAMODE_PUSH);

		MotionTask mt;
		
		System.out.println("Creating MotionArbiter...");
		MotionArbiter motionArbiter = new MotionArbiter(MotionType.MOTION_TRAXXAS, motors);

		System.out.println("Turning the front wheels left...");
		mt = new MotionTask(Priority.SECOND, 0, 0.5); // turn left
		motionArbiter.submitTask(mt);
		
		try {
			synchronized(mt) {
				mt.wait(1000*5);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("Turning the front wheels right...");
		mt = new MotionTask(Priority.SECOND, 0, -0.5); // turn right
		motionArbiter.submitTask(mt);
		try {
			synchronized(mt) {
				mt.wait(1000*5);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("Centering the front wheels...");
		mt = new MotionTask(Priority.SECOND, MotionTask.STOP_SPEED, MotionTask.STOP_STEERING_ANGLE);
		motionArbiter.submitTask(mt);
		
		System.out.println("End of test...");
		System.exit(0);
	}
}
