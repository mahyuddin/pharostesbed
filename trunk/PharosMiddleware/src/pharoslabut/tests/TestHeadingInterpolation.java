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

	private FileLogger flogger = null;
	
	/**
	 * The constructor.
	 * 
	 * @param expFileName The experiment log file name.
	 * @param outputFileName The output file name.
	 */
	public TestHeadingInterpolation(String expFileName, String outputFileName) {
		
		FileLogger flogger = new FileLogger(outputFileName);
		RobotExpData robotData = new RobotExpData(expFileName);
		
		// First print the actual heading measurements.
		log("Actual headings:");
		Enumeration<HeadingState> e = robotData.getHeadingEnum();
		while (e.hasMoreElements()) {
			HeadingState currState = e.nextElement();
			flogger.log(currState.getTimestamp() + "\t" + currState.getHeading());
		}
		
		// Next print the interpolated headings.
		// Let's interpolate every 100ms.
		log("Interpolated headings:");
		for (long time = robotData.getStartTime(); time < robotData.getStopTime();
			time += 100) {
			flogger.log(time + "\t" + robotData.getHeading(time));
		}
	}
	
	private void log(String msg) {
		System.out.println(msg);
		if (flogger != null) 
			flogger.log(msg);
	}

	private void pause(int duration) {
		synchronized(this) {
			try {
				wait(duration);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void usage() {
		System.err.println("Usage: pharoslabut.tests.TestHeadingInterpolation <options>\n");
		System.err.println("Where <options> include:");
		System.err.println("\t-log <file name>: name of file in which to save results (required)");
		System.err.println("\t-output <file name>: the name of the file in which to save the interpolated heading (required)");
	}
	
	public static void main(String[] args) {
		String expLogFileName = null;
		String outputFileName = null;
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-output")) {
					outputFileName = args[++i];
				}
				if (args[i].equals("-log")) {
					expLogFileName = args[++i];
				} else if (args[i].equals("-h")) {
					usage();
					System.exit(0);
				} else {
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
