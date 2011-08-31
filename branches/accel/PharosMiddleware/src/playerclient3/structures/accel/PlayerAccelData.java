/*
 *  Player Java Client 3 - PlayerIrData.java
 *  Copyright (C) 2006 Radu Bogdan Rusu
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * $Id: PlayerIrData.java 125 2011-03-24 02:24:05Z corot $
 *
 */

package playerclient3.structures.accel;

import playerclient3.structures.*;

/**
 * Data: ranges (PLAYER_IR_DATA_RANGES)
 * The ir interface returns range readings from the IR array.
 * @author Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v3.0 - Player 3.0 supported
 * </ul>
 */
public class PlayerAccelData implements PlayerConstants {
	 	
	private float x_axis;
	private float y_axis;
	private float z_axis;
	//X
	 public synchronized float getX_axis () {
	        return this.x_axis;
	    }

    public synchronized void setX_axis (float newX_axis) {
	        this.x_axis = newX_axis;
	    }
    //Y
	 public synchronized float getY_axis () {
		        return this.y_axis;
		    }

	   public synchronized void setY_axis (float newY_axis) {
		        this.y_axis = newY_axis;
		    }
	   //Z
		 public synchronized float getZ_axis () {
			        return this.z_axis;
			    }

	    public synchronized void setZ_axis (float newZ_axis) {
			        this.z_axis = newZ_axis;
			    }
}