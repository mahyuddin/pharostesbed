package pharoslabut.cartographer;

public class PathPlannerThread extends Thread {

	private static boolean running = false;
	private int executionTime = 180;
	private OrderedPair startingCoordinates = null;
	private double initialBearing = 0;
	
	public PathPlannerThread (int time, OrderedPair sc, double ib) {
		this.executionTime = time;  
		this.startingCoordinates = sc;
		this.initialBearing = ib;	
	}
	
	@Override
	public void run() {
		if (!PathPlannerThread.running) {
			PathPlannerThread.running = true;
			PathPlanner.beginPathPlanner(this.executionTime, this.startingCoordinates, this.initialBearing);
		} else
			System.out.println("PathPlanner thread already executing. " +
				"\nCreating multiple PathPlanner threads is forbidden.");
	}
	
	
	public static boolean isRunning() { return PathPlannerThread.running; }

}
