package pharoslabut.demo.mrpatrol2.logAnalyzer;

import pharoslabut.navigate.Location;

public class BehaviorGoToLocationState {

	private Location destLoc;
	
	private double speed;
	
	private String name;
	
	public BehaviorGoToLocationState(String name, Location destLoc, double speed) {
		this.name = name;
		this.destLoc = destLoc;
		this.speed = speed;
	}
	
	public Location getDestLoc() {
		return destLoc;
	}
	
	public double getSpeed() {
		return speed;
	}
	
	public String getName() {
		return name;
	}
	
}
