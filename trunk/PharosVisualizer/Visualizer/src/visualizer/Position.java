/*
 * Position.java
 * Locates and updates position of robots
 * Lok Wong
 * Pharos Lab
 * Created: June 2, 2012 1:24 AM
 * Last Modified: June 9, 2012 7:59 PM
 */

package visualizer;

public class Position{

	public long time, delay;
	public double begLat, begLong, endLat, endLong, heading;
	
	public Position(){
		this.time = this.delay = 0;
		this.begLat = this.begLong = this.endLat = this.endLong = this.heading = 0;
	}
	
	public Position(long initTime, long initDelay, double initBegLat, double initBegLong, double initEndLat, double initEndLong, double initHeading){
		this.time = initTime;
		this.delay = initDelay;
		this.begLat = initBegLat;
		this.begLong = initBegLong;
		this.endLat = initEndLat;
		this.endLong = initEndLong;
		this.heading = initHeading;
	}
	
}
