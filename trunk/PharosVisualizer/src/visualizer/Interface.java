/*
 * Interface.java
 * GUI interface for visualizer
 * Lok Wong
 * Pharos Lab
 * Created: June 11, 2012 9:55 AM
 * Last Modified: July 15, 2012 12:28 AM
 */

package visualizer;

import javax.swing.*;
import java.awt.event.*;

public class Interface extends JFrame {
	
	public static JSlider progress;
	private final static int timeIncrements = 2000;
	
	public static JButton play, stop, rewind, fforward;
	private static ImageIcon playImg, pauseImg, stopImg, rewindImg, fforwardImg;
	private static boolean isPlaying = false;
	
	static JTextField timeElapsed;
	private static long time = 0;
	private static String totalTime = new String();
	private static long endTime;
	private static Timer timer;
	private final static int updateCountRate = 500;
	private static int countFactor = 1;				// 1 for play, >1 for fast forward, etc.
	private static boolean isTimerStart = false;
	
	private static int frameWidth = 700, frameHeight = 700, panelHeight = 65;
	static Animation animation = new Animation();
	
	public static void main(String args[]){
/*		for(int i = 0; i < args.length; i++){
			if(args[i].equals("-exp")){
				
			}
		}
*/		
		Interface gui = new Interface();
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gui.setSize(frameWidth + 15, frameHeight + panelHeight + 35);
		gui.setVisible(true);
		gui.setTitle("Pharos Lab Visualizer");
		gui.getContentPane().setBounds(0, 0, 700, 700);
		
		animation.setLocation(0, 0);
		gui.add(animation);
		
		endTime = animation.endTime;
		// Set start at "00:00 / endTime" with endTime in terms of minutes, seconds, and milliseconds with leading zeros
		totalTime = ("" + (endTime/60000 < 10 ? 0 : "") + (endTime/60000) + ':' + (endTime%60000/1000 < 10 ? 0 : "")
				+ endTime%60000/1000);
		timeElapsed.setText("00:00 / " + totalTime);
	}
	
	public Interface(){
		setLayout(null);
		
		UIDefaults def = UIManager.getDefaults();
		ImageIcon thumb = new ImageIcon(getClass().getResource("img/slider_thumb.png"));
		def.put("Slider.horizontalThumbIcon", thumb);
		progress = new JSlider(0, timeIncrements, 0);
		progress.setBounds(0, frameHeight, 699, thumb.getIconHeight());
		add(progress);
		
		playImg = new ImageIcon(getClass().getResource("img/play.png"));
		pauseImg = new ImageIcon(getClass().getResource("img/pause.png"));
		play = new JButton(playImg);
		play.setBounds(5, progress.getY() + progress.getHeight() + 5, 35, 35);
		add(play);
		Play playEvent = new Play();
		play.addActionListener(playEvent);

		stopImg = new ImageIcon(getClass().getResource("img/stop.png"));
		stop = new JButton(stopImg);
		stop.setBounds(play.getX() + play.getWidth() + 5, progress.getY() + progress.getHeight() + 5, 35, 35);
		add(stop);
		Stop stopEvent = new Stop();
		stop.addActionListener(stopEvent);

		rewindImg = new ImageIcon(getClass().getResource("img/rewind.png"));
		rewind = new JButton(rewindImg);
		rewind.setBounds(stop.getX() + stop.getWidth() + 10, progress.getY() + progress.getHeight() + 5, 35, 35);
		add(rewind);
		Rewind rewindEvent = new Rewind();
		rewind.addActionListener(rewindEvent);

		fforwardImg = new ImageIcon(getClass().getResource("img/fforward.png"));
		fforward = new JButton(fforwardImg);
		fforward.setBounds(rewind.getX() + rewind.getWidth() + 5, progress.getY() + progress.getHeight() + 5, 35, 35);
		add(fforward);	
		FForward fforwardEvent = new FForward();
		fforward.addActionListener(fforwardEvent);

		timeElapsed = new JTextField();
		timeElapsed.setBounds(frameWidth - 81, progress.getY() + progress.getHeight()
				+ (int) ((double) ((panelHeight-progress.getHeight())/2) - (double) (21/2)), 76, 21);
		add(timeElapsed);
		timeElapsed.setText("00:00 / 00:00");
	}
	
	private static class Play implements ActionListener{
		public void actionPerformed(ActionEvent playEvent){
			if(!isPlaying){
				// Play pressed
				Count countEvent = new Count();
				animation.start();
				play.setIcon(pauseImg);
				isPlaying = true;
				countFactor = 1;
				if(timer == null){ timer = new Timer(updateCountRate, countEvent); }
				play.addActionListener(countEvent);
				timer.start();
				isTimerStart = true;
			} else{
				// Pause pressed
				try { animation.pause(); }
				catch (Exception e) {}
				play.setIcon(playImg);
				isPlaying = false;
				countFactor = 0;
			}
		}
	}
	
	private static class Stop implements ActionListener{
		public void actionPerformed(ActionEvent stopEvent){
			animation.stop();
			play.setIcon(playImg);
			isPlaying = false;
			timer.stop();
			isTimerStart = false;
			timeElapsed.setText("00:00 / " + totalTime);
			progress.setValue(0);
			countFactor = 0;
			time = 0;
		}
	}
	
	private static class Rewind implements ActionListener{
		public void actionPerformed(ActionEvent rewindEvent){
			animation.rewind();
			play.setIcon(playImg);
			isPlaying = false;
			countFactor = -1;
		}
	}
	
	private static class FForward implements ActionListener{
		public void actionPerformed(ActionEvent fforwardEvent){
			animation.fforward();
			play.setIcon(playImg);
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
			if(time >= 0){ time += updateCountRate * countFactor; }
			if(time < 0 && time >= endTime){
				timer.stop();
				timeElapsed.setText("00:00 / " + totalTime);
			} else{
				timeElapsed.setText("" + (time/60000 < 10 ? 0 : "") + time/60000 + ':' + (time%60000/1000 < 10 ? 0 : "")
				+ time%60000/1000 + " / " + totalTime);
			}
			progress.setValue((int) (time * timeIncrements / endTime));
		}
	}
}
