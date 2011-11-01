package pharoslabut.logger.analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;

import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;

/**
 * Analyzes the duration of log files within a particular directory.
 * 
 * @author Chien-Liang Fok
 */
public class LogDurationAnalyzer {
	
	private String expName;
	private DecimalFormat df = new DecimalFormat("#.#");

	public LogDurationAnalyzer(String expDir) {
		// Parse out the name of the experiment.  The name of the experiment should follow
		// the following format: "M##-Exp##".
		String absExpDir = new File(expDir).getAbsolutePath();
		String[] tokens = absExpDir.split("/");
		for (int i=0; i < tokens.length; i++) {
			if (tokens[i].matches("M\\d+-Exp\\d+")) {
				expName = tokens[i];
				Logger.logDbg("Found experiment name \"" + expName + "\", expDir = " + expDir);
			}
		}
		
		if (expName == null) {
			Logger.logErr("Unable to determine experiment name, expDir = " + expDir);
			System.exit(1);
		}
		
		// Get all of the robot logs from the experiment.
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
			long minDuration = Long.MAX_VALUE;
			System.out.println("Mission\tExperiment\tRobot\tDuration (s)");
		    for (int i=0; i<logFiles.length; i++) {
		    	String robotFileName = expDir + "/" + logFiles[i];
		    	long currDur = analyzeDuration(robotFileName);
		    	if (currDur < minDuration)
		    		minDuration = currDur;
		    }
		    System.out.println("Minimum duration: " + df.format(minDuration / 1000.0));
		}
	}
	
	/**
	 * Calculates the duration of the log file.
	 * 
	 * @param robotFileName The log file.
	 * @return The duration of the log file.
	 */
	private long analyzeDuration(String robotFileName) {
		String[] tokens = robotFileName.split("-");
		String missionName = tokens[0];
		String expName = tokens[1];
		String robotName = tokens[2];
		
		// remove leading "./" characters
		missionName = missionName.substring(missionName.indexOf('M'));
		
		File f = new File(robotFileName);
		String firstLine = head(f);
		String lastLine = tail(f);
		
		if (firstLine == null) {
			Logger.logErr("File " + robotFileName + " has no first line.");
			System.exit(1);
		}
		
		if (lastLine == null) {
			Logger.logErr("File " + robotFileName + " has no last line.");
			System.exit(1);
		}
		
		long startTime = Long.valueOf(firstLine.substring(1, firstLine.indexOf(']')));
		long stopTime = Long.valueOf(lastLine.substring(1, lastLine.indexOf(']')));
		
		long duration = stopTime - startTime;
		
		
		System.out.println(missionName + "\t" + expName + "\t" + robotName + "\t" + df.format(duration / 1000.0));
		return duration;
	}
	
	/**
	 * Returns the first line of a file.
	 * 
	 * @param file The file
	 * @return the first line of the file.
	 */
	private String head(File file) {
		try {
			BufferedReader in
			   = new BufferedReader(new FileReader(file));
			return in.readLine();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Returns the last non-blank line of a file.
	 * 
	 * @param file The file
	 * @return the last non-blank line of the file.
	 * @see http://stackoverflow.com/questions/686231/java-quickly-read-the-last-line-of-a-text-file
	 */
	private String tail( File file ) {
	    try {
	        RandomAccessFile fileHandler = new RandomAccessFile( file, "r" );
	        long fileLength = file.length() - 1;
	        StringBuilder sb = new StringBuilder();

	        for( long filePointer = fileLength; filePointer != -1; filePointer-- ) {
	            fileHandler.seek( filePointer );
	            int readByte = fileHandler.readByte();

	            if( readByte == 0xA ) {
	                if( filePointer == fileLength ) {
	                    continue;
	                } else {
	                    break;
	                }
	            } else if( readByte == 0xD ) {
	                if( filePointer == fileLength - 1 ) {
	                    continue;
	                } else {
	                    break;
	                }
	            }

	            sb.append( ( char ) readByte );
	        }

	        String lastLine = sb.reverse().toString();
	        return lastLine;
	    } catch( java.io.FileNotFoundException e ) {
	        e.printStackTrace();
	        return null;
	    } catch( java.io.IOException e ) {
	        e.printStackTrace();
	        return null;
	    }
	}

	
	private static void usage() {
		System.setProperty ("PharosMiddleware.debug", "true");
		System.out.println("Usage: " + LogDurationAnalyzer.class.getName() + " <options>\n");
		System.out.println("Where <options> include:");
		System.out.println("\t-expDir <experiment data directory>: The directory containing experiment data (required)");
		System.out.println("\t-log <log file name>: The file in which to log debug statements (default null)");
		System.out.println("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		String expDir = null;
		
		// Process the command line arguments...
		try {
			for (int i=0; i < args.length; i++) {
		
				if (args[i].equals("-log"))
					Logger.setFileLogger(new FileLogger(args[++i], false));
				else if (args[i].equals("-expDir"))
					expDir = args[++i];
				else if (args[i].equals("-debug") || args[i].equals("-d"))
					System.setProperty ("PharosMiddleware.debug", "true");
				else {
					usage();
					System.exit(1);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			usage();
			System.exit(1);
		}
		
		if (expDir == null) {
			System.err.println("Must specify expDir!");
			usage();
			System.exit(1);
		}
		
		new LogDurationAnalyzer(expDir);
	}
}
