package pharoslabut.demo.mrpatrol2.logAnalyzer;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import pharoslabut.logger.Logger;
import pharoslabut.logger.analyzer.PathEdge;
import pharoslabut.navigate.Location;
import pharoslabut.util.Stats;

public class AnalyzeWaypointIdleAndWaitTime {

	Vector<RobotExpData> robots = new Vector<RobotExpData>();
	
	public AnalyzeWaypointIdleAndWaitTime() {
		// Get all of the robot logs from the experiment.
		FilenameFilter filter = new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return !name.startsWith(".") && name.contains("-MRPatrol2_") && name.contains(".log");
		    }
		};
		
		File dir = new File(".");

		String[] logFiles = dir.list(filter);
		if (logFiles == null) {
		    System.err.println("No files found.");
		    System.exit(1);
		} else {
		    for (int i=0; i<logFiles.length; i++) {
		    	String robotFileName = "./" + logFiles[i];
		    	Logger.logDbg("Reading robot log " + robotFileName);
		        robots.add(new RobotExpData(robotFileName));
		    }
		}
		
		analyzeIdleTime();
		analyzeWaitTime();
	}
	
	private void analyzeIdleTime() {
		DecimalFormat df = new DecimalFormat("#.##");
		Hashtable<String, Vector<Long>> data = new Hashtable<String, Vector<Long>>();
		
		for (int i= 0; i < robots.size(); i++) {
			RobotExpData currRobot = robots.get(i);
			long patrolStartTime = currRobot.getPatrolStartTime();
			
			Vector<PathEdge> edges = currRobot.getPathEdges();
			
			// do not consider the first and last edges since those are going to and from the patrol route.
			for (int j = 1; j < edges.size() - 1; j++) {  
				PathEdge currEdge = edges.get(j);
				if (currEdge.getStartTime() > patrolStartTime) {
					Location endLocation = currEdge.getEndLocation();
					String waypointName = currRobot.getWaypointName(endLocation);
					if (!data.containsKey(waypointName)) {
//						Logger.logDbg("Creating key " + endLocation);
						data.put(waypointName, new Vector<Long>());
					}
					Vector<Long> timestamps = data.get(waypointName);
					timestamps.add(currEdge.getEndTime());
				}
			}
		}
		
//		StringBuffer sb = new StringBuffer(data.keySet().size() + " Keys:");
//		Enumeration<Location> e = data.keys();
//		while(e.hasMoreElements()) {
//			sb.append("\n\t" + e.nextElement());
//		}
//		Logger.logDbg(sb.toString());
		
		Vector<Double> allData = new Vector<Double>();
		
		System.out.println("\nWaypoint, Idle Time, 95% Conf.");
		// sort each waypoint's visit time
		Enumeration<String> keys = data.keys();
		while (keys.hasMoreElements()) {
			String currWayPoint = keys.nextElement();
			Vector<Long> visitTimes = data.get(currWayPoint);
			Collections.sort(visitTimes);
			
//			StringBuffer sb = new StringBuffer("Visit times for waypoint " + currLoc + ":");
//			for (int i = 0; i < visitTimes.size(); i++) {
//				sb.append("\n\t" + visitTimes.get(i));
//			}
//			Logger.log(sb.toString());
			
			Vector<Double> result = new Vector<Double>();
			for (int i=1; i < visitTimes.size(); i++) {
				long delta = visitTimes.get(i) - visitTimes.get(i-1);
				result.add(delta / 1000.0);
				allData.add(delta / 1000.0);
			}
			
			System.out.println(currWayPoint + ", " + df.format(Stats.getAvg(result)) + ", " + df.format(Stats.getConf95(result)));
		}
		System.out.println("Overall, " + df.format(Stats.getAvg(allData)) 
				+ ", " + df.format(Stats.getConf95(allData)));
		
		Collections.sort(allData);
		System.out.println("Max, " + allData.get(allData.size() - 1));
		System.out.println("Min, " + allData.get(0));
	}
	
	private void analyzeWaitTime() {
		
	}
	
	public static final void main(String[] args) {
		System.setProperty ("PharosMiddleware.debug", "true");
		new AnalyzeWaypointIdleAndWaitTime();
	}
}
