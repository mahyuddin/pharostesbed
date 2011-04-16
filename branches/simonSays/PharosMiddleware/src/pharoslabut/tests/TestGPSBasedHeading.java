package pharoslabut.tests;

import java.io.*;
import pharoslabut.navigate.*;
import pharoslabut.logger.*;

/**
 * Evaluates how the heading derived from GPS data compares with the heading
 * provided by a compass.  It showed that using the previous and current GPS
 * readings alone are insufficient for determining robot heading.
 * 
 * @author Chien-Liang Fok
 *
 */
public class TestGPSBasedHeading {

	Location currLoc, prevLoc;
	//Location targetLoc;
	double currCompassHeading;
	
	FileLogger flogger = new FileLogger("test-heading.txt");
	
	/**
	 * The constructor.
	 * 
	 * @param fileName The name of the file containing the log from a robot
	 * following a GPS-based motion script. 
	 * @throws IOException 
	 */
	public TestGPSBasedHeading(String fileName) throws IOException {
		File file = new File(fileName);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = null;
		while (( line = br.readLine()) != null){
			if (line.contains("Current Location")) {
				String[] tokens = line.split("[(,)]");
				saveCurrentLocation(new Location(Double.valueOf(tokens[1]), Double.valueOf(tokens[2])));
			} 
//			else if (line.contains("Target Location")) {
//				String[] tokens = line.split("[(,)]");
//				targetLoc = new Location(Double.valueOf(tokens[1]), Double.valueOf(tokens[2]));
//			} 
			else if (line.contains("Heading Error")) {
				String[] tokens = line.split(" ");
				currCompassHeading = Double.valueOf(tokens[2]);
				doCalculation();
			}
		}
	}
	
	private void saveCurrentLocation(Location currLoc) {
		if (this.currLoc != null) {
			if (!this.currLoc.equals(currLoc)) 
				this.prevLoc = this.currLoc;
		}
		this.currLoc = currLoc;
	}
	
	/**
	 * Only do the calculation if we have a current and previous GPS location.
	 */
	private void doCalculation() {
		if (prevLoc != null && currLoc != null) {
			double gpsBasedHeading = Navigate.angle(prevLoc, currLoc);
			String result = "currLoc=" + currLoc + "\nprevLoc=" + prevLoc + "\n" + gpsBasedHeading + "\t" + currCompassHeading;
			System.out.println(result);
			flogger.log(result);
		}
	}
	
	public static final void main(String[] args) {
		try {
			String fileName = "Mission 4/Exp21/Maredsous_Exp21_20101110164307.log";
			new TestGPSBasedHeading(fileName);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
