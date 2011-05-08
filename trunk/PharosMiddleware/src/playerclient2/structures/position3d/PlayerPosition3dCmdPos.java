/*
 *  Player Java Client 2 - PlayerPosition3dCmdPos.java
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
 * $Id: PlayerPosition3dCmdPos.java,v 1.1.1.1 2006/02/15 17:51:18 veedee Exp $
 *
 */

package playerclient2.structures.position3d;

import playerclient2.structures.*;

/**
 * Command: position (PLAYER_POSITION3D_CMD_SET_POS)
 * It accepts new positions and/or velocities for the robot's motors
 * (drivers may support position control, speed control, or both).  
 * @author Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v2.0 - Player 2.0 supported
 * </ul>
 */
public class PlayerPosition3dCmdPos implements PlayerConstants {

    // (x, y, z, roll, pitch, yaw) position [m, m, m, rad, rad, rad] 
    private PlayerPose3d pos;
    // Motor state (FALSE is either off or locked, depending on the driver). 
    private byte state;


    /**
     * @return  (x, y, z, roll, pitch, yaw) position [m, m, m, rad, rad, rad] 
     **/
    public synchronized PlayerPose3d getPos () {
        return this.pos;
    }

    /**
     * @param newPos  (x, y, z, roll, pitch, yaw) position [m, m, m, rad, rad, rad] 
     *
     */
    public synchronized void setPos (PlayerPose3d newPos) {
        this.pos = newPos;
    }
    /**
     * @return  Motor state (FALSE is either off or locked, depending on the driver). 
     **/
    public synchronized byte getState () {
        return this.state;
    }

    /**
     * @param newState  Motor state (FALSE is either off or locked, depending on the driver). 
     *
     */
    public synchronized void setState (byte newState) {
        this.state = newState;
    }

}