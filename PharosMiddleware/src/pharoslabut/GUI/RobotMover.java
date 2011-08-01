package pharoslabut.GUI;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import pharoslabut.navigate.MotionArbiter;
import pharoslabut.sensors.GPSDataBuffer;
import pharoslabut.tasks.Priority;
import pharoslabut.tasks.MotionTask;
import pharoslabut.exceptions.NoNewDataException;
import pharoslabut.logger.*;
import playerclient3.GPSInterface;
import playerclient3.PlayerClient;
import playerclient3.PlayerException;
import playerclient3.Position2DInterface;
import playerclient3.structures.PlayerConstants;
import playerclient3.structures.gps.PlayerGpsData;
 
public class RobotMover extends RobotWindow implements Runnable{
    
    
    private PlayerClient client = null;
    private FileLogger flogger = null;
    private String serverIP;
    private int serverPort;
    private String fileName;
    private String robotType;
    private double speed;
    private double angle;
    private long duration;
    private static String indoor;
    //GPS
    public static final long GPS_LOGGER_REFRESH_PERIOD = 100;
    private int deviceIndex;
    private GPSInterface gps = null;
    private GPSDataBuffer gpsdb;
    private boolean logging = false;
    private long startTime;
    private FileLogger gpsFlogger;
    private PlayerGpsData gpsData;
    private PlayerGpsData oldData = null;
    
    
    
    
        
        
   public RobotMover(){
            client = null;
            flogger = null;
            serverIP = "";
            serverPort = 0;
            fileName = "";
            robotType ="";
            speed = 0;
            angle = 0;
            duration = 0;
            indoor = " ";
        }
 
	public RobotMover(String serverIP, int serverPort, String fileName, String robotType, double speed, double angle, long duration, String indoor) 
               
	{
            this.serverIP = serverIP;
            this.serverPort = serverPort;
            this.fileName = fileName;
            this.robotType = robotType;
            this.speed = speed;
            this.angle = angle;
            this.duration = duration;
            this.indoor = indoor;
		
	}
	
        void connectRobot(){
            
            // Connect to the player server...
            try {
                
			client = new PlayerClient(serverIP, serverPort);
		} catch(PlayerException e) {
			log("Error connecting to Player: ");
			log("    [ " + e.toString() + " ]");
			System.exit (1);
		}
 
		// Subscribe to robot motors...
		Position2DInterface motors = client.requestInterfacePosition2D(0, PlayerConstants.PLAYER_OPEN_MODE);
		if (motors == null) {
			log("motors is null");
			System.exit(1);
		}
 
		// Create a motion arbiter...
		MotionArbiter motionArbiter = null;
		if (robotType.equals("traxxas"))
			motionArbiter = new MotionArbiter(MotionArbiter.MotionType.MOTION_TRAXXAS, motors);
		else if (robotType.equals("create"))
			motionArbiter = new MotionArbiter(MotionArbiter.MotionType.MOTION_IROBOT_CREATE, motors);
		else
			motionArbiter = new MotionArbiter(MotionArbiter.MotionType.MOTION_SEGWAY_RMP50, motors);
 
		// Enable logging...
		if (fileName != null) {
			flogger = new FileLogger(fileName);
			motionArbiter.setFileLogger(flogger);
		}
 
                
                
		MotionTask currTask;
 
		currTask = new MotionTask(Priority.SECOND, speed, angle);
		log("Submitting: " + currTask);
		motionArbiter.submitTask(currTask);
 
		log("Allowing robot to move for " + duration + "ms...");
		pause(duration);
 
 
		currTask = new MotionTask(Priority.SECOND, MotionTask.STOP_SPEED, MotionTask.STOP_HEADING);
		log("Submitting: " + currTask);
		motionArbiter.submitTask(currTask);
 
		log("Test complete!");
           
        }
        
        
        
