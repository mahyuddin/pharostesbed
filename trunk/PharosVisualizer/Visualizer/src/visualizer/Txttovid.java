/*
 * txttovid.java
 * Converts text from log into motion on interface screen
 * Lok Wong
 * Pharos Lab
 * Created: June 1, 2012 10:21 PM
 * Last Modified: June 9, 2012 8:10 PM
 */

package visualizer;

import java.io.*;
import java.util.*;

public class Txttovid {

	static ArrayList PosArray = new ArrayList<Position>();
	Thread thread;
	volatile boolean runThread = true;
	int Xpos = 100, Ypos = 50;
	
	public static void main(String[] args) throws IOException {
		
		String s;
		long startTime, prevTime;
		int i = 0;
		
		// Read log file
		FileReader fr = new FileReader("src\\visualizer\\M44_Exp1-GUINNESS-MRPatrol2_20120410092004_copy.log");
		BufferedReader br = new BufferedReader(fr);
		
		// Create database of positions
		s = br.readLine();
		startTime = getTime(s);	// Unix time (ms)
		prevTime = 0;
		
		s = br.readLine();
		String check = "Current State as of time";
		while(s != null){
			if(!s.contains(check)){
				s = br.readLine();
			} else{
				Position pos = new Position();
				pos.time = getTime(s) - startTime;
				pos.delay = pos.time - prevTime;
				prevTime = pos.time;
				while(!s.contains("Current Location")){ s = br.readLine(); }
				pos.begLat = getX(s);
				pos.begLong = getY(s);
				while(!s.contains("Target Location")){ s = br.readLine(); }
				pos.endLat = getX(s);
				pos.endLong = getY(s);
				while(!s.contains("Current Heading")){ s = br.readLine(); }
				pos.heading = getHeading(s);
				PosArray.add(i,pos);
				i++;
				System.out.print("" + i + ": " + pos.time + ", " + pos.delay + ", " + pos.begLat + ", " + pos.begLong + ", " + pos.endLat + ", " + pos.endLong + ", " + pos.heading + "\n");
				s = br.readLine();
			}
		}
				
		br.close();
		fr.close();
	}
	
	private static long convertTextToInt(String s){	
		long num = 0;
		int i = 0;
		int c = s.charAt(i);
		
		while(c >= '0' && c <= '9'){
			num = num*10 + (c - '0');
			i++;
			c = s.charAt(i);
		}
		return num;
	}
	
	private static long getTime(String s) throws IOException {	// Assumes log uses timestamps with format [Unix time]
		int i = 0;
		int c = s.charAt(i);
		while(c < '0' || c > '9'){
			i++;
			c = s.charAt(i);
		}
		return convertTextToInt(s.substring(i));
	}
	
	private static double getX(String s){
		String[] str;
		str = new String[2];
		str = s.split("\\(");
		str = str[1].split("\\,");		
		return Double.valueOf(str[0]);
	}

	private static double getY(String s){
		String[] str;
		str = new String[2];
		str = s.split("\\, ");
		str = str[1].split("\\)");		
		return Double.valueOf(str[0]);
	}

	private static double getHeading(String s){
		String[] str;
		str = new String[2];
		str = s.split("\\: ");
		str = str[1].split("\\ radians");		
		return Double.valueOf(str[0]);
	}

}
