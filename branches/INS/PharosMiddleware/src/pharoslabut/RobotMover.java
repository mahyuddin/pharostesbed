      package pharoslabut;

      import pharoslabut.MotionArbiter;

      import pharoslabut.tasks.Priority;

      import pharoslabut.tasks.MotionTask;

      import pharoslabut.logger.*;

import playerclient.NoNewDataException;
      import playerclient.PlayerClient;
import playerclient.Position2DListener;

      import playerclient.PlayerException;

      import playerclient.Position2DInterface;

import playerclient.structures.PlayerConstants;
import playerclient.structures.PlayerPose;
import playerclient.structures.position2d.PlayerPosition2dData;

      public class RobotMover implements Position2DListener {

      	private PlayerClient client = null;
      	private FileLogger flogger = null;
      	
		protected double Xpos;
		protected double Ypos;
		protected double Yaw;
		protected double BaseX;
		protected double BaseY;
		protected double BaseYaw;
      	
      	public Position2DInterface motors = null;
      	
      	public RobotMover(String serverIP, int serverPort,	String fileName, boolean showGUI) {

      		try {

      			client = new PlayerClient(serverIP, serverPort);
      			

      		} catch(PlayerException e) {

      			log("Error connecting to Player: ");

      			log("    [ " + e.toString() + " ]");

      			System.exit (1);

      		}

      		 motors = client.requestInterfacePosition2D(0, PlayerConstants.PLAYER_OPEN_MODE);
      		 motors.addPos2DListener(this);
      		if (motors == null) {

      			log("motors is null");

      			System.exit(1);

      		}

      		MotionArbiter motionArbiter = new MotionArbiter(MotionArbiter.MotionType.MOTION_IROBOT_CREATE, motors);

      		if (fileName != null) {
      			flogger = new FileLogger(fileName);

      			motionArbiter.setFileLogger(flogger);


      		}
      		
      		//INS data
      		//TODO: insert reset INS value to zero
				Xpos = 0;
				Ypos = 0;
				Yaw = 0;
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
      	
      	public void spinRobot(int time){
      		motors.setSpeed(0,1);
      		for(int i= 0; i<10000; i++)
      		try {
				this.wait(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
      		motors.setSpeed(.1, 0);
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
/*      	public static void main(String[] args) {
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

      	} */

		@Override
		public void newPlayerPosition2dData(PlayerPosition2dData data) {
			//System.out.println("movement");
			PlayerPose pose = data.getPos(); 
			Xpos = BaseX +pose.getPx();
			Ypos = BaseY +pose.getPy();
			Yaw =  BaseYaw + pose.getPa();
			
			//
			//position updates and other stuff in here
			
			// TODO Auto-generated method stub
			
		}
		

		public double INS_UpdateX(){
			/*try {
				Xpos = motors.getX();
			} catch (NoNewDataException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			return Xpos;
			
		}
		
		public double INS_UpdateY(){
			/*try {
				Ypos = motors.getY();
			} catch (NoNewDataException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			return Ypos;
			
		}
		
		public double INS_UpdateOrient(){
			return Yaw;
		}
		
		public String Orient(){
			double Yaw;
			try {
				Yaw = motors.getYaw();
				if(Yaw<45){
					return "E";
				}
				else if(Yaw<90)
				{return "NE"; 
				 }
				else if(Yaw==90)
				{return "N";
				}
				
				else if(Yaw<135){
					return "NW";
				}
				else if(Yaw==180){
					return "W";
				}
				else if(Yaw<225){
					return "SW";
				}
				else if(Yaw==270){
					return "S";
				}
				else if(Yaw < 315){
					return "SE";
				}
				else return "You're lost";
				
			} catch (NoNewDataException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			  return "failure";
			
		}
		
		public void ManUpdateINS(double n_X, double n_Y, double n_Yaw)
		{
			Xpos = n_X;
			Ypos = n_Y;
			Yaw = n_Yaw;
		}
		
		private double calc_dist(double o_x, double n_x, double o_y, double n_y )
		{
			double distance;
			distance = (n_x - o_x)*(n_x - o_x);
			distance += (n_y - o_y)*(n_y - o_y);
			return distance;
		}
		
		private double calc_rad(double o_H, double n_H)
		{
			return Math.abs((n_H%(2*Math.PI)) - (o_H%(2*Math.PI)));
		}
		
		public void FB_Mov(RoboMov mov_cmd)
		{
			//0 Forward, 1 Backward, 2 TurnCW, 3 TurnCCW 4 Stop
			double orig_X = Xpos, orig_Y = Ypos, orig_H = Yaw;
			double movAmt;
			
			switch(mov_cmd.MovType)
			{
				case 0:
					movAmt = calc_dist(orig_X, mov_cmd.goalWaypoint.X, orig_Y, mov_cmd.goalWaypoint.Y);
					moveForward(); 
					while (calc_dist(0, Xpos, 0, Ypos) < movAmt){};
					break;
				case 1: break; //Bwd
				case 2: 
						movAmt = calc_rad(orig_H, mov_cmd.goalWaypoint.H);
						turnRight();
						while (calc_rad(0, Yaw) < movAmt){};
						break;
				case 3: 
						movAmt = calc_rad(orig_H, mov_cmd.goalWaypoint.H);
						turnLeft();
						while (calc_rad(0, Yaw) < movAmt){};
						break;
				default: stop(); break;
			}

		}
		
		
      }