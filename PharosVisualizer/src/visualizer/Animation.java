/*
 * Animation.java
 * Performs animation of robots
 * Lok Wong
 * Pharos Lab
 * Created: June 2, 2012 3:34 PM
 * Last Modified: July 16, 2012 12:48 11:21 PM
 */

package visualizer;

import java.awt.*;
import java.applet.*;
import java.io.*;
import java.util.*;

import javax.swing.ImageIcon;

public class Animation extends Applet implements Runnable {
	
	private static Vector<Position> PosArray = new Vector<Position>();
	private static RobotPath path = new RobotPath();
	private static int i;
	/* 
	 * (minLong,maxLat) = (0,0)
	 * (maxLong,minLat) = (700,700) (default)
	 */
	private final static double minLat = 30.52702, maxLat = 30.52783, minLong = -97.63307, maxLong = -97.63214;
//	private static double defaultScale, Xmin, Xmax, Ymin, Ymax; // ***Code to be used for variable background***
	private volatile Thread thread;
	private boolean isPlaying, isRewinding;
	private int speedMultiple;
	private int Xpos, Ypos, prevXpos, prevYpos;
	public long endTime;
	public boolean isEnd = false;
	
	public Animation(){
		this.init();
	}
	
	public void init(){
		String s;
		long startTime, prevTime;
		
		this.setBackground(Color.white);
		
		try {
			// Read log file
			FileReader fr = new FileReader("example-logs/M44_Exp2-GUINNESS-MRPatrol2_20120410100226.log");
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
					PosArray.addElement(pos);
					i++;
					s = br.readLine();
				}
			}
			br.close();
			fr.close();
			
			// If there is no log file to read, create an array of one position with properties at zero
			if(PosArray.size() == 0){
				Position pos = new Position();
				PosArray.addElement(pos);
			}
			// endTime = total time from start of experiment to last position change
			endTime = PosArray.get(PosArray.size() - 1).time;
			
			int j = 0;
			path.add(0, 0, getXpos(PosArray.get(0).begLong), getYpos(PosArray.get(0).begLat));	// First position
			for(i = 1, j = 1; i < PosArray.size(); i++, j++){
				int k = i - 1;
				Position pos1 = PosArray.get(k);
				Position pos2 = PosArray.get(i);
				while(pos1.begLat == pos2.begLat && pos1.begLong == pos2.begLong){
					i++;
					if(i < PosArray.size()){ pos2 = PosArray.get(i); }
					else{ break; }
				}
				path.add(j, i, getXpos(pos2.begLong), getYpos(pos2.begLat));
			}
			path.add(j, i, 0, 0);	// Dummy entry (end indicator)
			
/*			***Code to be used for variable background***
 
  			// Create mapping of lat/long to 700x700 pixel screen
			findXmin();
			findXmax();
			findYmin();
			findYmax();
			if((Xmax - Xmin) > (Ymax - Ymin)){
				defaultScale = 500 / (Xmax - Xmin);
			} else{
				defaultScale = 500 / (Ymax - Ymin);
			}*/
			
			i = 0;
			
			resize(700,700);
		}
		catch (Exception e) { e.printStackTrace(); }
	}
	
	public synchronized void start(){
		if(thread == null){
			thread = new Thread(this);
			thread.start();
		} else{
			notify();
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
		repaint();
	}
	
	public synchronized void rewind(){
		isRewinding = true;
		isPlaying = true;
		speedMultiple = 1;
	}
	
	public synchronized void fforward(){
		if(isPlaying == false){ start(); }
		isPlaying = true;
		isRewinding = false;
		speedMultiple = 2;
	}
	
	public void run(){
		while(true){
			repaint();
			try {
				if(i >= PosArray.size() || i < 0){
					Interface.stop.doClick();
				}
				Position pos = PosArray.get(i);
				Thread.sleep(pos.delay / speedMultiple);
			}
			catch(InterruptedException e) {}			

			while(!isPlaying){
				try{ wait(); }
				catch (Exception e){}
			}
		}
	}
	
	public void paint(Graphics g){
		Position pos;
		
		if(i >= 0 && i < PosArray.size()){ pos = PosArray.get(i); }
		else{ pos = PosArray.get(0); }
		if(i == 0){
			prevXpos = getXpos(pos.begLong);
			prevYpos = getYpos(pos.begLat);
		} else{
			prevXpos = Xpos;
			prevYpos = Ypos;			
		}
		Xpos = getXpos(pos.begLong);
		Ypos = getYpos(pos.begLat);
		if(isPlaying){	
			if(!isRewinding){ i++; }
			else{ i--; }
		}

		/*
		 * North = 0
		 * West = pi/2
		 * South = pi
		 * East = -pi/2
		 * 
		 * Pivot point at (Xpos,Ypos)
		 * Arrow 48px x 24px 
		 */
		int[] xPts = {Xpos - (int) (4 * Math.cos(pos.heading)),
				Xpos + (int) (4 * Math.cos(pos.heading)),
				Xpos - (int) (Math.sqrt(4*4 + 32*32) * Math.sin(pos.heading - Math.atan2(4, 32))),
				Xpos - (int) (Math.sqrt(12*12 + 32*32) * Math.sin(pos.heading - Math.atan2(12, 32))),
				Xpos - (int) (48 * Math.sin(pos.heading)),
				Xpos - (int) (Math.sqrt(12*12 + 32*32) * Math.sin(pos.heading + Math.atan2(12, 32))),
				Xpos - (int) (Math.sqrt(4*4 + 32*32) * Math.sin(pos.heading + Math.atan2(4, 32)))};
		int[] yPts = {Ypos + (int) (4 * Math.sin(pos.heading)),
				Ypos - (int) (4 * Math.sin(pos.heading)),
				Ypos - (int) (Math.sqrt(4*4 + 32*32) * Math.cos(pos.heading - Math.atan2(4, 32))),
				Ypos - (int) (Math.sqrt(12*12 + 32*32) * Math.cos(pos.heading - Math.atan2(12, 32))),
				Ypos - (int) (48 * Math.cos(pos.heading)),
				Ypos - (int) (Math.sqrt(12*12 + 32*32) * Math.cos(pos.heading + Math.atan2(12, 32))),
				Ypos - (int) (Math.sqrt(4*4 + 32*32) * Math.cos(pos.heading + Math.atan2(4, 32)))};
		int n = 7;
		
		g.drawImage(new ImageIcon("src/visualizer/img/background.png").getImage(), 0, 0, null);
		Graphics2D g2 = (Graphics2D) g;
		g.setColor(Color.blue);
		g2.setStroke(new BasicStroke(3));
		int j;
		for(j = 0; i >= path.pos[j]; j++){}
		g.drawPolyline(path.x, path.y, j);
		g.fillPolygon(xPts, yPts, n);
		g.setColor(Color.black);
		g2.setStroke(new BasicStroke(2));
		g.drawPolygon(xPts, yPts, n);
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

/*	***Code to be used for variable background***
 
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
	}*/
	
	private static int getXpos(double GPSXpos){
		return (int) ((GPSXpos - minLong) * 700 / (maxLong - minLong));
	}
	
	private static int getYpos(double GPSYpos){
		return (int) ((maxLat - GPSYpos) * 700 / (maxLat - minLat));
	}
}
