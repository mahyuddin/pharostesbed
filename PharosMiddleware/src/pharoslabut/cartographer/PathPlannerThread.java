package pharoslabut.cartographer;

public class PathPlannerThread extends Thread {

	private static boolean running = false;
	private int executionTime = 180;
	
	public PathPlannerThread (int time) {
		this.executionTime = time;  
	}
	
	@Override
	public void run() {
		if (!PathPlannerThread.running) {
			PathPlannerThread.running = true;
			PathPlanner.beginPathPlanner(this.executionTime);
		} else
			System.out.println("PathPlanner thread already executing. " +
				"\nCreating multiple PathPlanner threads is forbidden.");
	}

}
