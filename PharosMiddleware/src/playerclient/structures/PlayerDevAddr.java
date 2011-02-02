/*
 *  Player Java Client - PlayerDevAddr.java
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
 * $Id: PlayerDevAddr.java,v 1.2 2006/02/20 22:44:57 veedee Exp $
 *
 */
package playerclient.structures;

import playerclient.PlayerClientUtils;

/**
 * Devices are identified by 12-byte addresses of this form. Some of the 
 * fields are transport-dependent in their intepretation.
 * (see the player_devaddr structure from player.h)
 * @author Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v2.0 - Player 2.0 supported
 * </ul>
 */
public class PlayerDevAddr {
	
	/**
	 * The device address is defined in player-2.1.3/libplayercore/player.h as follows:
	 * 
	 * typedef struct player_devaddr {
  	 *   uint32_t host;    // The "host" on which the device resides.
 	 *   uint32_t robot;   // The "robot" or device collection in which the device resides.
     *   uint16_t interf;  // The interface provided by the device; must be one of PLAYER_*_CODE
 	 *   uint16_t index;   // Which device of that interface
	 * } player_devaddr_t;
	 * 
	 */
    public static final int PLAYERXDR_DEVADDR_SIZE = 16;
    public static final int PLAYER_DEVADDR_SIZE    = 12;
    
	private int   host;		/* The "host" on which the device resides */
	private int   robot;	/* The "robot" or device collection in which the device resides */
	private short interf;	/* The interface provided by the device; must be one of PLAYER_*_CODE */
	private short index;	/* Which device of that interface */
    
	/**
	 * Returns true if this PlayerDevAddr matches the specified PlayerDevAddr passed
	 * in as a parameter.  A match exists if the host, robot, interf, and index 
	 * are all equal.
	 * 
	 * @param addr The PlayerDevAddr with which to compare.
	 * @return Whether a match exists.
	 */
	public boolean matches(PlayerDevAddr addr) {
		return (getHost() == addr.getHost() &&
				getIndex  () == addr.getIndex() && 
				getInterf () == addr.getInterf() && 
				getRobot  () == addr.getRobot());
	}
	
    /**
     * 
     * @return The "host" on which the device resides
     */
    public synchronized int getHost () {
        return this.host;
    }
    
    /**
     * 
     * @param newhost The "host" on which the device resides
     */
    public synchronized void setHost (int newhost) {
        this.host = newhost;
    }
    
    /**
     * 
     * @return The "robot" or device collection in which the device resides 
     */
    public synchronized int getRobot () {
        return this.robot;
    }
    
    /**
     * 
     * @param newrobot The "robot" or device collection in which the device resides 
     */
    public synchronized void setRobot (int newrobot) {
        this.robot = newrobot;
    }
    
    /**
     * Returns the interface provided by the device.  The interface is a value that
     * is defined by the PLAYER_*_CODE in javaclient2.structures.PlayerConstants.
     * 
     * @see playerclient.structures.PlayerConstants
     * @return The interface provided by the device; must be one of PLAYER_*_CODE 
     */
    public synchronized short getInterf () {
        return this.interf;
    }
    
    /**
     * 
     * @param newinterf The interface provided by the device; must be one of PLAYER_*_CODE 
     */
    public synchronized void setInterf (int newinterf) {
        this.interf = (short)newinterf;
    }
    
    /**
     * 
     * @return Which device of that interface
     */
    public synchronized short getIndex () {
        return this.index;
    }
    
    /**
     * 
     * @param newindex Which device of that interface 
     */
    public synchronized void setIndex (int newindex) {
        this.index = (short)newindex;
    }
    
	/**
	 * Returns a String representation of this class.
	 */
	public String toString() {
		return "(PlayerDevAddr host=" + host + " robot=" + robot + " interf=" 
			+ PlayerClientUtils.lookupName(interf) + "/" + interf + " index=" + index + ")";
	}
}
