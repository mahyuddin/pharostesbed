/*
 * RobotPath.java
 * Marks path robots travel
 * Lok Wong
 * Pharos Lab
 * Created: July 15, 2012 8:55 PM
 * Last Modified: July 16, 2012 11:11 PM
 */

package visualizer;

import java.util.Arrays;

public class RobotPath {
	public int[] pos = new int[100];
	public int[] x = new int[100];
	public int[] y = new int[100];
	
	public void add(int i, int initPos, int initX, int initY){
		if(i >= this.pos.length){ this.expand(this.pos.length); }
		
		this.pos[i] = initPos;
		this.x[i] = initX;
		this.y[i] = initY;
	}
	
	private void expand(int length){
		this.pos = Arrays.copyOf(this.pos, length*2);
		this.x = Arrays.copyOf(this.x, length*2);
		this.y = Arrays.copyOf(this.y, length*2);
	}
}
