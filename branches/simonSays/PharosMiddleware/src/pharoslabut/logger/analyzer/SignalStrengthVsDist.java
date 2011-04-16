package pharoslabut.logger.analyzer;

/**
 * This analyzes the log files from and experiment and extracts the signal
 * strength and distance data.
 * 
 * It produces a log file with the following format
 * 
 * @author Chien-Liang Fok
 */
public class SignalStrengthVsDist {
	
	ExpData expData;
	
	public SignalStrengthVsDist(String expDir) {
		expData = new ExpData(expDir);
	}
	
	public void analyzeTelosBSignal() {
		
	}
}
