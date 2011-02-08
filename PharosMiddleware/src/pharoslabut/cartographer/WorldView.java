package pharoslabut.cartographer;

import java.util.*;

class LocationElement {
	private int xCoord;
	private int yCoord;
	private double confidence; // percentage 
	private double elevation;
	
	public LocationElement (int x, int y) {
		this.xCoord = x;
		this.yCoord = y;
		this.confidence = .5;  // initialized to 50% 
		this.elevation = 0;    // initialized to 0, floor level		
	}	
	
	/**************** GETTERS AND SETTERS ******************/
	public int getxCoord() { return xCoord; }
	public void setxCoord(int x) { this.xCoord = x;	}
	public int getyCoord() { return yCoord;	}
	public void setyCoord(int y) { this.yCoord = y;	}
	public double getConfidence() {	return confidence; }
	public void setConfidence(double c) { this.confidence = c; }
	public double getElevation() { return elevation; }
	public void setElevation(double e) { this.elevation = e; }
	/*************** END GETTERS AND SETTERS ****************/
	
}

public class WorldView {
	private static List<ArrayList<LocationElement>> world;
	
	
	public WorldView() {
		world = Collections.synchronizedList(new ArrayList<ArrayList<LocationElement>>());
		for (int i = 0; i < 100; i++) { // iterate through each x coordinate
 
			//add a new list for all the y coordinates at that x coordinate
			world.add(new ArrayList<LocationElement>());  
			
			for (int j = 0; j < 100; j++) { // iterate through each y coordinate
				world.get(i).add(j, new LocationElement(i,j)); // add 	
			}	
		}
	}
	
	
	public static synchronized void writeConfidence(int x, int y, double c) {
	synchronized (world) {
		// set (x,y)'s confidence = c
		world.get(x).get(y).setConfidence(c);
	}
	}
	
	
	public static synchronized double readConfidence(int x, int y) {
	synchronized (world) {
		// return confidence at (x,y);
		return world.get(x).get(y).getConfidence();
	}
	}
	

}




