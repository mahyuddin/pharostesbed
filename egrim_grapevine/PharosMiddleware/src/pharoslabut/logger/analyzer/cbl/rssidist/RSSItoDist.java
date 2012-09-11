package pharoslabut.logger.analyzer.cbl.rssidist;

import pharoslabut.logger.analyzer.Line;
import pharoslabut.navigate.Location;

/**
 * Defines the parent class of all RSSI to Distance converters.
 * 
 * @author Chien-Liang Fok
 */
public abstract class RSSItoDist {
	static final int DIST = 0;
	static final int RSSI = 1;
	
	private double[][] dataTable;
	
	public RSSItoDist(double[][] dataTable) {
		this.dataTable = dataTable;
	}
	
	/**
	 * Returns the distance corresponding to the specified rssi.
	 * 
	 * @param rssi The rssi measurement (dBm).
	 * @return The distance in meters, or -1 if no data available.
	 */
	public double getDist(double rssi) {
		
		// If the rssi is lower than the lowest in the data table, return -1
		// since there is no good correllation between rssi and distance at that point.
		if (rssi == -1 && rssi < dataTable[dataTable.length-1][RSSI])
			return -1;
		
		int lowerIndx = 0;
		int upperIndx = dataTable.length - 1;
		
		// find the lower and upper indices
		for (int i=0; i < dataTable.length-1; i++) {
			if (dataTable[i][RSSI] > rssi)
				lowerIndx = i;
		}
		for (int i=dataTable.length-1; i > 0; i--) {
			if (dataTable[i][RSSI] < rssi)
				upperIndx = i;
		}
		
		log("lower indx = " + lowerIndx + ", upper indx = " + upperIndx);
		
		// do a linear approximation to find distance at specified rssi
		Line line = new Line(new Location(dataTable[lowerIndx][DIST], dataTable[lowerIndx][RSSI]), 
				new Location(dataTable[upperIndx][DIST], dataTable[upperIndx][RSSI]));
		return line.getLatitude(rssi);
	}
	
	/**
	 * The default implemenation ignores the robotIDs.
	 * 
	 * @param robot1ID
	 * @param robot2ID
	 * @param rssi
	 * @return
	 */
	public double getDist(int robot1ID, int robot2ID, double rssi) {
		return getDist(rssi);
	}
	
	private void log(String msg) {
		//System.out.println(msg);
	}
}
