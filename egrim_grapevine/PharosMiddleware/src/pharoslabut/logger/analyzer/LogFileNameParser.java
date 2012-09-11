package pharoslabut.logger.analyzer;

/**
 * Provides functions for extracting useful information from the name of a log file.
 * 
 * @author Chien-Liang Fok
 */
public class LogFileNameParser {

	/**
	 * Finds the name of the log file, determines whether it fits template
	 * "[mission name]##-Exp##-RobotName-Pharos_##.log",
	 * and return a tokenized version of the log file.  The first token is
	 * the mission name, the second token is the experiment name, and the
	 * last token is the robot name.
	 * 
	 * 
	 * @param logFileName The name of the log file.  May include the full path to it.
	 * @return The tokenized version of the log file name.
	 */
	private static String[] tokenizeLogFileName(String logFileName) {
		String[] pathTokens = logFileName.split("/");
		for (int i=0; i < pathTokens.length; i++) {
			String currToken = pathTokens[i];
			if (currToken.endsWith(".log")) {
				String[] fileNameTokens = currToken.split("-|_");
				if (fileNameTokens.length > 3 && fileNameTokens[0].matches("\\D+\\d+") && 
						fileNameTokens[1].matches("Exp\\d+")) 
				{
					return fileNameTokens;
				}
			}
		}
		return null;
	}
	
	/**
	 * Extract the robot's name from it's log file.  
	 * Assumes the log file is of form "[mission name]##-Exp##-RobotName-Pharos_##.log"
	 * May include the path to the log file.
	 * 
	 * @param logFileName The name of the log file
	 * @return The name of the robot.
	 */
	public static String extractRobotName(String logFileName) {
		String[] fileNameTokens = tokenizeLogFileName(logFileName);
		
		if (fileNameTokens == null) {
			System.err.println("ERROR: Unable to determine robot name: " + logFileName);
			new Exception().printStackTrace();
			System.exit(1);
		}
		
		return fileNameTokens[2];
	}
	
	/**
	 * Extract the mission name from an experiment's log file.  
	 * Assumes the log file is of form "[mission name]##-Exp##-RobotName-Pharos_##.log"
	 * May include the path to the log file.
	 * 
	 * @param logFileName The name of the log file
	 * @return The name of the mission.
	 */
	public static String extractMissionName(String logFileName) {
		String[] fileNameTokens = tokenizeLogFileName(logFileName);
		
		if (fileNameTokens == null) {
			System.err.println("ERROR: Unable to determine mission name: " + logFileName);
			new Exception().printStackTrace();
			System.exit(1);
		}
		
		return fileNameTokens[0];
	}
	
	/**
	 * Extract the mission name from an experiment's log file.  
	 * Assumes the log file is of form "[mission name]##-Exp##-RobotName-Pharos_##.log"
	 * May include the path to the log file.
	 * 
	 * @param logFileName The name of the log file
	 * @return The name of the mission.
	 */
	public static String extractExpName(String logFileName) {
		String[] fileNameTokens = tokenizeLogFileName(logFileName);
		
		if (fileNameTokens == null) {
			System.err.println("ERROR: Unable to determine experiment name: " + logFileName);
			new Exception().printStackTrace();
			System.exit(1);
		}
		
		return fileNameTokens[1];
	}
}
