package pharoslabut.demo.mrpatrol2.logAnalyzer;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;
import pharoslabut.logger.analyzer.PathEdge;
import pharoslabut.navigate.Location;
import pharoslabut.util.Stats;

public class AnalyzeWaypointIdleAndWaitTime {

	Vector<RobotExpData> robots = new Vector<RobotExpData>();
	DecimalFormat df = new DecimalFormat("#.###");
	
	
	
	/**
	 * 
	 * @param args An array of experiment directories to analyze.
	 */
	public AnalyzeWaypointIdleAndWaitTime(String[] args) {
		
		// Get all of the robot logs from the experiment.
		FilenameFilter logFileFilter = new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return !name.startsWith(".") && name.contains("-MRPatrol2_") && name.contains(".log");
		    }
		};
		
		Vector<Result> idleResults = new Vector<Result>();
		Vector<Result> waitResults = new Vector<Result>();
		
		for (int d = 0; d < args.length; d++) {
			Logger.logDbg("Analyzing experiment " + args[d]);
			
			File dir = new File("./" + args[d] + "/");
			String prefix = null;
			
			String[] logFiles = dir.list(logFileFilter);
			if (logFiles == null) {
			    System.err.println("No files found.");
			    System.exit(1);
			} else {
			    for (int i=0; i<logFiles.length; i++) {
			    	String robotFileName = "./" + args[d] + "/" + logFiles[i];
			    	Logger.logDbg("Reading robot log " + robotFileName);
			        robots.add(new RobotExpData(robotFileName));
			        
			        if (prefix == null) {
			        	String[] tokens = logFiles[i].split("[_-]");
			        	prefix = tokens[0] + "_" + tokens[1];
			        }
			    }
			}
			
			Result resultIdle = analyzeIdleTime();
			Result resultWait = analyzeWaitTime();

			resultIdle.expName = prefix;
			resultWait.expName = prefix;
			
			idleResults.add(resultIdle);
			waitResults.add(resultWait);
		}
		
		System.out.println("\nIdle time results:");
		System.out.println("Experiment, Average (s), 95% Conf., Min., Max.");
		Enumeration<Result> e = idleResults.elements();
		while (e.hasMoreElements()) {
			Result r = e.nextElement();
			System.out.println(r.toString());
		}
		
		System.out.println("\nWait time results:");
		System.out.println("Experiment, Average (s), 95% Conf., Min., Max.");
		e = waitResults.elements();
		while (e.hasMoreElements()) {
			Result r = e.nextElement();
			System.out.println(r.toString());
		}
	}
	
	private Result analyzeIdleTime() {
		
		// This hashtable maps the waypoint name to the times it was visited.
		Hashtable<String, Vector<Long>> data = new Hashtable<String, Vector<Long>>();
		
		/*
		 * For each robot, go through each edge it traversed within the patrol route
		 * (i.e., disregard the edges to and from the patrol route).  Record
		 * the time the robot reached the waypoint at the end of the edge.  Store
		 * these timestamps in a hashtable that maps the waypoint's name to the
		 * times it was visited.
		 */
		for (int i= 0; i < robots.size(); i++) {
			RobotExpData currRobot = robots.get(i);
			Vector<PathEdge> edges = currRobot.getPathEdges();
			
			// do not consider the first and last edges since those are going to and from the patrol route.
			for (int j = 1; j < edges.size() - 1; j++) {  
				PathEdge currEdge = edges.get(j);
				if (currEdge.getStartTime() > currRobot.getPatrolStartTime()) {
					Location endLocation = currEdge.getEndLocation();
					String waypointName = currRobot.getWaypointName(endLocation);
					if (!data.containsKey(waypointName))
						data.put(waypointName, new Vector<Long>());
					Vector<Long> timestamps = data.get(waypointName);
					timestamps.add(currEdge.getEndTime());
				}
			}
		}
		
		/*
		 * This stores all of the waypoint idle times among all waypoints and 
		 * all robots.
		 */
		Vector<Double> allData = new Vector<Double>();
		
		StringBuffer sb = new StringBuffer("Waypoint, Average Idle Time (s), 95% Conf.");
		
		/*
		 * For each waypoint, sort the times it was visited and compute
		 * the amount of time between visits.  Calculate the average, max, and min
		 * of these time intervals.
		 */
		Enumeration<String> keys = data.keys();
		while (keys.hasMoreElements()) {
			String currWayPoint = keys.nextElement();
			Vector<Long> visitTimes = data.get(currWayPoint);
			Collections.sort(visitTimes);
			
			Vector<Double> result = new Vector<Double>();
			for (int i=1; i < visitTimes.size(); i++) {
				long delta = visitTimes.get(i) - visitTimes.get(i-1);
				result.add(delta / 1000.0); // convert to seconds
				allData.add(delta / 1000.0); // convert to seconds
			}
			
			sb.append("\n" + currWayPoint + ", " + df.format(Stats.getAvg(result)) + ", " + df.format(Stats.getConf95(result)));
		}
		
		Collections.sort(allData);
		
		Result result = new Result(Stats.getAvg(allData), Stats.getConf95(allData), allData.get(0), allData.get(allData.size() - 1));
		sb.append("\n\nOverall Average Idle Time (s), " + df.format(result.avg) 
				+ ", " + df.format(result.conf95));
		sb.append("\nOverall Max Idle Time (s), " + result.max);
		sb.append("\nOverall Min Idle Time (s), " + result.min);
		
		//FileLogger floggerIdle = new FileLogger(prefix + "-Idle_Times.txt", false);
		//floggerIdle.log(sb.toString());
		System.out.println("\n" + sb.toString());
		
		return result;
	}
	
	private Result analyzeWaitTime() {
		// This hashtable maps the waypoint name to the times robots waited at it.
		Hashtable<String, Vector<Double>> data = new Hashtable<String, Vector<Double>>();
		
		for (int i= 0; i < robots.size(); i++) {
			RobotExpData currRobot = robots.get(i);
			
			Vector<PathEdge> edges = currRobot.getPathEdges();
			
			// do not consider the first and last edges since those are going to and from the patrol route.
			for (int j = 2; j < edges.size() - 1; j++) {  
				PathEdge currEdge = edges.get(j);
				PathEdge prevEdge = edges.get(j-1);
				
				if (currEdge.getStartTime() > currRobot.getPatrolStartTime()) {
					Location waypointLoc = prevEdge.getEndLocation();
					String waypointName = currRobot.getWaypointName(waypointLoc);
					if (!data.containsKey(waypointName))
						data.put(waypointName, new Vector<Double>());
					Vector<Double> waitTimes = data.get(waypointName);
					waitTimes.add((currEdge.getStartTime() - prevEdge.getEndTime()) / 1000.0);
				}
			}
		}
		
		Vector<Double> allData = new Vector<Double>();
		
		StringBuffer sb = new StringBuffer("Waypoint, Avg. Wait Time (s), 95% Conf.");
		// sort each waypoint's visit time
		Enumeration<String> keys = data.keys();
		while (keys.hasMoreElements()) {
			String currWayPoint = keys.nextElement();
			Vector<Double> waitTimes = data.get(currWayPoint);
			
			allData.addAll(waitTimes);
			
			sb.append("\n" + currWayPoint + ", " + df.format(Stats.getAvg(waitTimes)) + ", " + df.format(Stats.getConf95(waitTimes)));
		}
		
		Collections.sort(allData);
		
		Result result = new Result(Stats.getAvg(allData), Stats.getConf95(allData), allData.get(0), allData.get(allData.size() - 1));
		
		sb.append("\n\nOverall Avg. Wait Time (s), " + df.format(result.avg) + ", " + df.format(result.conf95));
		sb.append("\nOverall Max Wait Time (s), " + df.format(result.max));
		sb.append("\nOverall Min Wait Time (s), " + df.format(result.min));
		
		//FileLogger floggerWait = new FileLogger(prefix + "-Wait_Times.txt", false);
		//floggerWait.log(sb.toString());
		System.out.println("\n" + sb.toString());
		
		return result;
	}
	
	private class Result {
		String expName;
		double avg, conf95, min, max;
		
		public Result(double avg, double conf95, double min, double max) {
			this.avg = avg;
			this.conf95 = conf95;
			this.min = min;
			this.max = max;
		}
		
		public String toString() {
			return expName + ", " + df.format(avg) + ", " + df.format(conf95) + ", " + df.format(min) + ", " + df.format(max);
		}
	}
	
	public static final void main(String[] args) {
		System.setProperty ("PharosMiddleware.debug", "true");
		new AnalyzeWaypointIdleAndWaitTime(args);
	}
}
