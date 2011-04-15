package pharoslabut;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

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
public class testing implements Position2DListener, CompassLoggerEventListener, IRListener {
	double odreading;
	double compreading;
	boolean odflag = false;
	boolean compflag = false;
	float [] IRdata = new float[3];
	GUI pavGUI;
	private PlayerClient client = null;
	private FileLogger flogger = null;
	List <Double> CompLookUp;
	int table_size = 0;
	
	short [] IR_table = {
			10651,  //  0
			10507,  //  1
			10363	,  //  2
			10219	,  //  3
			10075	,  //  4
			9931	,  //  5
			9779	,  //  6
			9627	,  //  7
			9474	,  //  8
			9322	,  //  9
			9169	,  //  10
			9014	,  //  11
			8860	,  //  12
			8705	,  //  13
			8550	,  //  14
			8395	,  //  15
			8230	,  //  16
			8065	,  //  17
			7899	,  //  18
			7734	,  //  19
			7569	,  //  20
			7346	,  //  21
			7122	,  //  22
			6899	,  //  23
			6675	,  //  24
			6452	,  //  25
			6270	,  //  26
			6088	,  //  27
			5906	,  //  28
			5723	,  //  29
			5541	,  //  30
			5359	,  //  31
			5226	,  //  32
			5093	,  //  33
			4959	,  //  34
			4826	,  //  35
			4676	,  //  36
			4526	,  //  37
			4376	,  //  38
			4227	,  //  39
			4077	,  //  40
			3992	,  //  41
			3907	,  //  42
			3823	,  //  43
			3738	,  //  44
			3653	,  //  45
			3569	,  //  46
			3483	,  //  47
			3397	,  //  48
			3312	,  //  49
			3226	,  //  50
			3160	,  //  51
			3094	,  //  52
			3028	,  //  53
			2962	,  //  54
			2896	,  //  55
			2838	,  //  56
			2781	,  //  57
			2724	,  //  58
			2667	,  //  59
			2610	,  //  60
			2553	,  //  61
			2515	,  //  62
			2477	,  //  63
			2438	,  //  64
			2400	,  //  65
			2362	,  //  66
			2318	,  //  67
			2273	,  //  68
			2229	,  //  69
			2184	,  //  70
			2159	,  //  71
			2134	,  //  72
			2108	,  //  73
			2083	,  //  74
			2057	,  //  75
			2032	,  //  76
			2004	,  //  77
			1976	,  //  78
			1948	,  //  79
			1920	,  //  80
			1892	,  //  81
			1870	,  //  82
			1848	,  //  83
			1826	,  //  84
			1803	,  //  85
			1782	,  //  86
			1760	,  //  87
			1739	,  //  88
			1717	,  //  89
			1695	,  //  90
			1673	,  //  91
			1651	,  //  92
			1629	,  //  93
			1607	,  //  94
			1590	,  //  95
			1573	,  //  96
			1556	,  //  97
			1539	,  //  98
			1522	,  //  99
			1505	,  //  100
			1490	,  //  101
			1474	,  //  102
			1459	,  //  103
			1444	,  //  104
			1429	,  //  105
			1417	,  //  106
			1406	,  //  107
			1394	,  //  108
			1383	,  //  109
			1372	,  //  110
			1358	,  //  111
			1344	,  //  112
			1330	,  //  113
			1316	,  //  114
			1302	,  //  115
			1290	,  //  116
			1279	,  //  117
			1267	,  //  118
			1256	,  //  119
			1245	,  //  120
			1229	,  //  121
			1214	,  //  122
			1199	,  //  123
			1184	,  //  124
			1168	,  //  125
			1159	,  //  126
			1149	,  //  127
			1140	,  //  128
			1130	,  //  129
			1121	,  //  130
			1111	,  //  131
			1106	,  //  132
			1102	,  //  133
			1097	,  //  134
			1092	,  //  135
			1081	,  //  136
			1070	,  //  137
			1058	,  //  138
			1047	,  //  139
			1036	,  //  140
			1024	,  //  141
			1013	,  //  142
			1002	,  //  143
			991	,  //  144
			983	,  //  145
			975	,  //  146
			968	,  //  147
			960	,  //  148
			953	,  //  149
			929	,  //  150
			905	,  //  151
			881	,  //  152
			857	,  //  153
			843	,  //  154
			830	,  //  155
			816	,  //  156
			802	,  //  157
			788	,  //  158
			775	,  //  159
			759	,  //  160
			744	,  //  161
			729	,  //  162
			714	,  //  163
			699	,  //  164
			682	,  //  165
			666	,  //  166
			650	,  //  167
			633	,  //  168
			617	,  //  169
			601	,  //  170
			584	,  //  171
			579	,  //  172
			574	,  //  173
			569	,  //  174
			564	,  //  175
			559	,  //  176
			548	,  //  177
			537	,  //  178
			525	,  //  179
			514	,  //  180
			509	,  //  181
			504	,  //  182
			498	,  //  183
			493	,  //  184
			488	,  //  185
			483	,  //  186
			476	,  //  187
			470	,  //  188
			464	,  //  189
			457	,  //  190
			451	,  //  191
			445	,  //  192
			438	,  //  193
			432	,  //  194
			425	,  //  195
			422	,  //  196
			418	,  //  197
			414	,  //  198
			410	,  //  199
			406	,  //  200
			401	,  //  201
			396	,  //  202
			391	,  //  203
			386	,  //  204
			381	,  //  205
			377	,  //  206
			373	,  //  207
			368	,  //  208
			364	,  //  209
			360	,  //  210
			356	,  //  211
			352	,  //  212
			349	,  //  213
			346	,  //  214
			343	,  //  215
			340	,  //  216
			338	,  //  217
			335	,  //  218
			333	,  //  219
			330	,  //  220
			325	,  //  221
			320	,  //  222
			315	,  //  223
			310	,  //  224
			305	,  //  225
			302	,  //  226
			300	,  //  227
			297	,  //  228
			295	,  //  229
			292	,  //  230
			290	,  //  231
			287	,  //  232
			284	,  //  233
			282	,  //  234
			279	,  //  235
			277	,  //  236
			274	,  //  237
			272	,  //  238
			269	,  //  239
			267	,  //  240
			264	,  //  241
			262	,  //  242
			259	,  //  243
			257	,  //  244
			254	,  //  245
			251	,  //  246
			249	,  //  247
			246	,  //  248
			244	,  //  249
			241	,  //  250
			236	,  //  251
			231	,  //  252
			226	,  //  253
			221	,  //  254
			216  //  255
			 };

	
	public testing(String serverIP, int serverPort, String fileName, boolean showGUI, List<Double> command, GUI gui) {
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		table_size = CompLookUp.size();
		
		// if we use one driver change to 6665
		System.out.println(command);
		CompassLoggerEvent compassLogger = new CompassLoggerEvent(serverIP, 6665, 2 /* device index */, showGUI);
		IRInterface irdata = (client).requestInterfaceIR(0, PlayerConstants.PLAYER_OPEN_MODE);
		
		irdata.addIRListener(this);
		compassLogger.addListener(this);
		motors.addPos2DListener(this);
		
		compassLogger.start(1, fileName);
		

	//	turn_comp(-90, motors);
	//  move_odometry(10, motors);
		//pause(1000);
		//move_odometry(0.5, motors);
	//	turn_comp(-90, motors);
	//	turn_comp(45, motors);
	//	pause(1000);
	//	move_odometry(0.5, motors);
	//	move_odometry(0.5, motors);
	//	pause(1000);
	//	turn_comp(-90, motors);
	//	turn_comp(90, motors);
	//	move_odometry(0.5, motors);
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

			//System.out.println("Abort button was pushed: "+abort);

			if(pavGUI.abortMovement){
				break;
			}

			
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
		/*for(;;){
			move_odometry(0,motors);
		}*/
		compassLogger.stop();
		motors.removePos2DListener(this);
		irdata.removeIRListener(this);

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
		//System.out.println("move");
		//System.out.println(odflag);
		while(!odflag){
			synchronized(this) {
				try {
		            wait();
		        } catch (InterruptedException e) {}
		        starting = odreading;
			}
		}
		//NOTE: this is a hack, since setOdometry() is asynchronous.
		starting = 0;
		//System.out.println("starting od " + starting);
		
		motors.setSpeed(speed, 0);
	
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
		//	System.out.println(compflag);
		/*
		 * TODO: compflag sometimes changes to true even though we have manually set to false at the end of this method.
		 * 		 temprarily fixed by setting it to false at the end of move_odometry as well.
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
		
//		command.add(-3.0);
//		command.add(14.0);
//		command.add(1.0);
//		command.add(10.0);
//		command.add(0.0);
//		command.add(-10.0);
		
		GUI pav = new GUI();
		
		new testing(serverIP, serverPort, fileName, showGUI, command, pav);

	}
	
	@Override
	public synchronized void newPlayerPosition2dData(PlayerPosition2dData data) {
		// TODO Auto-generated method stub
		odflag = true;
		odreading = data.getPos().getPx();
		notifyAll();
		//System.out.println("Odometry  " + data.getPos().getPx());
		
	}

	@Override
	public synchronized void newHeading(double heading) {
		compflag = true;
		compreading = heading;
		notifyAll();
		//System.out.println("heading: " + heading);
		
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public synchronized void newPlayerIRData (PlayerIrData data)
	{
		notifyAll();
		IRdata = data.getRanges();
		System.out.println("  raw  " + IRdata[0]);
		System.out.println("  raw  " + IRdata[1]);
		System.out.println( "  raw  " + IRdata[2]);
		System.out.println("\n");
		// TODO Auto-generated method stub
	}


	
	
}