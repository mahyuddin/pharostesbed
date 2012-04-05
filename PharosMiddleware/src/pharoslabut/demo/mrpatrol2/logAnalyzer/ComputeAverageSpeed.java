package pharoslabut.demo.mrpatrol2.logAnalyzer;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

//import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;
import pharoslabut.util.Stats;

public class ComputeAverageSpeed {

	DecimalFormat df = new DecimalFormat("#.###");

	/**
	 * @param args An array of experiment directories to analyze.
	 */
	public ComputeAverageSpeed(String[] args) {
		
		// Get all of the robot logs from the experiment.
		FilenameFilter logFileFilter = new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return !name.startsWith(".") && name.contains("-MRPatrol2_") && name.contains(".log");
		    }
		};
		
		Vector<Result> results = new Vector<Result>();
		
		for (int d = 0; d < args.length; d++) {
			Logger.logDbg("Analyzing experiment " + args[d]);
			
			File dir = new File("./" + args[d] + "/");
			String prefix = null;
			
			String[] logFiles = dir.list(logFileFilter);
			if (logFiles == null) {
			    System.err.println("No files found.");
			    System.exit(1);
			} else {
				Vector<RobotExpData> robots = new Vector<RobotExpData>();
				
			    for (int i=0; i<logFiles.length; i++) {
			    	String robotFileName = "./" + args[d] + "/" + logFiles[i];
			    	Logger.logDbg("Reading robot log " + robotFileName);
			        robots.add(new RobotExpData(robotFileName));
			        
			        if (prefix == null) {
			        	String[] tokens = logFiles[i].split("[_-]");
			        	prefix = tokens[0] + "_" + tokens[1];
			        }
			    }
			    
			    Result result = analyzeAverageSpeed(robots);
				result.expName = prefix;
				results.add(result);
			}
		}
		
		System.out.println("Experiment, Average Speed (m/s), 95% Conf., Min., Max.");
		Enumeration<Result> e = results.elements();
		while (e.hasMoreElements()) {
			Result r = e.nextElement();
			System.out.println(r.toString());
		}
	}
	
	private Result analyzeAverageSpeed(Vector<RobotExpData> robots) {
		
		// This hashtable maps the robot's name to its measured speeds.
		Hashtable<String, Vector<Double>> data = new Hashtable<String, Vector<Double>>();
		
		/*
		 * For each robot, compute the speed it was traveling at as it patrolled the route.
		 * Save this information in the data hashtable.
		 */
		for (int i= 0; i < robots.size(); i++) {
			RobotExpData currRobot = robots.get(i);
			Vector<Double> speedHist = new Vector<Double>();
			for (long time = currRobot.getPatrolStartTime(); time < currRobot.getPatrolEndtime(); time += 1000) {
				double speed = currRobot.getSpeed(time);
				speedHist.add(speed);
			}
			data.put(currRobot.getRobotName(), speedHist);
		}
		
		/*
		 * This stores all of the speed measurements of
		 * all robots.
		 */
		Vector<Double> allData = new Vector<Double>();
		
		StringBuffer sb = new StringBuffer("Robot, Speed (m/s), 95% Conf., Min (m/s), Max (m/s)");
		
		/*
		 * For each waypoint, sort the times it was visited and compute
		 * the amount of time between visits.  Calculate the average, max, and min
		 * of these time intervals.
		 */
		Enumeration<String> keys = data.keys();
		while (keys.hasMoreElements()) {
			String currRobotName = keys.nextElement();
			Vector<Double> speeds = data.get(currRobotName);
			Collections.sort(speeds);
			allData.addAll(speeds);
			sb.append("\n" + currRobotName + ", " + df.format(Stats.getAvg(speeds)) + ", " + df.format(Stats.getConf95(speeds)) 
					+ ", " + df.format(speeds.get(0)) + ", " + df.format(speeds.get(speeds.size() - 1)));
		}
		
		Collections.sort(allData);
		
		Result result = new Result(Stats.getAvg(allData), Stats.getConf95(allData), allData.get(0), allData.get(allData.size() - 1));
		
		sb.append("\n\nOverall Average Idle Time (s), " + df.format(result.avg) 
				+ ", " + df.format(result.conf95));
		sb.append("\nOverall Max Idle Time (s), " + result.max);
		sb.append("\nOverall Min Idle Time (s), " + result.min + "\n");
		
		//FileLogger floggerIdle = new FileLogger(prefix + "-Idle_Times.txt", false);
		//floggerIdle.log(sb.toString());
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
		new ComputeAverageSpeed(args);
	}
}
