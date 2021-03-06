package pharoslabut.tests;

import java.util.*;
import pharoslabut.logger.FileLogger;
import pharoslabut.logger.analyzer.*;

/**
 * Tests the interpolation of a robot's heading.
 * 
 * @author Chien-Liang Fok
 */
public class TestHeadingInterpolation {
	
	/**
	 * The constructor.
	 * 
	 * @param expFileName The experiment log file name.
	 * @param outputFileName The output file name.
	 */
	public TestHeadingInterpolation(String expFileName, String outputFileName) {
		
		FileLogger flogger = new FileLogger(outputFileName, false);
		RobotExpData robotData = new RobotExpData(expFileName);
		
		long startTime = robotData.getStartTime();
		
		// First print the actual heading measurements.
		log("Time (ms)\tDelta Time (ms)\tActual headings:", flogger);
		Enumeration<HeadingState> e = robotData.getHeadingEnum();
		while (e.hasMoreElements()) {
			HeadingState currState = e.nextElement();
			log(currState.getTimestamp()  
					+ "\t" + (currState.getTimestamp() - startTime) 
					+ "\t" + currState.getHeading(), flogger);
		}
		
		// Next print the interpolated headings.
		// Let's interpolate every 100ms.
		log("Time (ms)\tDelta Time (ms)\tInterpolated headings:", flogger);
		for (long time = startTime; time < robotData.getStopTime();
			time += 100) {
			log(time 
					+ "\t" + (time - startTime) 
					+ "\t" + robotData.getHeading(time), flogger);
		}
	}
	
	private void log(String msg, FileLogger flogger) {
		System.out.println(msg);
		if (flogger != null) 
			flogger.log(msg);
	}

	private static void usage() {
		System.err.println("Usage: " + TestHeadingInterpolation.class.getName() + " <options>\n");
		System.err.println("Where <options> include:");
		System.err.println("\t-log <file name>: robot's experiment log file (required)");
		System.err.println("\t-output <file name>: the name of the file in which to save the interpolated heading (required)");
		System.err.println("\t-d or -debug: enable debug mode.");
	}
	
	public static void main(String[] args) {
		String expLogFileName = null;
		String outputFileName = null;
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-output")) {
					outputFileName = args[++i];
				}
				else if (args[i].equals("-log")) {
					expLogFileName = args[++i];
				} else if (args[i].equals("-h")) {
					usage();
					System.exit(0);
				} else if (args[i].equals("-debug") || args[i].equals("-d")) {
					System.setProperty ("PharosMiddleware.debug", "true");
				}  else {
					System.err.println("Unknown option: " + args[i]);
					usage();
					System.exit(1);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}		
		new TestHeadingInterpolation(expLogFileName, outputFileName);
	}
}
