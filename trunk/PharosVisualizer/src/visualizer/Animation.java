/*
 * Animation.java
 * Performs animation of robots
 * Lok Wong
 * Pharos Lab
 * Created: June 2, 2012 3:34 PM
 * Last Modified: July 29, 2012 7:05 AM
 */

package visualizer;

import java.awt.*;
import java.applet.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

public class Animation extends Applet implements Runnable {
	
	private Vector<Robot> Robots = new Vector<Robot>();
	private Vector<Position> PosArray;
	
	public Vector<Color> ColorArray = new Vector<Color>();
	private int numLogs = 0;
	private int longestRobot;	// longest in terms of time
	
	private int i;
	/* 
	 * (minLong,maxLat) = (0,0)
	 * (maxLong,minLat) = (700,700) (default)
	 */
	private final double minLat = 30.52702, maxLat = 30.52783, minLong = -97.63307, maxLong = -97.63214;
//	private static double defaultScale, Xmin, Xmax, Ymin, Ymax; // ***Code to be used for variable background***

	private volatile Thread thread;
	private final int frameInterval = 10;	// ms of time between each frame
	private final int frameRate = 25;		// every nth frame per update during normal playback
	private int speedMultiple;				// 1 for play, 0 for pause/stop, <0 for rewind, >1 for fast forward
	private boolean isPlaying, isRewinding;
	private int Xpos, Ypos;
	public long endTime = 0;				// in ms
	public boolean isEnd = false;
	
	public Animation(String logdir){
		ColorArray.add(Color.blue);
		ColorArray.add(Color.red);
		ColorArray.add(new Color(0, 127, 0));
		ColorArray.add(new Color(127, 0, 127));
		this.init(logdir);
	}
	
