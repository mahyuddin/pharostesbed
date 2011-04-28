package pharoslabut;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;

import pharoslabut.logger.*;
import playerclient.IRInterface;
import playerclient.IRListener;
import playerclient.PlayerClient;
import playerclient.PlayerException;
import playerclient.Position2DInterface;
import playerclient.Position2DListener;
import playerclient.structures.PlayerConstants;
import playerclient.structures.PlayerPose;
import playerclient.structures.ir.PlayerIrData;
import playerclient.structures.position2d.PlayerPosition2dData;


/*
 * testing.java
 * Purpose: This class creates a testing object which implements the movement instructions.
 * Authors: Harvey Cheng and Aniwar Ibrahim
 * Modified Date: 04/13/11
 */
public class testing implements Position2DListener/*, CompassLoggerEventListener */, IRListener {
	double odreading;
	double compreading;
	double yawreading;
	boolean odflag = false;
	boolean compflag = false;
	float [] IRdata = new float[3];
	GUI pavGUI;
	private PlayerClient client = null;
	private FileLogger flogger = null;
	List <Double> CompLookUp;
	int table_size = 0;
	double halfway = 0;
	List<List<Double>> IRMatrix = new ArrayList<List<Double>>();
	
	double [] ir_table = {100.388,//	34
			98.6252	,//	35
			96.759	,//	36
			94.7861	,//	37
			92.7081	,//	38
			90.5329	,//	39
			88.278	,//	40
			85.97	,//	41
			83.6438	,//	42
			81.3377	,//	43
			79.0875	,//	44
			76.9212	,//	45
			74.8572	,//	46
			72.9045	,//	47
			71.0644	,//	48
			69.3336	,//	49
			67.7059	,//	50
			66.1735	,//	51
			64.7284	,//	52
			63.363	,//	53
			62.0699	,//	54
			60.8424	,//	55
			59.6745	,//	56
			58.5609	,//	57
			57.4967	,//	58
			56.4777	,//	59
			55.5001	,//	60
			54.5605	,//	61
			53.6561	,//	62
			52.784	,//	63
			51.9419	,//	64
			51.1277	,//	65
			50.3393	,//	66
			49.5752	,//	67
			48.8337	,//	68
			48.1133	,//	69
			47.4128	,//	70
			46.731	,//	71
			46.0668	,//	72
			45.4192	,//	73
			44.7873	,//	74
			44.1704	,//	75
			43.5675	,//	76
			42.978	,//	77
			42.4012	,//	78
			41.8366	,//	79
			41.2836	,//	80
			40.7415	,//	81
			40.21	,//	82
			39.6886	,//	83
			39.1768	,//	84
			38.6743	,//	85
			38.1807	,//	86
			37.6956	,//	87
			37.2186	,//	88
			36.7496	,//	89
			36.2881	,//	90
			35.8339	,//	91
			35.3868	,//	92
			34.9464	,//	93
			34.5126	,//	94
			34.0851	,//	95
			33.6638	,//	96
			33.2484	,//	97
			32.8387	,//	98
			32.4346	,//	99
			32.0358	,//	100
			31.6423	,//	101
			31.2538	,//	102
			30.8702	,//	103
			30.4914	,//	104
			30.1172	,//	105
			29.7475	,//	106
			29.3821	,//	107
			29.0211	,//	108
			28.6641	,//	109
			28.3112	,//	110
			27.9623	,//	111
			27.6171	,//	112
			27.2757	,//	113
			26.9379	,//	114
			26.6036	,//	115
			26.2728	,//	116
			25.9454	,//	117
			25.6213	,//	118
			25.3004	,//	119
			24.9826	,//	120
			24.6679	,//	121
			24.3562	,//	122
			24.0475	,//	123
			23.7416	,//	124
			23.4385	,//	125
			23.1381	,//	126
			22.8405	,//	127
			22.5455	,//	128
			22.2531	,//	129
			21.9632	,//	130
			21.6757	,//	131
			21.3907	,//	132
			21.1081	,//	133
			20.8278	,//	134
			20.5498	,//	135
			20.2741	//	136
			};
	
