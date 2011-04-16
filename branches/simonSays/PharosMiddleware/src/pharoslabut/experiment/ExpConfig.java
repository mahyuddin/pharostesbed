package pharoslabut.experiment;

import java.util.*;

public class ExpConfig {
	
	/**
	 * The settings of each robot.
	 */
	private Vector<RobotExpSettings> robotSettings = new Vector<RobotExpSettings>();
	
	/**
	 * The start interval in milliseconds.
	 */
	private int startInterval = 0;
	
	/**
	 * The name of the experiment.
	 */
	private String expName = "Exp";
	
	public ExpConfig() {}
	
	public void addRobot(RobotExpSettings robot) {
		robotSettings.add(robot);
	}
	
	public int numRobots() {
		return robotSettings.size();
	}
	
	public RobotExpSettings getRobot(int indx) {
		if (indx >= numRobots())
			return null;
		else
			return robotSettings.get(indx);
	}
	
	public void setStartInterval(int startInterval) {
		this.startInterval = startInterval;
	}
	
	public int getStartInterval() {
		return startInterval;
	}
	
	public void setExpName(String expName) {
		this.expName = expName;
	}
	
	public String getExpName() {
		return expName;
	}
	
}