	public void init(String logdir){
		String s;
		long startTime;
		
		this.setBackground(Color.white);

		try {
			// Determines directory to search for log files
			if(logdir == "."){ logdir = ""; }
			else if(!logdir.endsWith("/")){ logdir = logdir.concat("/"); }
			File[] files = new File(logdir).listFiles();
			
			// Creates Robots database
			for(File file : files){
				if(file.getName().toLowerCase().endsWith(".log")){
					FileReader fr = new FileReader(logdir + file.getName());
					BufferedReader br = new BufferedReader(fr);
					
					// Create database of unique positions
					PosArray = new Vector<Position>();
					s = br.readLine();
					startTime = getTime(s);	// Unix time (ms)
					
					s = br.readLine();
					String check = "Current State as of time";
					while(s != null){
						if(!s.contains(check)){
							s = br.readLine();
						} else{
							Position pos = new Position();
							pos.time = getTime(s) - startTime;
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
					// endTime = total time from start of experiment to last position change
					if(endTime < PosArray.get(PosArray.size() - 1).time){
						endTime = PosArray.get(PosArray.size() - 1).time;
						longestRobot = numLogs;
					}

					// Convert database of unique positions into database of equal-timed frames
					Robot bot = new Robot(numLogs, getName(file.getName()), numLogs < ColorArray.size() ? ColorArray.get(numLogs) : Color.lightGray);
					i = 1;
					int j = 0;
					long t = 0;
					for(i = 1; i < PosArray.size(); j++){
						Position pos0 = PosArray.get(i - 1), pos1 = PosArray.get(i);
						double frac = (t - pos0.time) / (pos1.time - pos0.time);
						if(t <= pos1.time){
							Position pos = new Position(t, pos0.begLat + ((pos1.begLat - pos0.begLat) * frac),
									pos0.begLong + ((pos1.begLong - pos0.begLong) * frac), pos0.endLat, pos0.endLong,
									pos0.heading + ((pos1.heading - pos0.heading) * frac));
							t += frameInterval;
							bot.frames.add(pos);
						} else{ i++; }
					}
					
					// Create database of path segments
					bot.path.add(0, 0, getXpos(bot.frames.get(0).begLong), getYpos(bot.frames.get(0).begLat));	// First position
					for(i = 1, j = 1; i < bot.frames.size(); i++, j++){
						int k = i - 1;
						Position pos1 = bot.frames.get(k), pos2 = bot.frames.get(i);
						while(pos1.begLat == pos2.begLat && pos1.begLong == pos2.begLong){
							i++;
							if(i < bot.frames.size()){ pos2 = bot.frames.get(i); }
							else{ break; }
						}
						bot.path.add(j, i, getXpos(pos2.begLong), getYpos(pos2.begLat));
					}
					Robots.add(bot);
															
					br.close();
					fr.close();

					numLogs++;
				}
			}
			Position pos0 = PosArray.get(0);
			PosArray.add(0, new Position(0, pos0.begLat, pos0.begLong, pos0.endLat, pos0.endLong, pos0.heading));
			
			// If there is no log file to read, create an array of one position with properties at zero
			if(PosArray.size() == 0){
				Position pos = new Position();
				PosArray.addElement(pos);
			}
			
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
		for(Robot bot : Robots){ bot.isFinished = false; }
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
				if(i < 0){ Interface.stop.doClick(); }
				else{
					for(Robot bot : Robots){				
						if(i >= bot.frames.size()){
							bot.isFinished = true;
							if(bot.n == longestRobot){ Interface.stop.doClick(); }
						}
					}
				}
				Thread.sleep(frameInterval * frameRate);
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
		Graphics2D g2 = (Graphics2D) g;

		g.drawImage(new ImageIcon("src/visualizer/img/background.png").getImage(), 0, 0, null);
		
		// Create robot legend
		g.setColor(Color.white);
		g.setFont(new Font("SansSerif", Font.BOLD, 12));
		g.fillRect(536, 31, 157, 8 + (int) (Robots.size() * ((4/3) * g.getFont().getSize() + 4)));
		for(Robot bot : Robots){
			g.setColor(bot.color);
			g.drawString(bot.name + "\n", 536 + 5, 31 + (int) (((4/3) * g.getFont().getSize() + 4) * (bot.n + 1)));
		}
		
		// Create paths traveled
		for(int j = 0; j < Robots.size(); j++){
			Robot bot = Robots.get(j);

			g.setColor(Robots.get(j).color);
			g2.setStroke(new BasicStroke(3));
			int k;
			if(bot.isFinished){
				for(k = 0; bot.frames.size() > bot.path.pos[k]; k++){}
				g.drawPolyline(bot.path.x, bot.path.y, k);
			} else{
				for(k = 0; i < bot.frames.size() && i >= bot.path.pos[k]; k++){}
				g.drawPolyline(bot.path.x, bot.path.y, k);
			}
		}
		
		// Create position/heading markers
		for(int j = 0; j < Robots.size(); j++){
			Robot bot = Robots.get(j);

			if(i >= 0 && i < bot.frames.size()){ pos = bot.frames.get(i); }
			else{ pos = bot.frames.get(0); }
			Xpos = getXpos(pos.begLong);
			Ypos = getYpos(pos.begLat);
	
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
			
			g.setColor(Robots.get(j).color);
			g2.setStroke(new BasicStroke(3));
			int k;
			if(bot.isFinished){
				g.fillRect(getXpos(bot.frames.get(bot.frames.size() - 1).begLong) - 8,
					getYpos(bot.frames.get(bot.frames.size() - 1).begLat) - 8, 16, 16);
				g.setColor(Color.black);
				g2.setStroke(new BasicStroke(2));
				g.drawRect(getXpos(bot.frames.get(bot.frames.size() - 1).begLong) - 8,
					getYpos(bot.frames.get(bot.frames.size() - 1).begLat) - 8, 16, 16);
			} else{
				g.fillPolygon(xPts, yPts, n);
				g.setColor(Color.black);
				g2.setStroke(new BasicStroke(2));
				g.drawPolygon(xPts, yPts, n);
			}
		}

		if(isPlaying){	
			if(!isRewinding){
				i += frameRate * speedMultiple;
			} else{
				i -= frameRate * speedMultiple;
			}
		}
	}
	
	public void update(Graphics g){
		paint(g);
	}
	
	/*
	 * getName, getTime, getLat, getLong, and getHeading assumes the following format of the log files:
	 * Log file name: [mission name]_[exp name]-[robot name]-MRPatrol2_[timestamp]
	 * Sample content of "Current State":
	 * [1334067966552] pharoslabut.navigate.Navigate: locateTarget: locateTarget(): Current State as of time 1334067966537:
	 *		Location Data:
	 *			Current Location: (30.527175, -97.6327883)
	 *			Target Location: (30.5272852, -97.6324283)
	 *			Distance: 36.59231812941649 meters
	 *		Heading Data:
	 *			Angle to target: -1.2737424809040778 radians
	 *			Current Heading: -0.5113814473152161 radians
	 *			Heading Error: -0.7623610335888618 radians (Must Turn Right!)
	 */
	private String getName(String s){
		String[] str = new String[2];
		str = s.split("-MRPatrol2");
		return str[0];
	}
	
	private long getTime(String s){	// Assumes log uses timestamps with format [Unix time]
		String[] str = new String[2];
		str = s.split("\\[");
		str = str[1].split("\\]");
		return Long.valueOf(str[0]);
	}
	
	private double getLat(String s){
		String[] str = new String[2];
		str = s.split("\\(");
		str = str[1].split("\\,");		
		return Double.valueOf(str[0]);
	}

	private double getLong(String s){
		String[] str = new String[2];
		str = s.split("\\, ");
		str = str[1].split("\\)");		
		return Double.valueOf(str[0]);
	}

	private double getHeading(String s){
		String[] str = new String[2];
		str = s.split("\\: ");
		str = str[1].split("\\ radians");		
		return Double.valueOf(str[0]);
	}

/*	***Code to be used for variable background***
 
  	private void findXmin(){
		Position pos = PosArray.get(0);
		Xmin = pos.begLong;
		for(; i < PosArray.size(); i++){
			pos = PosArray.get(i);
			if(pos.begLong < Xmin){ Xmin = pos.begLong; }
		}
		i = 0;
	}

	private void findXmax(){
		Position pos = PosArray.get(0);
		Xmax = pos.begLong;
		for(; i < PosArray.size(); i++){
			pos = PosArray.get(i);
			if(pos.begLong > Xmax){ Xmax = pos.begLong; }
		}
		i = 0;
	}

	private void findYmin(){
		Position pos = PosArray.get(0);
		Ymin = pos.begLat;
		for(; i < PosArray.size(); i++){
			pos = PosArray.get(i);
			if(pos.begLat > Ymin){ Ymin = pos.begLat; }	// Signs flip between latitude and Y-coordinate
		}
		i = 0;
	}

	private void findYmax(){
		Position pos = PosArray.get(0);
		Ymax = pos.begLat;
		for(; i < PosArray.size(); i++){
			pos = PosArray.get(i);
			if(pos.begLat < Ymax){ Ymax = pos.begLat; }	// Signs flip between latitude and Y-coordinate
		}
		i = 0;
	}
	
	private int getXpos(double GPSXpos){
		double mid = (Xmin + Xmax) / 2;
		return (int) (350 - (defaultScale * (mid - GPSXpos)));
	}

	private int getYpos(double GPSYpos){
		double mid = (Ymin + Ymax) / 2;
		return (int) (350 + (defaultScale * (mid - GPSYpos)));	// Signs flip between latitude and Y-coordinate
	}*/
	
	private int getXpos(double GPSXpos){
		return (int) ((GPSXpos - minLong) * 700 / (maxLong - minLong));
	}
	
	private int getYpos(double GPSYpos){
		return (int) ((maxLat - GPSYpos) * 700 / (maxLat - minLat));
	}
	
	// Used to allow user to jump to a point in the animation
	public synchronized void setTime(double frac){
		i = (int) (frac * Robots.get(longestRobot).frames.size());
		for(Robot bot : Robots){				
			if(i >= bot.frames.size()){	bot.isFinished = true; }
			else{ bot.isFinished = false; }
		}
		repaint();
	}
}
