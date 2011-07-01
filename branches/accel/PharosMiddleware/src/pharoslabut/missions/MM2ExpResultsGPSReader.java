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
public class MM2ExpResultsGPSReader {

	

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
//			String exp12logfile = "Mini-Mission 2/MM2-Exp12_20101115153909.log";
//			String exp13logfile = "Mini-Mission 2/MM2-Exp13_20101115154455.log";
//			String exp14logfile = "Mini-Mission 2/MM2-Exp14_20101115155000.log";
//			String exp15logfile = "Mini-Mission 2/MM2-Exp15_20101115155512.log";
//			Vector<Location> exp12locs = readExpLog(exp12logfile);
//			Vector<Location> exp13locs = readExpLog(exp13logfile);
//			Vector<Location> exp14locs = readExpLog(exp14logfile);
//			Vector<Location> exp15locs = readExpLog(exp15logfile);
//			
//			FileLogger flogger = new FileLogger("Mini-Mission 2/long-test.csv");
//			flogger.log("type,latitude,longitude,name,color");
//			for (int i=0; i < exp12locs.size(); i++) {
//				Location currLoc = exp12locs.get(i);
//				String line = "T," + currLoc.latitude() + "," + currLoc.longitude();
//				if (i == 0) {
//					line += ",Exp12,red";
//				}
//				flogger.log(line);
//			}
//			
//			flogger.log("type,latitude,longitude,name,color");
//			for (int i=0; i < exp13locs.size(); i++) {
//				Location currLoc = exp13locs.get(i);
//				String line = "T," + currLoc.latitude() + "," + currLoc.longitude();
//				if (i == 0) {
//					line += ",Exp13,blue";
//				}
//				flogger.log(line);
//			}
//			
//			flogger.log("type,latitude,longitude,name,color");
//			for (int i=0; i < exp14locs.size(); i++) {
//				Location currLoc = exp14locs.get(i);
//				String line = "T," + currLoc.latitude() + "," + currLoc.longitude();
//				if (i == 0) {
//					line += ",Exp14,purple";
//				}
//				flogger.log(line);
//			}
//			
//			flogger.log("type,latitude,longitude,name,color");
//			for (int i=0; i < exp15locs.size(); i++) {
//				Location currLoc = exp15locs.get(i);
//				String line = "T," + currLoc.latitude() + "," + currLoc.longitude();
//				if (i == 0) {
//					line += ",Exp15,orange";
//				}
//				flogger.log(line);
//			}
			String exp1logfile = "Mini-Mission 2/MM2-Exp1_20101115143157.log";
			String exp2logfile = "Mini-Mission 2/MM2-Exp2_20101115143745.log";
			String exp3logfile = "Mini-Mission 2/MM2-Exp3_20101115144621.log";
			String exp8logfile = "Mini-Mission 2/MM2-Exp8_20101115151745.log";
			String exp9logfile = "Mini-Mission 2/MM2-Exp9_20101115152219.log";
			//String exp10logfile = "Mini-Mission 2/MM2-Exp10_20101115153050.log";
			
			Vector<Location> exp1locs = readExpLog(exp1logfile);
			Vector<Location> exp2locs = readExpLog(exp2logfile);
			Vector<Location> exp3locs = readExpLog(exp3logfile);
			Vector<Location> exp8locs = readExpLog(exp8logfile);
			Vector<Location> exp9locs = readExpLog(exp9logfile);
			//Vector<Location> exp10locs = readExpLog(exp10logfile);
			
			FileLogger flogger = new FileLogger("Mini-Mission 2/short-runs.csv");
			
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
			for (int i=0; i < exp3locs.size(); i++) {
				Location currLoc = exp3locs.get(i);
				String line = "T," + currLoc.latitude() + "," + currLoc.longitude();
				if (i == 0) {
					line += ",Exp3,purple";
				}
				flogger.log(line);
			}
			
			flogger.log("type,latitude,longitude,name,color");
			for (int i=0; i < exp8locs.size(); i++) {
				Location currLoc = exp8locs.get(i);
				String line = "T," + currLoc.latitude() + "," + currLoc.longitude();
				if (i == 0) {
					line += ",Exp8,purple";
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
			
			/* // In this test, the robot ran into a curbside
			flogger.log("type,latitude,longitude,name,color");
			 
			for (int i=0; i < exp10locs.size(); i++) {
				Location currLoc = exp10locs.get(i);
				String line = "T," + currLoc.latitude() + "," + currLoc.longitude();
				if (i == 0) {
					line += ",Exp10,turqoise";
				}
				flogger.log(line);
			}*/
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	} 
}

