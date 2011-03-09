      package pharoslabut;

      import pharoslabut.MotionArbiter;

      import pharoslabut.tasks.Priority;

      import pharoslabut.tasks.MotionTask;

      import pharoslabut.logger.*;

      import playerclient.PlayerClient;

      import playerclient.PlayerException;

      import playerclient.Position2DInterface;

      import playerclient.structures.PlayerConstants;

      public class RobotMover {

      	private PlayerClient client = null;

      	private FileLogger flogger = null;
      	
      	protected Position2DInterface motors = null;
      	
      	public RobotMover(String serverIP, int serverPort,	String fileName, boolean showGUI) {

      		try {

      			client = new PlayerClient(serverIP, serverPort);

      		} catch(PlayerException e) {

      			log("Error connecting to Player: ");

      			log("    [ " + e.toString() + " ]");

      			System.exit (1);

      		}

      		 motors = client.requestInterfacePosition2D(0, PlayerConstants.PLAYER_OPEN_MODE);

      		if (motors == null) {

      			log("motors is null");

      			System.exit(1);

      		}

      		MotionArbiter motionArbiter = new MotionArbiter(MotionArbiter.MotionType.MOTION_IROBOT_CREATE, motors);

      		if (fileName != null) {
      			flogger = new FileLogger(fileName);

      			motionArbiter.setFileLogger(flogger);


      		}
      		turnLeft();
      		pause (10000);
      		turnRight();
      		pause (1000);
      		stop();
/*
//      		MotionTask currTask;

      		double speed = 0.5;
      		double rotation = .5;
//      		currTask = new MotionTask(Priority.SECOND, speed, rotation);
//      		log("Submitting: " + currTask);
//      		motionArbiter.submitTask(currTask);
      		motors.setSpeed(speed,0);
      		pause(1000);
      		
      		System.out.println("Turning robot...");
      		motors.setSpeed(0,.5);
      		pause(2000);

//      		currTask = new MotionTask(Priority.SECOND, MotionTask.STOP_VELOCITY, MotionTask.STOP_HEADING);

//      		log("Submitting: " + currTask);
//      		motionArbiter.submitTask(currTask);

      		System.out.println("Stopping robot...");
      		motors.setSpeed(0,0);
      		log("Test complete!");
      		System.exit(0);	
      		*/
      	}
  
      	public void turnLeft()
      	{
      		motors.setSpeed(0, .2);
      	}
      	
      	public void turnRight()
      	{
      		motors.setSpeed(0, -.2);
      	}
      	
      	public void moveForward()
      	{
      		motors.setSpeed(.01, 0);
      	}
      	
      	public void stop()
      	{
      		motors.setSpeed(0, 0);
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
  /*    	public static void main(String[] args) {
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

      		new RobotMover(serverIP, serverPort, fileName, showGUI);

      	}
*/
      }