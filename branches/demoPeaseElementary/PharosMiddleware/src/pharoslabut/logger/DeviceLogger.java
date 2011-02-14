package pharoslabut.logger;

/**
 * Defines the interface for all classes that log data collected abou a device.
 * 
 * @author Chien-Liang Fok
 */
public interface DeviceLogger {
	
	/**
	 * Starts the logging process.
	 * 
	 * @param period The period at which to log the data in milliseconds.
	 * @param fileName The file in which to save the data.
	 * @return true if the logging was started, false otherwise.
	 */
	public boolean start(int period, String fileName);
	
	/**
	 * Returns the logging period.
	 * 
	 * @return The period in milliseconds.
	 */
	public int getPeriod();
	
	/**
	 * Stops the logging process.
	 */
	public void stop();
}
