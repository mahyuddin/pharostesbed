package pharoslabut.logger.analyzer.cbl.rssidist;

import pharoslabut.RobotIPAssignments;

/**
 * Convert RSSI measurements into distance for Mission 15, Experiment 9.  Uses an exponential
 * function to curve fit the actual RSSI vs. Distance readings.
 * 
 * From the plot of average distance vs. RSSI, we see that any RSSI reading less than about -86dBm
 * has very poor correlation with distance.
 * 
 * NOTE: This appears to always overestimate the distance!  Not sure why as of 03/22/2011.
 * 
 * @author Chien-Liang Fok
 * @see http://pharos.ece.utexas.edu/wiki/index.php/Mission_15_-_February_15,_2011_-_Dell_Diamond#Experiment_9
 */
public class RSSIToDistM15Exp9 extends RSSItoDist {
	
	
	public RSSIToDistM15Exp9() {
		super(null);
	}
	
	public double getDist(int robot1ID, int robot2ID, double rssi) {
		
		if (rssi == -1)
			return -1;
		
		rssi *= -1; // because the exponential functions are inverse RSSI...
		
		if ((robot1ID == RobotIPAssignments.SHINER && robot2ID == RobotIPAssignments.WYNKOOP)
				|| (robot1ID == RobotIPAssignments.WYNKOOP && robot2ID == RobotIPAssignments.SHINER)) 
		{
			return 0.1888 * Math.exp(0.0566 * rssi);
			//return 0.1034 * Math.exp(0.0639 * rssi);
		} else if ((robot1ID == RobotIPAssignments.SHINER && robot2ID == RobotIPAssignments.ZIEGEN)
				|| (robot1ID == RobotIPAssignments.ZIEGEN && robot2ID == RobotIPAssignments.SHINER)) 
		{
			return 0.0334 * Math.exp(0.0799 * rssi);
			//return 0.0248 * Math.exp(0.0832 * rssi);
		} else if ((robot1ID == RobotIPAssignments.ZIEGEN && robot2ID == RobotIPAssignments.WYNKOOP)
				|| (robot1ID == RobotIPAssignments.WYNKOOP && robot2ID == RobotIPAssignments.ZIEGEN)) 
		{
			return 0.0335 * Math.exp(0.0804 * rssi);
			//return 0.0252 * Math.exp(0.0868 * rssi);
		} else {
			System.err.println("Unable to get distance from rssi of robots " + robot1ID + " and " + robot2ID);
			System.exit(1);
		}
		return -1;
	}
	
	
	public static void main(String[] args) {
		//int robot1ID = 17; // shiner
		int robot1ID = 18; // ziegen
		int robot2ID = 25; // wynkoop
		//int robot2ID = 18; // ziegen
		
		RSSItoDist rssi2dist = new RSSIToDistM15Exp9();
		
		System.out.println("Inv. RSSI \t Distance");
		for (int rssi = 55; rssi < 90; rssi++) {
			System.out.println(rssi + "\t" + rssi2dist.getDist(robot1ID, robot2ID, -1*rssi));
		}
	}

}
