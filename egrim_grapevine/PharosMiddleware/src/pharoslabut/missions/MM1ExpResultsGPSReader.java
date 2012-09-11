package pharoslabut.missions;

import java.util.*;
import java.io.*;
import pharoslabut.navigate.*;
import pharoslabut.logger.*;

/**
 * Reads in the GPS locations recorded by a robot during an experimental round.
 * 
 * @author Chien-Liang Fok
 *
 */
public class MM1ExpResultsGPSReader {

	

	/**
	 * The constructor.
	 * 
	 * @param fileName The name of the file containing the log from a robot
	 * following a GPS-based motion script. 
	 * @throws IOException 
	 */
	public static Vector<Location> readExpLog(String fileName) throws IOException {
		Vector<Location> locs = new Vector<Location>();
		File file = new File(fileName);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = null;
		while (( line = br.readLine()) != null){
			if (line.contains("Current Location")) {
				String[] tokens = line.split("[(,)]");
				locs.add(new Location(Double.valueOf(tokens[1]), Double.valueOf(tokens[2])));
			}
		}
		return locs;
	}
	
	public static final void main(String[] args) {
		try {
			String exp12logfile = "MM1/Navigate_2010118155838.log";
			String exp13logfile = "MM1/Navigate_20101181654.log";
			String exp14logfile = "MM1/Navigate_20101181691.log";
			String exp15logfile = "MM1/Navigate_201011815039.log";
			
			Vector<Location> exp12locs = readExpLog(exp12logfile);
			Vector<Location> exp13locs = readExpLog(exp13logfile);
			Vector<Location> exp14locs = readExpLog(exp14logfile);
			Vector<Location> exp15locs = readExpLog(exp15logfile);
			
			FileLogger flogger = new FileLogger("MM1/long-runs.csv");
			
			flogger.log("type,latitude,longitude,name,color");
			for (int i=0; i < exp12locs.size(); i++) {
				Location currLoc = exp12locs.get(i);
				String line = "T," + currLoc.latitude() + "," + currLoc.longitude();
				if (i == 0) {
					line += ",Run1,red";
				}
				flogger.log(line);
			}
			
			flogger.log("type,latitude,longitude,name,color");
			for (int i=0; i < exp13locs.size(); i++) {
				Location currLoc = exp13locs.get(i);
				String line = "T," + currLoc.latitude() + "," + currLoc.longitude();
				if (i == 0) {
					line += ",Run2,blue";
				}
				flogger.log(line);
			}
			
			flogger.log("type,latitude,longitude,name,color");
			for (int i=0; i < exp14locs.size(); i++) {
				Location currLoc = exp14locs.get(i);
				String line = "T," + currLoc.latitude() + "," + currLoc.longitude();
				if (i == 0) {
					line += ",Run3,purple";
				}
				flogger.log(line);
			}
			
			flogger.log("type,latitude,longitude,name,color");
			for (int i=0; i < exp15locs.size(); i++) {
				Location currLoc = exp15locs.get(i);
				String line = "T," + currLoc.latitude() + "," + currLoc.longitude();
				if (i == 0) {
					line += ",Run4,purple";
				}
				flogger.log(line);
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	} 
}

