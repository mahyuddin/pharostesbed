/*
 * Animation.java
 * Performs animation of robots
 * Lok Wong
 * Pharos Lab
 * Created: June 2, 2012 3:34 PM
 * Last Modified: June 16, 2012 10:43 PM
 */

package visualizer;

import java.awt.*;
import java.applet.*;
import java.io.*;
import java.util.*;

public class Animation extends java.applet.Applet implements Runnable {
	
	private static Vector<Position> PosArray = new Vector<Position>();
	private static int i;
	private static double defaultScale, Xmin, Xmax, Ymin, Ymax;
	private volatile Thread thread;
	private boolean isPlaying, isRewinding;
	private int speedMultiple;
	private int Xpos, Ypos, prevXpos, prevYpos;
	public boolean isEnd = false;
	
	public Animation(){
		this.init();
	}
	
	public void init(){
		String s;
		long startTime, prevTime;
		
		try {
			// Read log file
			FileReader fr = new FileReader("C:\\Users\\Lok Wong\\Desktop\\workspace\\Visualizer\\example-logs\\M44_Exp1-GUINNESS-MRPatrol2_20120410092604.log");
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
					pos.begLat = getLat(s);
					pos.begLong = getLong(s);
					while(!s.contains("Target Location")){ s = br.readLine(); }
					pos.endLat = getLat(s);
					pos.endLong = getLong(s);
					while(!s.contains("Current Heading")){ s = br.readLine(); }
					pos.heading = getHeading(s);
					PosArray.addElement(new Position(pos.time, pos.delay, pos.begLat, pos.begLong, pos.endLat, pos.endLong, pos.heading));
					i++;
					s = br.readLine();
				}
			}
			br.close();
			fr.close();
			
			i = 0;			
			
			// Create mapping of lat/long to 700x700 pixel screen
			findXmin();
			findXmax();
			findYmin();
			findYmax();
			if((Xmax - Xmin) > (Ymax - Ymin)){
				defaultScale = 500 / (Xmax - Xmin);
			} else{
				defaultScale = 500 / (Ymax - Ymin);
			}
						
			resize(700,700);
		}
		catch (FileNotFoundException e) {	e.printStackTrace(); }
		catch (IOException e) {	e.printStackTrace(); }
	}
	
	public synchronized void start(){
		if(thread == null){
			thread = new Thread(this);
			thread.start();
		}
		isPlaying = true;
		isRewinding = false;
		speedMultiple = 1;
		isEnd = false;
	}
	
	public synchronized void pause(){
		isPlaying = false;
	}
	
	public synchronized void stop(){
		isPlaying = false;
		isRewinding = false;
		speedMultiple = 1;
		i = 0;
	}
	
	public synchronized void rewind(){
		isRewinding = true;
		isPlaying = true;
		speedMultiple = 1;
	}
	
	public synchronized void fforward(){
		if(isPlaying == false){ start(); }
		isRewinding = false;
		speedMultiple = 2;
		isPlaying = true;
	}
	
	public void run(){
		while(true){
			repaint();
			try {
				if(i >= PosArray.size() || i < 0){
					isEnd = true;
					i = 0;
					Interface.stop.doClick();
				}
				Position pos = PosArray.get(i);
				Thread.sleep(pos.delay / speedMultiple);
			}
			catch(InterruptedException e) { System.out.print("FAIL"); }
			
			while(!isPlaying){
				try{ wait(); }
				catch (Exception e){ System.out.print("FAIL"); }
			}
		}
	}
	
	public void paint(Graphics g){
		Position pos = PosArray.get(i);
		if(i == 0){
			prevXpos = getXpos(pos.begLong);
			prevYpos = getYpos(pos.begLat);
		} else{
			prevXpos = Xpos;
			prevYpos = Ypos;			
		}
		Xpos = getXpos(pos.begLong);
		Ypos = getYpos(pos.begLat);
		if(!isRewinding){ i++; }
		else{ i--; }

		int[] xPts = {Xpos - 4, Xpos + 4, Xpos + 4, Xpos + 12, Xpos, Xpos - 12, Xpos - 4};
		int[] yPts = {Ypos, Ypos, Ypos - 32, Ypos - 32, Ypos - 48, Ypos - 32, Ypos - 32};
		int n = 7;
		
		g.setColor(Color.red);
		g.fillPolygon(xPts, yPts, n);
		System.out.print("" + i + ": " + pos.time + "\n");
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
	
	private static double getLat(String s){
		String[] str;
		str = new String[2];
		str = s.split("\\(");
		str = str[1].split("\\,");		
		return Double.valueOf(str[0]);
	}

	private static double getLong(String s){
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

	private static void findXmin(){
		Position pos = PosArray.get(0);
		Xmin = pos.begLong;
		for(; i < PosArray.size(); i++){
			pos = PosArray.get(i);
			if(pos.begLong < Xmin){ Xmin = pos.begLong; }
		}
		i = 0;
	}

	private static void findXmax(){
		Position pos = PosArray.get(0);
		Xmax = pos.begLong;
		for(; i < PosArray.size(); i++){
			pos = PosArray.get(i);
			if(pos.begLong > Xmax){ Xmax = pos.begLong; }
		}
		i = 0;
	}

	private static void findYmin(){
		Position pos = PosArray.get(0);
		Ymin = pos.begLat;
		for(; i < PosArray.size(); i++){
			pos = PosArray.get(i);
			if(pos.begLat > Ymin){ Ymin = pos.begLat; }	// Signs flip between latitude and Y-coordinate
		}
		i = 0;
	}

	private static void findYmax(){
		Position pos = PosArray.get(0);
		Ymax = pos.begLat;
		for(; i < PosArray.size(); i++){
			pos = PosArray.get(i);
			if(pos.begLat < Ymax){ Ymax = pos.begLat; }	// Signs flip between latitude and Y-coordinate
		}
		i = 0;
	}
	
	private static int getXpos(double GPSXpos){
		double mid = (Xmin + Xmax) / 2;
		return (int) (350 - (defaultScale * (mid - GPSXpos)));
	}

	private static int getYpos(double GPSYpos){
		double mid = (Ymin + Ymax) / 2;
		return (int) (350 + (defaultScale * (mid - GPSYpos)));	// Signs flip between latitude and Y-coordinate
	}

}
