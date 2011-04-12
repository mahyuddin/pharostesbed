package pharoslabut;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import pharoslabut.logger.*;
import playerclient.PlayerClient;
import playerclient.PlayerException;
import playerclient.Position2DInterface;
import playerclient.Position2DListener;
import playerclient.structures.PlayerConstants;
import playerclient.structures.position2d.PlayerPosition2dData;


public class testing implements Position2DListener, CompassLoggerEventListener{
	double odreading;
	double compreading;
	boolean odflag = false;
	boolean compflag = false;
	boolean abort = false;
	private PlayerClient client = null;
	private FileLogger flogger = null;
	List <Double> CompLookUp;
	int table_size = 0;
	
	public testing(String serverIP, int serverPort, String fileName, boolean showGUI, List<Double> command) {
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
		
		//This generate the lookup table for compass range (0,2pi)
		try {
			CompLookUp = IO_Helper.readInput("src/pharoslabut/compass.txt");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		table_size = CompLookUp.size();
		
		// if we use one driver change to 6665
		System.out.println(command);
		CompassLoggerEvent compassLogger = new CompassLoggerEvent(serverIP, 6665, 2 /* device index */, showGUI);
		compassLogger.addListener(this);

		motors.addPos2DListener(this);
		compassLogger.start(1, fileName);
		
		
	//	turn_comp(175, motors);
	//	move_odometry(0.2, motors);
		//pause(1000);
		//move_odometry(0.5, motors);
	//	turn_comp(180, motors);
	//	turn_comp(45, motors);
	//	pause(1000);
	//	move_odometry(0.5, motors);
	//	move_odometry(0.5, motors);
	//	pause(1000);
	//	turn_comp(-90, motors);
	//	turn_comp(90, motors);

	//	turn_comp(90, motors);
	//	turn_comp(90, motors);
		
	/*
	 * This loop go through the movement instructions and figure whether to move forward
	 * or to turn, the look up table is the following:
	 * 	 ****************************************	
		 * 	Lookup Table for move_instruction	*
		 * 	0  - No turn						*
		 * 	1  - CW 45							*
		 *  2  - CW 90							*
		 *  3  - CW 135							*
		 * 	-1 - CCW 45							*
		 *  -2 - CCW 90							*
		 *  -3 - CCW 135						*
		 *  -4 - CCW	180						*
		 *  >=10 - Move forward					*
		 ****************************************
	 */
		for (int i = 0; i < command.size(); i++)
		{
			System.out.println("Abort button was pushed: "+abort);
			
			if(Math.abs(command.get(i)) >= 10.0)
			{
				System.out.println(command.get(i));
				double distance = command.get(i)/20;
				move_odometry(distance, motors);
			}
			
			else
			{
				int s = (int)(command.get(i).doubleValue());
				System.out.println("s:" + s);
				switch (s)
				{
					case -1: 
						turn_comp(45, motors);
						break;
					case -2:
						turn_comp(90, motors);
						break;
					case -3:
						turn_comp(135, motors);
						break;
					case -4:
						turn_comp(180, motors);
						break;
					case 1: 
						turn_comp(-45, motors);
						break;
					case 2:
						turn_comp(-90, motors);
						break;
					case 3:
						turn_comp(-135, motors);
						break;
					default:
						break;
				}
			}
		}
		

		log("Test complete!");
		compassLogger.stop();
		//System.exit(0);
	}
	
	
	private void move_odometry(double distance, Position2DInterface motors){
		double starting = 0;
		double speed = 0.2;
		if (distance <0) {
			speed = speed * -1;
			distance = distance* -1;
			}
		motors.resetOdometry();
		System.out.println("move");
		System.out.println(odflag);
		while(!odflag){
			synchronized(this) {
				try {
		            wait();
		        } catch (InterruptedException e) {}
		        starting = odreading;
			}
		}
		starting = 0;
		System.out.println("starting od " + starting);
		motors.setSpeed(speed, 0);
		/*
		 * TODO: needs new odometry driver
		 */
	    while((starting + distance - 0.04) > odreading) {
	    	synchronized(this) {
		    	try {
		            wait();
		        } catch (InterruptedException e) {}
	    	}
	    }
	    
		/*for(starting = odreading; (starting + distance) > odreading;)
		{
			System.out.println("position" + odreading); // works with but not without this line ?????
			pause(80);
		}
		//System.out.println(starting);*/
	    
		motors.setSpeed(0, 0);
		odflag = false;
		compflag = false;
	}
	
	
	private void circle(Position2DInterface motors){
		double starting = 0;
		double speed = 0.2;
		
		while(!compflag){
			synchronized(this) {
				try {
		            wait();
		        } catch (InterruptedException e) {}
		        starting = compreading;
			}
		}
		
		motors.setSpeed(0,speed);
		
	    while(true) {
	    	synchronized(this) {
		    	try {
		            wait();
		        } catch (InterruptedException e) {}
	    	}
	    }
		
	    //motors.setSpeed(0,0);
		
	}
	
	private void turn_comp(double angle, Position2DInterface motors){
		double speed = 0.1;
		double starting = 0;
		double ending = 0;
		int Next;
		
		Next = (int) ((double)table_size / (double)(360 / angle));
		System.out.println("slots: " + Next);
		if (angle < 0) {
			speed = speed * -1;
			}
		System.out.println(compflag);
		/*
		 * TODO: need to check this
		 */
		while(!compflag){
			synchronized(this) {
				try {
		            wait();
		        } catch (InterruptedException e) {}
		        starting = (compreading + Math.PI);
		        System.out.println("Print " + starting);
			}
		}
		
		for (int i = 0; i < table_size; i++)
		{
			if (i == table_size)
				i = 0;
			if (starting >= CompLookUp.get(i)-0.02 && starting <= CompLookUp.get(i)+0.02) {
				if (Next < 0 && Math.abs(Next) > i) {
					Next = table_size + Next;
				}
					System.out.println("current slot" + i);
					System.out.println("dest. slot" + (i+Next) % table_size);
					ending = CompLookUp.get((i+Next) % table_size);
				//	System.out.println((i+Next) % table_size);
					System.out.println("starting" + CompLookUp.get(i));
					System.out.println("ending" + CompLookUp.get((i+Next) % table_size));
					break;
			}
		}
		System.out.println("move:" + angle + " " + speed);
	
		motors.setSpeed(0,speed);

	    while((ending - 0.02) > (compreading + Math.PI) || (compreading + Math.PI) > (ending + 0.02)) {
			synchronized(this) {
		    	try {
		            wait();
		        } catch (InterruptedException e) {}
	    	}
	    }
	    
		motors.setSpeed(0,0);
		compflag=false;
		odflag = false;
		motors.resetOdometry();
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
		List<Double> command = new ArrayList<Double>();
		
		//command.add(-3.0);
//		command.add(14.0);
//		command.add(1.0);
		//command.add(10.0);
		//command.add(0.0);
//		command.add(-10.0);
		
		
		new testing(serverIP, serverPort, fileName, showGUI, command);

	}
	
	@Override
	public synchronized void newPlayerPosition2dData(PlayerPosition2dData data) {
		// TODO Auto-generated method stub
		odflag = true;
		odreading = data.getPos().getPx();
		notifyAll();
		System.out.println("Odometry  " + data.getPos().getPx());
		
	}

	@Override
	public synchronized void newHeading(double heading) {
		compflag = true;
		compreading = heading;
		notifyAll();
		System.out.println("heading: " + heading);
		
		// TODO Auto-generated method stub
		
	}
}