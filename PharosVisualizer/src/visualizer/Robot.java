/*
 * Robot.java
 * Robot object
 * Lok Wong
 * Pharos Lab
 * Created: July 21, 2012 9:04 PM
 * Last Modified: July 25, 2012 12:03 AM
 */

package visualizer;

import java.util.*;
import java.awt.*;

public class Robot {
	public int n;
	public String name;
	public Vector<Position> frames;
	public RobotPath path;
	public Color color;
	public boolean isFinished;
	
	public Robot(){
		this.n = 0;
		this.name = "null";
		this.frames = new Vector<Position>();
		this.path = new RobotPath();
		this.color = new Color(0, 0, 0, 0);
		this.isFinished = false;
	}
	
	public Robot(int initN, String initName, Color initColor){
		this.n = initN;
		this.name = initName;
		this.frames = new Vector<Position>();
		this.path = new RobotPath();
		this.color = initColor;
		this.isFinished = false;
	}	
}
