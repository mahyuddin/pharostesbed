package pharoslabut.cartographer;

import java.util.*;

public class WorldView {
	static List<ArrayList<LocationElement>> world;
	
	
	public WorldView() {
		world = Collections.synchronizedList(new ArrayList<ArrayList<LocationElement>>());
		// go through each element and create a new LocationElement(x,y)
	}
	
	
	public synchronized void writeConfidence(int x, int y, double c) {
	synchronized (world) {
		// set (x,y).confidence = c;
	}
	}
	
	
	public synchronized double readConfidence(int x, int y) {
	synchronized (world) {
		// return confidence at (x,y);
		return 0; // placeholder
	}
	}
	

}


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
	
	
}