	public testing(String serverIP, int serverPort, String fileName, boolean showGUI, List<Double> command, List<Integer> heading, List<List<Double>> ir_range, GUI gui) {
		pavGUI = gui;
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
			e.printStackTrace();
		}
		table_size = CompLookUp.size();
		
		// if we use one driver change to 6665
//		CompassLoggerEvent compassLogger = new CompassLoggerEvent(serverIP, 6665, 2 /* device index */, showGUI);
		IRInterface irdata = (client).requestInterfaceIR(0, PlayerConstants.PLAYER_OPEN_MODE);
		
		irdata.addIRListener(this);
//		compassLogger.addListener(this);
		motors.addPos2DListener(this);
		
//		compassLogger.start(1, fileName);
		

//		turn_odometry(45, motors);
//		move_odometry(0.17, motors);

//		move_odometry(0.18, motors);
//		pause(1000);
		
	/*
	 * This loop go through the movement instructions and figure whether to move forward
	 * or to turn, the look up table is the following:
	 * 	 ****************************************	
		 * 	Lookup Table for move_instruction	*
		 * 	0  - No turn						*
		 * 	1  - CW 45							*
		 *  2  - CW 90							*
		 *  3  - CW 135							*
		 *  4  - CW 180						    *
		 * 	-1 - CCW 45							*
		 *  -2 - CCW 90							*
		 *  -3 - CCW 135						*
		 *  -4 - CCW	180						*
		 *  >=10 - Move forward					*
		 ****************************************
	 */
		System.out.println("Command received: " + command);
		for (int i = 0; i < command.size(); i++)
		{


			if(pavGUI.abortMovement){
				break;
				}

			
			if(Math.abs(command.get(i)) >= 10.0)
			{
				IRMatrix.clear();
//				System.out.println(command.get(i));
				double distance = command.get(i) * 0.035;
				int nodeindex = i/2+1;
				int h = heading.get(nodeindex);
				move_odometry(distance, motors);
				if(ir_range.get(nodeindex).get(1)<3.0)
				{
					long start = System.currentTimeMillis();
					while (System.currentTimeMillis() - start < 1000){}
					ir_adjustdistance(ir_range,IRMatrix,nodeindex,h,motors);
				}
				if(h%90==0)
					ir_adjustheading1(ir_range,IRMatrix,nodeindex,motors);
				else
					ir_adjustheading2(ir_range,IRMatrix,nodeindex,motors); 
				pavGUI.incrementPosition();
			}
			
			else
			{
				int s = (int)(command.get(i).doubleValue());
				switch (s)
				{
					case -1: 
						turn_odometry(45, motors);
						pavGUI.rotateHeading(45);
						break;
					case -2:
						turn_odometry(90, motors);
						pavGUI.rotateHeading(90);
						break;
					case -3:
						turn_odometry(135, motors);
						pavGUI.rotateHeading(135);
						break;
					case -4:
						turn_odometry(180, motors);
						pavGUI.rotateHeading(180);
						break;
					case 1: 
						turn_odometry(-45, motors);
						pavGUI.rotateHeading(-45);
						break;
					case 2:
						turn_odometry(-90, motors);
						pavGUI.rotateHeading(-90);
						break;
					case 3:
						turn_odometry(-135, motors);
						pavGUI.rotateHeading(-135);
						break;
					case 4:
						turn_odometry(-180, motors);
						pavGUI.rotateHeading(-180);
					default:
						break;
				}
			}
		}
		

		log("Test complete!");
/*		for(;;)
		{
			motors.setSpeed(0,0);
		}

*/
//		compassLogger.stop();
		motors.removePos2DListener(this);
		irdata.removeIRListener(this);


