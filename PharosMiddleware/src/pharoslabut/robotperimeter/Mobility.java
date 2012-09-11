package robotPerimeter;

import pharoslabut.beacon.WiFiBeaconBroadcaster;
import pharoslabut.exceptions.NoNewDataException;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.Location;
import pharoslabut.navigate.MotionArbiter;
import pharoslabut.navigate.NavigateCompassGPS;
import pharoslabut.sensors.CompassDataBuffer;
import pharoslabut.sensors.GPSDataBuffer;
import playerclient3.structures.gps.PlayerGpsData;


public class Mobility {
	
	MotionArbiter motionArbiter;
	CompassDataBuffer compassDataBuffer; 
	GPSDataBuffer gpsDataBuffer;
//	WiFiBeaconBroadcaster wifiBeaconBroadcaster;
	static NavigateCompassGPS nav;
	Intelligence intel;
	
	public Mobility(MotionArbiter motionArbiter, CompassDataBuffer compassDataBuffer, 
			GPSDataBuffer gpsDataBuffer/*, WiFiBeaconBroadcaster w*/)
	{
		this.motionArbiter = motionArbiter;
		this.compassDataBuffer = compassDataBuffer;
		this.gpsDataBuffer = gpsDataBuffer;
//		this.wifiBeaconBroadcaster = w;
		nav = new NavigateCompassGPS(motionArbiter, compassDataBuffer, gpsDataBuffer);
		intel = new Intelligence(0,10,10,10,20,10,0,0);
//		//int myId, int defaultRadius, int minRadius,
//		int maxRadius, double followingThreshold, double changeAngleBy,
//		double speed, double angle
//		System.out.printf("Created Mobility\n");
		Logger.log("Created Mobility\n");
//		Location cur = null;
//		while(cur==null)
//		{
//			try{
//				PlayerGpsData d = gpsDataBuffer.getCurrLoc();
//				cur = new Location(d);
//			}
//			catch(Exception e)
//			{
//				Logger.log("not yet");
//			}
//		}
//		Logger.log("going");
//		nav.go(cur, new Location(97.736773, 30.28669705), 5);
	}
	
	public static Location current;
	
	public void controlMotionAndIntelligence() throws InterruptedException
	{
		Logger.log("Started Motion control");
		long time = System.currentTimeMillis();
		int i=0;
		long currentTime = time;
		 current = null;
			//TODO remove fake
			current = new Location (0,0,0);

		class GoThread extends Thread {

			Location start;
			Location goal;
			double speed;
			
			public GoThread(Location start, Location goal, double speed){
				this.start = start;
				this.goal = goal;
				this.speed = speed;
			}
		    public void run() {
		    	nav.go(null, goal, speed);
		    	}
		}
		
		GoThread go = null;
		Logger.log("starting infinite loop");
		while (true)
		{
			currentTime = System.currentTimeMillis();
			
			Location newLocation = null;
			try{
			PlayerGpsData newLoc = nav.getLocation();// nav.getCurrLoc();
			 newLocation = new Location(newLoc);
			}
			catch(Exception e)
			{
				
			}
			if (newLocation !=null)	 current = newLocation;

			if (current == null){
				Logger.log("no location known...continuing\n");
				Thread.sleep(100);
				continue;
			}
			intel.updateLocationKnowledge(current);
						
			Location goal = intel.determineNewGoPosition();
			double speed = intel.setSpeed();
			
			if (go!=null)go.stop(); //TODO Find out if can do this safer
			go = new GoThread(current,goal,speed);
			go.start();

			Logger.log(String.format("Intelligence Done...Starting Again\ncurrent location %f %f\ngoal: %f %f",current.latitude(), current.longitude(), goal.latitude(), goal.longitude()));
			Thread.sleep(1000);
		}
	}
}


