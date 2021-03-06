/*
 *  Player Java Client 3 - PlayerPtzReqControlMode.java
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
 * $Id: PlayerPtzReqControlMode.java 125 2011-03-24 02:24:05Z corot $
 *
 */

package playerclient3.structures.ptz;

import playerclient3.structures.*;

/**
 * Request/reply: Control mode.
 * To switch between position and velocity control (for those drivers that
 * support it), send a PLAYER_PTZ_REQ_CONTROL_MODE request.  Note that this
 * request changes how the driver interprets forthcoming commands from all
 * clients.  Null response.
 * @author Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v3.0 - Player 3.0 supported
 * </ul>
 */
public class PlayerPtzReqControlMode implements PlayerConstants {

    // Mode to use: must be either PLAYER_PTZ_VELOCITY_CONTROL or
    // PLAYER_PTZ_POSITION_CONTROL.
    private int mode;


    /**
     * @return  Mode to use: must be either PLAYER_PTZ_VELOCITY_CONTROL or
     * PLAYER_PTZ_POSITION_CONTROL.
     */
    public synchronized int getMode () {
        return this.mode;
    }

    /**
     * @param newMode  Mode to use: must be either PLAYER_PTZ_VELOCITY_CONTROL or
     * PLAYER_PTZ_POSITION_CONTROL.
     */
    public synchronized void setMode (int newMode) {
        this.mode = newMode;
    }
}