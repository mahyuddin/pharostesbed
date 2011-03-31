package pharoslabut;
import pharoslabut.tasks.Priority;
import pharoslabut.tasks.MotionTask;
import pharoslabut.logger.*;
import playerclient.PlayerClient;
import playerclient.PlayerException;
import playerclient.Position2DInterface;
import playerclient.Position2DListener;
import playerclient.structures.PlayerConstants;
import playerclient.structures.position2d.PlayerPosition2dData;

public class Rob_Mov implements Position2DListener {

	private PlayerClient client = null;
	private FileLogger flogger = null;
	public Rob_Mov(String serverIP, int serverPort, String fileName, boolean showGUI) {
		try {
			client = new PlayerClient(serverIP, serverPort);
			
		} catch(PlayerException e) {

			log("Error connecting to Player: ");
			log("    [ " + e.toString() + " ]");
			System.exit (1);
		}

		Position2DInterface motors = client.requestInterfacePosition2D(0, PlayerConstants.PLAYER_OPEN_MODE);
		if (motors == null) {
			log("motors is null");
			System.exit(1);
		}
	MotionArbiter motionArbiter = new MotionArbiter(MotionArbiter.MotionType.MOTION_IROBOT_CREATE, motors);
		if (fileName != null) {
			flogger = new FileLogger(fileName);
			motionArbiter.setFileLogger(flogger);
		}
		
		MotionTask currTask = null;
		motors = client.requestInterfacePosition2D(0,PlayerConstants.PLAYER_OPEN_MODE);
	//	motors.addPos2DListener(this);
//		motors.setSpeed(0.4,0);
	//	pause(1000);
		
		double speed = 0.4;
		int interval = 1000;
		
		
	/*	currTask = new MotionTask(Priority.SECOND, speed, 0);
		log("Submitting: " + currTask);
		motionArbiter.submitTask(currTask);
		pause(interval);
	*/
	//	turn(currTask,motionArbiter);
		move(1,currTask,motionArbiter);
		move(-1,currTask,motionArbiter);
	//	turn(currTask,motionArbiter,-1.142);
	//	new_move(0.4);
	/*	currTask = new MotionTask(Priority.SECOND, 0, 0.3142);
		log("Submitting: " + currTask);
		motionArbiter.submitTask(currTask);
		pause(interval);
	*/	
		currTask = new MotionTask(Priority.FIRST, MotionTask.STOP_VELOCITY, MotionTask.STOP_HEADING);
		log("Submitting: " + currTask);
		motionArbiter.submitTask(currTask);
		//motionArbiter.revokeTask(currTask);
		log("Test complete!");
		System.exit(0);
	}
	
	private void move(double distance, MotionTask currTask, MotionArbiter motionArbiter){
		// need to add 0.25 m to the magnitude of distance for calibration.
		double speed = 0.4;
		int runtime = 0;
		if (distance <0) {
			speed = speed * -1;
			distance = distance - 0.25;
			}
		else
			distance = distance + 0.25;
		runtime = (int)(distance *1000 / speed);
		currTask = new MotionTask(Priority.SECOND, speed, 0);
		log("Submitting: " + currTask);
		motionArbiter.submitTask(currTask);
		pause(runtime);
	}
	
	private void new_move(double speed)
	{
		
		MotionTask currTask = new MotionTask(Priority.SECOND, speed, 0);
	//	motionArbiter.submitTask(currTask);
		pause(1000);
	}
	
	
	private void turn(MotionTask currTask, MotionArbiter motionArbiter, double angle){
		double speed = 0.3;
		double time = 0;
		int runtime = 0;
		if (angle <0) {
			speed = speed * -1;
			}
		
		time = angle * 1000 / speed;
		runtime = (int) time;
		currTask = new MotionTask(Priority.SECOND, 0, speed);
		log("Submitting: " + currTask);
		motionArbiter.submitTask(currTask);
		pause(runtime);
	}
	
	private void turn(MotionTask currTask, MotionArbiter motionArbiter){
		turn(currTask, motionArbiter, 3.14);
	}

	private void pause(int duration) {
		synchronized(this) {
			try {
				wait(duration);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void log(String msg) {

		String result = "MotorStressTest: " + msg;
		System.out.println(result);
		if (flogger != null) {
			flogger.log(result);
		}
	}

	private static void usage() {
		System.err.println("Usage: pharoslabut.RobotMover <options>\n");
		System.err.println("Where <options> include:");
		System.err.println("\t-server <ip address>: The IP address of the Player Server (default localhost)");
		System.err.println("\t-port <port number>: The Player Server's port number (default 6665)");
		System.err.println("\t-file <file name>: name of file in which to save results (default log.txt)");
		System.err.println("\t-gui: display GUI (default not shown)");

	}

	public static void main(String[] args) {
		String fileName = "log.txt";
		String serverIP = "localhost";
		int serverPort = 6665;
		boolean showGUI = false;
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-server")) {
					serverIP = args[++i];
				} 
				
				else if (args[i].equals("-port")) {
					serverPort = Integer.valueOf(args[++i]);
				}

				else if (args[i].equals("-file")) {
					fileName = args[++i];
				}
				
				else if (args[i].equals("-gui")) {
					showGUI = true;
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

		System.out.println("Server IP: " + serverIP);
		System.out.println("Server port: " + serverPort);
		System.out.println("File: " + fileName);
		System.out.println("ShowGUI: " + showGUI);
		new Rob_Mov(serverIP, serverPort, fileName, showGUI);

	}

	@Override
	public void newPlayerPosition2dData(PlayerPosition2dData data) {
		// TODO Auto-generated method stub
		
		
	}


}