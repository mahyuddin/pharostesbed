/*
 * Interface.java
 * GUI interface for visualizer
 * Lok Wong
 * Pharos Lab
 * Created: June 11, 2012 9:55 AM
 * Last Modified: June 17, 2012 9:01 AM
 */

package visualizer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Interface extends JFrame {
	
	public static JButton play, stop, rewind, fforward;
	private static boolean isPlaying = false;
	private static int FrameWidth = 700, FrameHeight = 700;
	static Animation animation = new Animation();
	
	public static void main(String args[]){
		Interface gui = new Interface();
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gui.setSize(FrameWidth + 15, FrameHeight + 80);
		gui.setVisible(true);
		gui.setTitle("Pharos Lab Visualizer");
		
		animation.setLocation(0, 0);
		gui.add(animation);
	}
	
	public Interface(){
		setLayout(null);
		
		play = new JButton(new ImageIcon(getClass().getResource("img/play.png")));
		play.setLayout(null);
		play.setBounds(5, FrameHeight + 5, 35, 35);
		add(play);

		stop = new JButton(new ImageIcon(getClass().getResource("img/stop.png")));
		stop.setLayout(null);
		stop.setBounds(play.getX() + play.getWidth() + 5, FrameHeight + 5, 35, 35);
		add(stop);

		rewind = new JButton(new ImageIcon(getClass().getResource("img/rewind.png")));
		rewind.setLayout(null);
		rewind.setBounds(stop.getX() + stop.getWidth() + 10, FrameHeight + 5, 35, 35);
		add(rewind);

		fforward = new JButton(new ImageIcon(getClass().getResource("img/fforward.png")));
		fforward.setLayout(null);
		fforward.setBounds(rewind.getX() + rewind.getWidth() + 5, FrameHeight + 5, 35, 35);
		add(fforward);	

		Play playEvent = new Play();
		play.addActionListener(playEvent);

		Stop stopEvent = new Stop();
		stop.addActionListener(stopEvent);
		
		Rewind rewindEvent = new Rewind();
		rewind.addActionListener(rewindEvent);
		
		FForward fforwardEvent = new FForward();
		fforward.addActionListener(fforwardEvent);	
	}
	
	private static class Play implements ActionListener{
		public void actionPerformed(ActionEvent playEvent){
			if(isPlaying == false){
				animation.start();
				play.setIcon(new ImageIcon(getClass().getResource("img/pause.png")));
				isPlaying = true;
			} else if(isPlaying == true){
				try { animation.pause(); }
				catch (Exception e) { System.out.print("FAIL"); }
				play.setIcon(new ImageIcon(getClass().getResource("img/play.png")));
				isPlaying = false;
			}
		}
	}
	
	private static class Stop implements ActionListener{
		public void actionPerformed(ActionEvent stopEvent){
			animation.stop();
			play.setIcon(new ImageIcon(getClass().getResource("img/play.png")));
			isPlaying = false;
		}
	}
	
	private static class Rewind implements ActionListener{
		public void actionPerformed(ActionEvent rewindEvent){
			animation.rewind();
			play.setIcon(new ImageIcon(getClass().getResource("img/pause.png")));
			isPlaying = true;
		}
	}
	
	private static class FForward implements ActionListener{
		public void actionPerformed(ActionEvent fforwardEvent){
			animation.fforward();
			play.setIcon(new ImageIcon(getClass().getResource("img/pause.png")));
			isPlaying = true;
		}
	}
	
}