		//System.exit(0);
	
	}
	
	/*
	 * Function ir_adjust: Adjust distance movement using IR data
	 * Input:
	 * 		ir_data: Readings from IR sensors
	 * 		ir_range: Obstacle distances according to map
	 * 		nodeindex: index of current node on the path
	 * Output: none
	 * Added by Le Wang on Apr. 19, 2011
	 */
	private void ir_adjustdistance(List<List<Double>> ir_range,List<List<Double>> ir_data,
			int nodeindex,int heading,Position2DInterface motors){
		System.out.println("Adjust Distance");
		int size = ir_data.size();
		double gridsize = 35,frontoffset = 0,angeloffset = 1;
		double frontmap,frontfinal,fronterror;
		if(heading%90 != 0)
			angeloffset = Math.sqrt(2);
		frontmap = angeloffset*(gridsize*ir_range.get(nodeindex).get(1)+frontoffset);
		frontfinal = ir_data.get(size-1).get(1);
		//correct for straight line movement error
		fronterror = frontfinal - frontmap;
		System.out.println("map " + frontmap);
		System.out.println("final " + frontfinal);
	//	System.out.println(fronterror);
		if(fronterror>5 && frontmap<gridsize*4+frontoffset){
			move_odometry(fronterror/100,motors);
			System.out.println("Done");
		}
	}
	
	/***
	 * Function ir_adjust: Adjust heading using IR data(heading is 90, 180, 270)
	 * Input:
	 * 		ir_data: Readings from IR sensors
	 * 		ir_range: Obstacle distances according to map
	 * 		nodeindex: index of current node on the path
	 * Output: none
	 * Added by Le Wang on Apr. 22, 2011
	 */
	private void ir_adjustheading1(List<List<Double>> ir_range,List<List<Double>> ir_data,
			int nodeindex,Position2DInterface motors){
		System.out.println("adjust heading 1");
		int indexmid,indexright = 0,indexleft = 0;
		int size = ir_data.size();
		boolean flagleft = false,flagright = false;
		double error = 0,rightmap,leftmap;
		double adjangle = 0;
	//	frontfinal = ir_data.get(size-1).get(1);
		leftmap = ir_range.get(nodeindex).get(2);
		rightmap = ir_range.get(nodeindex).get(0);
		for(int i=1;i<size-1;i++){
			if(Math.abs(ir_data.get(i).get(0)-ir_data.get(i-1).get(0))>17)
				indexright = Math.min(size-1,i+1);
		}
		if(indexright == 0)
			flagright = true;
		for(int i=1;i<size-1;i++){
			if(Math.abs(ir_data.get(i).get(2)-ir_data.get(i-1).get(2))>17)
				indexleft = Math.min(size-1,i+1);
		}
		if(indexleft == 0)
			flagleft = true;
		//frontmid = ir_data.get(indexmid).get(1);
		//lefterror = ir_data.get(size-1).get(2) - ir_data.get(indexmid).get(2);
		//System.out.println("left error: " + lefterror);
		//righterror = ir_data.get(indexmid).get(0) - ir_data.get(size-1).get(0);
		if(leftmap<3 && rightmap<3)
			if(!flagright && !flagleft){
				indexmid = Math.max(indexleft, indexright);
				error = (ir_data.get(size-1).get(2)-ir_data.get(indexmid).get(2)
						+ir_data.get(indexmid).get(0)-ir_data.get(size-1).get(0))/2;
			}
			else if(!flagright)
				error = ir_data.get(size-1).get(2)-ir_data.get(indexleft).get(2);
			else
				error = ir_data.get(indexright).get(0)-ir_data.get(size-1).get(0);		
		else if(leftmap<3)
			error = ir_data.get(size-1).get(2)-ir_data.get(indexleft).get(2);
		else if(rightmap<3)
			error = ir_data.get(indexright).get(0)-ir_data.get(size-1).get(2);
		System.out.println(error);
		if(!flagright && !flagleft)
			adjangle = Math.atan(error/halfway);
		else
			adjangle = Math.atan(error/(2*halfway));
		adjangle = 180*adjangle/Math.PI;
		System.out.println("adjangle" + adjangle);
		System.out.println("halfway " + halfway);
		if(adjangle>5)
			turn_odometry(adjangle,motors);
	}
	
	/***
	 * Function ir_adjust: Adjust heading using IR data(heading is 45, 135, 225, 315)
	 * Input:
	 * 		ir_data: Readings from IR sensors
	 * 		ir_range: Obstacle distances according to map
	 * 		nodeindex: index of current node on the path
	 * Output: none
	 * Added by Le Wang on Apr. 22, 2011
	 */
	private void ir_adjustheading2(List<List<Double>> ir_range,List<List<Double>> ir_data,
			int nodeindex,Position2DInterface motors){
		
		System.out.println("adjust heading 2");
		int indexmid = 0,indexright = 0,indexleft = 0;
		int size = ir_data.size();
		boolean flagright = false,flagleft = false;
		double frontfinal,frontmid,leftmax=0,leftmin=255,rightmax=0,rightmin=255,rightmap,leftmap;
		double adjangle = 0,lefterror,righterror;
		frontfinal = ir_data.get(size-1).get(1);
		leftmap = ir_range.get(nodeindex).get(2);
		rightmap = ir_range.get(nodeindex).get(0);
		for(int i=1;i<size-1;i++){
			if(Math.abs(ir_data.get(i).get(0)-ir_data.get(i-1).get(0))>24)
				indexright = Math.min(size-1,i+1);
		}
		if(indexright == 0)
			flagright = true;
		for(int i=1;i<size-1;i++){
			if(Math.abs(ir_data.get(i).get(2)-ir_data.get(i-1).get(2))>24)
				indexleft = Math.min(size-1,i+1);
		}
		for(int i=indexleft;i<size;i++){
			if(leftmin>ir_data.get(i).get(2))
				leftmin = ir_data.get(i).get(2);
			if(leftmax<ir_data.get(i).get(2))
					leftmax = ir_data.get(i).get(2);
		}
		for(int i=indexright;i<size;i++){
			if(rightmin>ir_data.get(i).get(0))
				rightmin = ir_data.get(i).get(0);
			if(rightmax<ir_data.get(i).get(0))
					rightmax = ir_data.get(i).get(0);
		}
		lefterror = leftmax - leftmin;
		righterror = rightmax - rightmin;
		if(leftmap<2 && rightmap<2)
			adjangle = Math.atan2(halfway,(lefterror+righterror)/2) - Math.PI/4;
		else if(leftmap<2)
			adjangle = Math.atan2(halfway,lefterror) - Math.PI/4;
		else if(rightmap<2)
			adjangle = Math.atan2(halfway,righterror) - Math.PI/4;
		adjangle = 180*adjangle/Math.PI;
		if(adjangle>5)
			turn_comp((180*adjangle/Math.PI),motors);
	}

	private void move_odometry(double distance, Position2DInterface motors){
		double starting = 0;
		double speed = 0.1;
		if (distance <0) {
			speed = speed * -1;
			distance = distance* -1;
			}
		
		while(!odflag){
			synchronized(this) {
				try {
		            wait();
		        } catch (InterruptedException e) {}
		        motors.resetOdometry();
		        pause(100);
		        starting = odreading;
			}
		}
	//	System.out.println("starting od " + starting);
		
		motors.setSpeed(speed, 0);
		
		//NOTE: this is a hack, since setOdometry() is asynchronous.
	    while((distance-0.02) > odreading) {
	    	synchronized(this) {
		    	try {
		            wait();
		        } catch (InterruptedException e) {}
	    	}
	    }
	    
	    
		motors.setSpeed(0, 0);
		halfway = odreading*100/2;
		motors.resetOdometry();
		pause(100);
		odflag = false;
		compflag = false;
	}
	
	
	private void turn_odometry(double degree, Position2DInterface motors){
		double starting = 0;
		double speed = Math.PI/20;
		if (degree < 0) {
			speed = speed * -1;
		//	degree = degree* -1;
			}
		
		while(!odflag){
			synchronized(this) {
				try {
		            wait();
		        } catch (InterruptedException e) {}
		        motors.resetOdometry();
		        pause(100);
		        starting = yawreading;
			}
		}
	//	System.out.println("starting od " + starting);
		
		motors.setSpeed(0, speed);
		
		//NOTE: this is a hack, since setOdometry() is asynchronous.
		
		if (speed > 0)
		{
			while((degree*Math.PI/180-0.08) > yawreading) {
	    		synchronized(this) {
	    			try {
	    				wait();
	    			} catch (InterruptedException e) {}
	    		}
	    	}
		}
		
		else
		{
			while((degree*Math.PI/180+0.08) < yawreading) {
		    	synchronized(this) {
			    	try {
			            wait();
			        } catch (InterruptedException e) {}
		    	}
		    }
			
		}
	    
	    
		motors.setSpeed(0, 0);
		motors.resetOdometry();
		pause(100);
		odflag = false;
		compflag = false;
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
		
		/*
		 * NOTE: compflag sometimes changes to true even though we have manually set to false at the end of this method.
		 * 		 temprarily fixed by setting it to false at the end of move_odometry as well.
		 */
		while(!compflag){
			synchronized(this) {
				try {
		            wait();
		        } catch (InterruptedException e) {}
	//	        motors.resetOdometry();
		        starting = compreading;
	//	        System.out.println("Print " + starting);
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
//					System.out.println("current slot" + i);
//					System.out.println("dest. slot" + (i+Next) % table_size);
					ending = CompLookUp.get((i+Next) % table_size);
//					System.out.println((i+Next) % table_size);
//					System.out.println("starting" + CompLookUp.get(i));
//					System.out.println("ending" + CompLookUp.get((i+Next) % table_size));
					break;
			}
		}
//		System.out.println("move:" + angle + " " + speed);
	
		motors.setSpeed(0,speed);

	    while((ending - 0.02) > compreading || compreading > (ending + 0.02)) {
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
		pause(100);
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

	private void pause (int duration) {
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
		List<Integer> heading = new ArrayList<Integer>();
		List<List<Double>> ir_range = new ArrayList<List<Double>>();


//		command.add(10.0);
//		command.add(14.0);
//		command.add(1.0);
//		command.add(10.0);
//		command.add(-10.0);
		

		heading.add(0);
		heading.add(0);
		ir_range.add(Arrays.asList(3.0,3.0,1.0));
		ir_range.add(Arrays.asList(3.0,3.0,1.0));
		JFrame f = new JFrame();
		GUI gui = new GUI(f);
		new testing(serverIP, serverPort, fileName, showGUI, command, heading, ir_range, gui);

	}
	
	@Override
	public synchronized void newPlayerPosition2dData(PlayerPosition2dData data) {
		odflag = true;
		odreading = data.getPos().getPx();
		yawreading = data.getPos().getPa();
		notifyAll();
//		System.out.println("Odometry  " + data.getPos().getPx());
//		System.out.println("Odometry yaw " + yawreading);
	}

	/*
	public synchronized void newHeading(double heading) {
		compflag = true;
		compreading = heading + Math.PI;
		notifyAll();
	//	System.out.println("heading: " + compreading);
				
	}
	
	*/
	
	@Override
	public synchronized void newPlayerIRData (PlayerIrData data)
	{
		notifyAll();
		IRdata = data.getRanges();
		//0 right
		//1 front
		//2 left


		double [] convert = {-1,-1,-1};
		
		if (IRdata[0] >= 34 && IRdata[0] <= 136 )
		{
			convert[0] = ir_table[(int) (IRdata[0]-34)];
		}
		else
		{
			convert[0] = -1;
		}
		
		if (IRdata[1] >= 34 && IRdata[1] <= 136 )
		{
			convert[1] = ir_table[(int) (IRdata[1]-34)]; 
		}
		else
		{

			convert[1] = -1;
		}
		
		if (IRdata[2] >= 34 && IRdata[2] <= 136 )
		{
			convert[2] = ir_table[(int) (IRdata[2]-34)]; 
		}
		else
		{
			convert[2] = -1;
		}
		
		System.out.println("right: " + convert[0] + "\t front: " + convert[1] + "\t left: " + convert[2] );
		IRMatrix.add(Arrays.asList(convert[0],convert[1], convert[2]));
	}



	
}