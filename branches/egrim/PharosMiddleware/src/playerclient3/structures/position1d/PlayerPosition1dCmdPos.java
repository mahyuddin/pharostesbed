/*
 *  Player Java Client 3 - PlayerPosition1dCmdPos.java
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
 * $Id: PlayerPosition1dCmdPos.java 125 2011-03-24 02:24:05Z corot $
 *
 */

package playerclient3.structures.position1d;

import playerclient3.structures.*;

/**
 * Command: state (PLAYER_POSITION1D_CMD_POS)
 * The position1d interface accepts new positions and/or velocities for
 * the robot's motors (drivers may support position control, speed control,
 * or both).
 * @author Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v3.0 - Player 3.0 supported
 * </ul>
 */
public class PlayerPosition1dCmdPos implements PlayerConstants {

    // position [m] or [rad]
    private float pos;
    // velocity at which to move to the position [m/s] or [rad/s]
    private float vel;
    // Motor state (FALSE is either off or locked, depending on the driver).
    private byte state;


    /**
     * @return position [m] or [rad]
     */
    public synchronized float getPos () {
        return this.pos;
    }

    /**
     * @param newPos position [m] or [rad]
     */
    public synchronized void setPos (float newPos) {
        this.pos = newPos;
    }

    /**
     * @return velocity at which to move to the position [m/s] or [rad/s]
     */
    public synchronized float getVel () {
        return this.vel;
    }

    /**
     * @param newVel velocity at which to move to the position [m/s] or [rad/s]
     */
    public synchronized void setVel (float newVel) {
        this.vel = newVel;
    }

    /**
     * @return  Motor state (FALSE is either off or locked, depending on the driver).
     */
    public synchronized byte getState () {
        return this.state;
    }

    /**
     * @param newState  Motor state (FALSE is either off or locked, depending on the driver).
     */
    public synchronized void setState (byte newState) {
        this.state = newState;
    }
}