        void connectGPS(){
            
            
            	try {
			logDbg("Connecting to server " + serverIP + ":" + serverPort);
			client = new PlayerClient(serverIP, serverPort);
		} catch(PlayerException e) {
			logErr("Problem connecting to Player server: " + e.toString());
			System.exit (1);
		}
		
		while (gps == null) {
			try {
				logDbg("Subscribing to GPS service...");
				gps = client.requestInterfaceGPS(deviceIndex, PlayerConstants.PLAYER_OPEN_MODE);
				if (gps != null)
					logDbg("Subscribed to GPS service...");
				else {
					logErr("GPS service was null, waiting 1s then retrying...");
					synchronized(this) {
						try {
							wait(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			} catch(PlayerException pe) {
				logErr(pe.getMessage());
			}
		

		logDbg("run: thread starting...");

		while(logging) {
			try {
				
				gpsData = gpsdb.getCurrLoc();
				
				if (oldData != null && !oldData.equals(gpsData)) {

					long endTime = System.currentTimeMillis();


					String result = endTime + "\t" + (endTime - startTime) + "\t";
					result += gpsData.getQuality()  + "\t" + (gpsData.getLatitude()/1e7) + "\t" 
					+ (gpsData.getLongitude()/1e7) + "\t" + gpsData.getAltitude() + "\t"
					+ gpsData.getTime_sec() + "\t" + gpsData.getTime_usec() + "\t"
					+ gpsData.getErr_vert() + "\t" + gpsData.getErr_horz();
                                        
            ExecutorService es = Executors.newFixedThreadPool(3);        
            List<Future> futures = new ArrayList<Future>();
           
              futures.add(es.submit(new Callable() {
            @Override
                    public Object call() throws Exception {
                        updateLatitude(Double.toString(gpsData.getLatitude()/1e7)+"\n");
                        return null;
                    }
                }));
              futures.add(es.submit(new Callable() {
            @Override
                    public Object call() throws Exception {
                        updateLongitude(Double.toString(gpsData.getLongitude()/1e7)+"\n");
                        return null;
                    }
                }));
              futures.add(es.submit(new Callable() {
            @Override
                    public Object call() throws Exception {
                        updateAltitude(Double.toString(gpsData.getAltitude())+"\n");
                        return null;
                    }
                }));
              
               for (Future future:futures)
            try {
                future.get();  // blocking call, explicitly waiting for the response from a specific task, not necessarily the first task that is completed
            } catch (InterruptedException e) {
            } catch (ExecutionException e) {
            }
            
                                        
					log(result);
				}
				
				oldData = gpsData;
				
			} catch (NoNewDataException e1) {
				logDbg("run: No new data...");
			}

			try {
				synchronized(this) {
					wait(GPS_LOGGER_REFRESH_PERIOD);
				}
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}

		logDbg("run: thread terminating...");
	}
            
        }
        
        
        
	@Override
	public void run() {
           

		if(this.indoor.equalsIgnoreCase("yes")){
                      
            connectRobot();
            
                        
                         
		
		}
		
		else if(this.indoor.equalsIgnoreCase("no")){
			
			connectGPS();
			
			connectRobot();
                       
                }
                        
		else{
			
			System.out.println("For indoor type: yes and for outdoor type: no");
		}
	}
        
        
        
   
        
        
        
 
	/**
	 * Pauses the calling thread a certain amount of time.
	 * 
	 * @param duration The pause duration in milliseconds.
	 */
	public void pause(long duration) {
		synchronized(this) {
			try {
				wait(duration);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
 
	public void log(String msg) {
		if(indoor.equals("no")){
		String result = "RobotMover: " + msg;
		System.out.println(result);
		if (flogger != null)
			flogger.log(result);
		//GPS
		System.out.println(msg);
		if (gpsFlogger != null)
			gpsFlogger.log(msg);
		}
		else if(indoor.equals("yes")){
			String result = "RobotMover: " + msg;
			System.out.println(result);
			if (flogger != null)
				flogger.log(result);	
		}
		else{
			System.out.println("For logging process you should type yes or no according to indoor location" );
		}
	}
 
	public static void usage() {
		if(indoor.equals("yes")){
		System.err.println("Usage: pharoslabut.RobotMover <options>\n");
		System.err.println("Where <options> include:");
		System.err.println("\t-server <ip address>: The IP address of the Player Server (default localhost)");
		System.err.println("\t-port <port number>: The Player Server's port number (default 6665)");
		System.err.println("\t-log <file name>: name of file in which to save results (default RobotMover.log)");
		System.err.println("\t-robot <robot type>: The type of robot, either traxxas, segway, or create (default traxxas)");
		System.err.println("\t-speed <speed>: The speed in meters per second (default 0.5)");
		System.err.println("\t-angle <angle>: The angle in radians (default 0)");
		System.err.println("\t-duration <duration>: The duration in milliseconds (default 1000)");
		}
		
		if(indoor.equals("no")){
		System.err.println("Usage: pharoslabut.RobotMover <options>\n");
		System.err.println("Where <options> include:");
		System.err.println("\t-server <ip address>: The IP address of the Player Server (default localhost)");
		System.err.println("\t-port <port number>: The Player Server's port number (default 6665)");
		System.err.println("\t-log <file name>: name of file in which to save results (default RobotMover.log)");
		System.err.println("\t-robot <robot type>: The type of robot, either traxxas, segway, or create (default traxxas)");
		System.err.println("\t-speed <speed>: The speed in meters per second (default 0.5)");
		System.err.println("\t-angle <angle>: The angle in radians (default 0)");
		System.err.println("\t-duration <duration>: The duration in milliseconds (default 1000)");
		System.err.println("Usage: pharoslabut.logger.GPSLogger <options>\n");
		System.err.println("Where <options> include:");
		System.err.println("\t-server <ip address>: The IP address of the Player Server (default localhost)");
		System.err.println("\t-port <port number>: The Player Server's port number (default 6665)");
		System.err.println("\t-index <index>: the index of the GPS device (default 0)");
		System.err.println("\t-log <file name>: The name of the file into which the compass data is logged (default log.txt)");
		System.err.println("\t-time <period>: The amount of time in seconds to record data (default infinity)");
		System.err.println("\t-d: enable debug output");
		}
		
		
	}
        
        public void setAllParameters(String serverIP, int serverPort,	String fileName, String robotType,
			double speed, double angle, long duration, String indoor){
            this.serverIP = serverIP;
            this.serverPort = serverPort;
            this.fileName = fileName;
            this.robotType = robotType;
            this.speed = speed;
            this.angle = angle;
            this.duration = duration;
            this.indoor = indoor;
        }
 
        public void setserverIp(String  serverIp){
            this.serverIP = serverIp;
            
        }
        
        public void setFileName(String fileName){
            this.fileName = fileName;
        }
        public void setRobotType(String robotType){
            this.robotType = robotType;
            
        }
        public void setSpeed(double speed){
            this.speed = speed;
        }
        public void setAngle(double angle){
            this.angle = angle;
        }
        public void setDuration(long duration){
            this.duration = duration;
        }
        
        public void setIndoor(String indoor){
        	this.indoor = indoor;
        }
        
        
        

    public double getAngle() {
        return angle;
    }

    public long getDuration() {
        return duration;
    }

    public String getFileName() {
        return fileName;
    }

    public String getRobotType() {
        return robotType;
    }

    public String getServerIp() {
        return serverIP;
    }

    public int getServerPort() {
        return serverPort;
    }

    public double getSpeed() {
        return speed;
    }

    public String getIndoor(){
        return indoor;
    }
//-------------------------GPS------------------------------//

    
public void gpsConnection(String serverIP, int serverPort, int deviceIndex, FileLogger flogger) {
		
		this.serverIP = serverIP;
		this.serverPort = serverPort;
		this.deviceIndex = deviceIndex;
		this.flogger = flogger;
		
		
	}
public void gpsConnection(GPSInterface gps) {
	this.gps = gps;
}

	public boolean start() {
		if (!logging) {
			logging = true;
			
			log("Time (ms)\tDelta Time (ms)\tGPS Quality\tLatitude\tLongitude\tAltitude\tGPS Time (s)\tGPS Time(us)\tGPS Error Vertical\tGPS Error Horizontal");
			
			startTime = System.currentTimeMillis();
			
			new Thread(this).start();
			
			return true;
		} else
			return false;
	}
	public void stop() {
		logging = false;
	}
	      
	 public double Latitude() throws NoNewDataException{
         
         
         return gpsdb.getCurrLoc().getLatitude();
     }
     
     
     public double Longitude() throws NoNewDataException{
        
         
         return gpsdb.getCurrLoc().getLongitude();
     }
     
     public double Altitude() throws NoNewDataException{
         
         return gpsdb.getCurrLoc().getAltitude();
     }
     
     
     public String getServerIP() {
			return serverIP;
		}

		public void setServerIP(String serverIP) {
			this.serverIP = serverIP;
		}

		

		public void setServerPort(int serverPort) {
			this.serverPort = serverPort;
		}

		public int getDeviceIndex() {
			return deviceIndex;
		}

		public void setDeviceIndex(int deviceIndex) {
			this.deviceIndex = deviceIndex;
		}

		public long getStartTime() {
			return startTime;
		}

		public void setStartTime(long startTime) {
			this.startTime = startTime;
		}  
		
		private void logDbg(String msg) {
			String result = "GPSLogger: " + msg;
			if (System.getProperty ("PharosMiddleware.debug") != null) {
				System.out.println(result);
				if (gpsFlogger != null)
					gpsFlogger.log(result);
			}
		}
		
		private void logErr(String msg) {
			String result = "GPSLogger: ERROR: " + msg;
			System.err.println(result);
			if (gpsFlogger != null)
				gpsFlogger.log(result);
		}
		
		
		
	}
        
        
        

