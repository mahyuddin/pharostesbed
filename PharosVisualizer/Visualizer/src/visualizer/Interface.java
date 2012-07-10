/*
 * Interface.java
 * GUI interface for visualizer
 * Lok Wong
 * Pharos Lab
 * Created: June 11, 2012 9:55 AM
 * Last Modified: July 9, 2012 9:31 PM
 */

package visualizer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Interface extends JFrame {
	
	public static JProgressBar bar;
	
	public static JButton play, stop, rewind, fforward;
	private static boolean isPlaying = false;
	
	static JTextField timeElapsed;
	private static long time = 0;
	private static String totalTime = new String();
	private static long endTime;
	private static Timer timer;
	private static int updateCountRate = 137;	// in ms; not to be changed as program runs
	private static int countFactor = 1;			// 1 for play, >1 for fast forward, etc.
	private static boolean isTimerStart = false;
	
	private static int FrameWidth = 700, FrameHeight = 700, panelHeight = 55;
	static Animation animation = new Animation();
	
	public static void main(String args[]){
/*		for(int i = 0; i < args.length; i++){
			if(args[i].equals("-exp")){
				
			}
		}
*/		
		Interface gui = new Interface();
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gui.setSize(FrameWidth + 15, FrameHeight + panelHeight + 35);
		gui.setVisible(true);
		gui.setTitle("Pharos Lab Visualizer");
		
		animation.setLocation(0, 0);
		gui.add(animation);
		
		endTime = animation.endTime;
		// Set start at "00:00.000 / endTime" with endTime in terms of minutes, seconds, and milliseconds with leading zeros
		totalTime = ("" + (endTime/60000 < 10 ? 0 : "") + (endTime/60000) + ':' + (endTime%60000/1000 < 10 ? 0 : "")
				+ endTime%60000/1000 + '.' + (endTime%1000 < 100 ? 0 : "") + (endTime%1000 < 10 ? 0 : "") + endTime%1000);
		timeElapsed.setText("00:00.000 / " + totalTime);
	}
	
	public Interface(){
		setLayout(null);
		
		bar = new JProgressBar();
		bar.setBounds(0, FrameHeight, 699, 10);
		add(bar);
		
		play = new JButton(new ImageIcon(getClass().getResource("img/play.png")));
		play.setBounds(5, FrameHeight + bar.getHeight() + 5, 35, 35);
		add(play);
		Play playEvent = new Play();
		play.addActionListener(playEvent);

		stop = new JButton(new ImageIcon(getClass().getResource("img/stop.png")));
		stop.setBounds(play.getX() + play.getWidth() + 5, FrameHeight + bar.getHeight() + 5, 35, 35);
		add(stop);
		Stop stopEvent = new Stop();
		stop.addActionListener(stopEvent);

		rewind = new JButton(new ImageIcon(getClass().getResource("img/rewind.png")));
		rewind.setBounds(stop.getX() + stop.getWidth() + 10, FrameHeight + bar.getHeight() + 5, 35, 35);
		add(rewind);
		Rewind rewindEvent = new Rewind();
		rewind.addActionListener(rewindEvent);

		fforward = new JButton(new ImageIcon(getClass().getResource("img/fforward.png")));
		fforward.setBounds(rewind.getX() + rewind.getWidth() + 5, FrameHeight + bar.getHeight() + 5, 35, 35);
		add(fforward);	
		FForward fforwardEvent = new FForward();
		fforward.addActionListener(fforwardEvent);

		timeElapsed = new JTextField();
		timeElapsed.setBounds(FrameWidth - 129, FrameHeight + bar.getHeight() + (int) ((double) ((panelHeight-bar.getHeight())/2) - (double) (21/2)), 124, 21);
		add(timeElapsed);
		timeElapsed.setText("00:00.000 / 00:00.000");
	}
	
	private static class Play implements ActionListener{
		public void actionPerformed(ActionEvent playEvent){
			Count countEvent = new Count();
			if(!isPlaying){
				animation.start();
				play.setIcon(new ImageIcon(getClass().getResource("img/pause.png")));
				isPlaying = true;
				countFactor = 1;
				if(!isTimerStart){
					if(timer == null){ timer = new Timer(updateCountRate, countEvent); }
					play.addActionListener(countEvent);
					timer.start();
					isTimerStart = true;
				}
			} else{
				try { animation.pause(); }
				catch (Exception e) { System.out.print("FAIL"); }
				play.setIcon(new ImageIcon(getClass().getResource("img/play.png")));
				isPlaying = false;
				countFactor = 0;
			}
		}
	}
	
	private static class Stop implements ActionListener{
		public void actionPerformed(ActionEvent stopEvent){
			animation.stop();
			play.setIcon(new ImageIcon(getClass().getResource("img/play.png")));
			isPlaying = false;
			timer.stop();
			isTimerStart = false;
			timeElapsed.setText("00:00.000 / " + totalTime);
			time = 0;
		}
	}
	
	private static class Rewind implements ActionListener{
		public void actionPerformed(ActionEvent rewindEvent){
			animation.rewind();
			play.setIcon(new ImageIcon(getClass().getResource("img/play.png")));
			isPlaying = false;
			countFactor = -1;
		}
	}
	
	private static class FForward implements ActionListener{
		public void actionPerformed(ActionEvent fforwardEvent){
			animation.fforward();
			play.setIcon(new ImageIcon(getClass().getResource("img/play.png")));
			isPlaying = false;
			if(!isTimerStart){
				Count countEvent = new Count();
				timer = new Timer(updateCountRate, countEvent);
				fforward.addActionListener(countEvent);
				timer.start();
			}
			countFactor = 2;
		}
	}
	
	private static class Count implements ActionListener{
		public void actionPerformed(ActionEvent countEvent){
			time += updateCountRate * countFactor;
			if(time > 0 && time < endTime){
				timeElapsed.setText("" + (time/60000 < 10 ? 0 : "") + time/60000 + ':' + (time%60000/1000 < 10 ? 0 : "")
				+ time%60000/1000 + '.' + (time%1000 < 100 ? 0 : "") + (time%1000 < 10 ? 0 : "") + time%1000 + " / " + totalTime);
			} else{
				timer.stop();
				timeElapsed.setText("00:00.000 / " + totalTime);
			}
		}
	}
}
