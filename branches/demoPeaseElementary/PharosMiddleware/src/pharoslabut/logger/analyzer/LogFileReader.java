package pharoslabut.logger.analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.StringTokenizer;

import pharoslabut.navigate.Location;
import playerclient.structures.gps.PlayerGpsData;

/**
 * Contains a single static method, readLogFile, that takes as input
 * the name of an experiment log file, analyzes the contents of this
 * file, and returns an ExpData object that contains the results of
 * the experiment.
 * 
 * @author Chien-Liang Fok
 */
public class LogFileReader {
	
	/**
	 * Reads an experiment log file.
	 * 
	 * @param logFileName The name of the file containing the experiment data.
	 * @return An ExpData object that contains the results of the experiment.
	 * @throws Exception If there is any error while reading the experiment log file.
	 */
	public static ExpData readLogFile(String logFileName) throws Exception {
		File file = new File(logFileName);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = null;
		PathEdge currEdge = null;
		PlayerGpsData currLoc = null;
		ExpData result = new ExpData(logFileName);
		
		while (( line = br.readLine()) != null){
			if (line.contains("Starting experiment at time:")) {
				String[] tokens = line.split("[: ]");
				result.setExpStartTime(Long.valueOf(tokens[8]));
			}
			else if (line.contains("WayPointFollower: Going to")) {
				StringTokenizer tokens = new StringTokenizer(line, ":()[], m/s");
				long timeStamp = Long.valueOf(tokens.nextToken());
				tokens.nextToken(); // WayPointFollower
				tokens.nextToken(); // going
				tokens.nextToken(); // to
				double lat = Double.valueOf(tokens.nextToken());
				double lon = Double.valueOf(tokens.nextToken());
				tokens.nextToken(); // 0.0
				tokens.nextToken(); // at
				double speed = Double.valueOf(tokens.nextToken()); 
				
				currEdge = new PathEdge(new Location(lat, lon), timeStamp, speed);
				
				// Set the start location of the path edge if we know where we are.
				if (currLoc != null) {
					currEdge.setStartLoc(new Location(currLoc));
				}
				
//				int i = 0;
//				while (tokens.hasMoreElements()) {
//					System.out.println(i++ + ": " + tokens.nextToken());
//				}
				
			}
			else if (line.contains("GPSDataBuffer")) {
				
				String[] tokens = line.split("[:=(), ]");
				long timeStamp = Long.valueOf(tokens[0].substring(1, tokens[0].length()-1));
				currLoc = new PlayerGpsData();
				currLoc.setAltitude(Integer.valueOf(tokens[18]));
				currLoc.setErr_horz(Double.valueOf(tokens[32]));
				currLoc.setErr_vert(Double.valueOf(tokens[34]));
				currLoc.setHdop(Integer.valueOf(tokens[28]));
				currLoc.setLatitude(Integer.valueOf(tokens[14]));
				currLoc.setLongitude(Integer.valueOf(tokens[16]));
				currLoc.setNum_sats(Integer.valueOf(tokens[26]));
				currLoc.setQuality(Integer.valueOf(tokens[24]));
				currLoc.setTime_sec(Integer.valueOf(tokens[10]));
				currLoc.setTime_usec(Integer.valueOf(tokens[12]));
				currLoc.setUtm_e(Double.valueOf(tokens[20]));
				currLoc.setUtm_n(Double.valueOf(tokens[22]));
				currLoc.setVdop(Integer.valueOf(tokens[30]));
				
				if (currEdge != null) {
					currEdge.addLocation(new GPSLocationState(timeStamp, currLoc));
					if (!currEdge.hasStartLoc())
						currEdge.setStartLoc(new Location(currLoc));
				}
//				for (int i=0; i < tokens.length; i++) {
//					System.out.println(i + ": " + tokens[i]);
//				}
			}
			else if (line.contains("Arrived at destination")) {
				String[] tokens = line.split("[:= ]");
				long timeStamp = Long.valueOf(tokens[0].substring(1, tokens[0].length()-1));
				currEdge.setEndTime(timeStamp);
				result.addPathEdge(currEdge);
				currEdge = null;
			}
		}
		return result;
	}
}
