/*
 * Robot.java
 * Robot object
 * Lok Wong
 * Pharos Lab
 * Created: July 21, 2012 9:04 PM
 * Last Modified: August 4, 2012 11:21 PM
 */

package visualizer;

import java.util.*;
import java.awt.*;

public class Robot {
	public int n;
	public String name;
	public Vector<Position> frames;
	public RobotPath path;
	public int pathIndex;
	public Color color;
	public boolean isFinished, showRobot, showPath;
	
	public Robot(){
		this.n = 0;
		this.name = "null";
		this.frames = new Vector<Position>();
		this.path = new RobotPath();
		this.pathIndex = 0;
		this.color = new Color(0, 0, 0, 0);
		this.isFinished = false;
		this.showRobot = true;
		this.showPath = true;
	}
	
	public Robot(int initN, String initName, Color initColor){
		this.n = initN;
		this.name = initName;
		this.frames = new Vector<Position>();
		this.path = new RobotPath();
		this.pathIndex = 0;
		this.color = initColor;
		this.isFinished = false;
		this.showRobot = true;
		this.showPath = true;
	}	
}
