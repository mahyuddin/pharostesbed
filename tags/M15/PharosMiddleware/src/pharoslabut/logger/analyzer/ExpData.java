package pharoslabut.logger.analyzer;

import java.util.*;
import java.io.*;

/**
 * Encapsulates the data recorded during an experiment.  An experiment
 * consists of one or more robots following motion scripts.
 * 
 * @author Chien-Liang Fok
 */
public class ExpData {
	
	private Vector<RobotExpData> robots = new Vector<RobotExpData>();
	
	/**
	 * The constructor.
	 * 
	 * @param expDir The directory containing the experiment log files.
	 */
	public ExpData(String expDir) {
	
		FilenameFilter filter = new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return !name.startsWith(".") && name.contains("-Pharos_") && name.contains(".log");
		    }
		};
		
		File dir = new File(expDir);

		String[] logFiles = dir.list(filter);
		if (logFiles == null) {
		    System.err.println("No files found.");
		    System.exit(1);
		} else {
		    for (int i=0; i<logFiles.length; i++) {
		        //String filename = logFiles[i];
		        robots.add(new RobotExpData(expDir + "/" + logFiles[i]));
		    }
		}
	}
	
	public int numRobots() {
		return robots.size();
	}
	
	
	
	public static final void main(String[] args) {
		new ExpData(args[0]);
	}
}
