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
public class MM3ExpResultsGPSReader {

	

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
			 String exp1logfile = "MM3/MM3-Exp1_20101117002222.log";
			 String exp2logfile = "MM3/MM3-Exp2_20101117003049.log";
			 String exp4logfile = "MM3/MM3-Exp4_20101117003704.log";
			 String exp5logfile = "MM3/MM3-Exp5_20101117004219.log";
			 
			 String exp6logfile = "MM3/MM3-Exp6_20101117004737.log";
			 
			 String exp7logfile = "MM3/MM3-Exp7_20101117010106.log";
			 String exp8logfile = "MM3/MM3-Exp8_20101117010633.log";
			 String exp9logfile = "MM3/MM3-Exp9_20101117011150.log";
			 String exp10logfile = "MM3/MM3-Exp10_20101117011759.log";
			 String exp11logfile = "MM3/MM3-Exp11_20101117012302.log";
			
			Vector<Location> exp1locs = readExpLog(exp1logfile);
			Vector<Location> exp2locs = readExpLog(exp2logfile);
			Vector<Location> exp4locs = readExpLog(exp4logfile);
			Vector<Location> exp5locs = readExpLog(exp5logfile);
			
			Vector<Location> exp6locs = readExpLog(exp6logfile);
			
			Vector<Location> exp7locs = readExpLog(exp7logfile);
			Vector<Location> exp8locs = readExpLog(exp8logfile);
			Vector<Location> exp9locs = readExpLog(exp9logfile);
			Vector<Location> exp10locs = readExpLog(exp10logfile);
			Vector<Location> exp11locs = readExpLog(exp11logfile);
			
			FileLogger flogger = new FileLogger("MM3/Exp1-5.csv", false);
			
			flogger.log("type,latitude,longitude,name,color");
			for (int i=0; i < exp1locs.size(); i++) {
				Location currLoc = exp1locs.get(i);
				String line = "T," + currLoc.latitude() + "," + currLoc.longitude();
				if (i == 0) {
					line += ",Exp1,red";
				}
				flogger.log(line);
			}
			
			flogger.log("type,latitude,longitude,name,color");
			for (int i=0; i < exp2locs.size(); i++) {
				Location currLoc = exp2locs.get(i);
				String line = "T," + currLoc.latitude() + "," + currLoc.longitude();
				if (i == 0) {
					line += ",Exp2,blue";
				}
				flogger.log(line);
			}
			
			flogger.log("type,latitude,longitude,name,color");
			for (int i=0; i < exp4locs.size(); i++) {
				Location currLoc = exp4locs.get(i);
				String line = "T," + currLoc.latitude() + "," + currLoc.longitude();
				if (i == 0) {
					line += ",Exp4,purple";
				}
				flogger.log(line);
			}
			
			flogger.log("type,latitude,longitude,name,color");
			for (int i=0; i < exp5locs.size(); i++) {
				Location currLoc = exp5locs.get(i);
				String line = "T," + currLoc.latitude() + "," + currLoc.longitude();
				if (i == 0) {
					line += ",Exp5,orange";
				}
				flogger.log(line);
			}
			
			flogger = new FileLogger("MM3/Exp6.csv", false);
			flogger.log("type,latitude,longitude,name,color");
			for (int i=0; i < exp6locs.size(); i++) {
				Location currLoc = exp6locs.get(i);
				String line = "T," + currLoc.latitude() + "," + currLoc.longitude();
				if (i == 0) {
					line += ",Exp6,red";
				}
				flogger.log(line);
			}
			
			flogger = new FileLogger("MM3/Exp7-11.csv", false);
			flogger.log("type,latitude,longitude,name,color");
			for (int i=0; i < exp7locs.size(); i++) {
				Location currLoc = exp7locs.get(i);
				String line = "T," + currLoc.latitude() + "," + currLoc.longitude();
				if (i == 0) {
					line += ",Exp7,red";
				}
				flogger.log(line);
			}
			
			flogger.log("type,latitude,longitude,name,color");
			for (int i=0; i < exp8locs.size(); i++) {
				Location currLoc = exp8locs.get(i);
				String line = "T," + currLoc.latitude() + "," + currLoc.longitude();
				if (i == 0) {
					line += ",Exp8,blue";
				}
				flogger.log(line);
			}
			
			flogger.log("type,latitude,longitude,name,color");
			for (int i=0; i < exp9locs.size(); i++) {
				Location currLoc = exp9locs.get(i);
				String line = "T," + currLoc.latitude() + "," + currLoc.longitude();
				if (i == 0) {
					line += ",Exp9,green";
				}
				flogger.log(line);
			}
			
			flogger.log("type,latitude,longitude,name,color"); 
			for (int i=0; i < exp10locs.size(); i++) {
				Location currLoc = exp10locs.get(i);
				String line = "T," + currLoc.latitude() + "," + currLoc.longitude();
				if (i == 0) {
					line += ",Exp10,turqoise";
				}
				flogger.log(line);
			}
			
			flogger.log("type,latitude,longitude,name,color"); 
			for (int i=0; i < exp11locs.size(); i++) {
				Location currLoc = exp11locs.get(i);
				String line = "T," + currLoc.latitude() + "," + currLoc.longitude();
				if (i == 0) {
					line += ",Exp11,maroon";
				}
				flogger.log(line);
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	} 
}

