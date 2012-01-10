/*
 *  Player Java Client 3 - PlayerIrPowerReq.java
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
 * $Id: PlayerIrPowerReq.java 125 2011-03-24 02:24:05Z corot $
 *
 */

package playerclient3.structures.ir;

import playerclient3.structures.*;

/**
 * Request/reply: set power
 * To turn IR power on and off, send a PLAYER_IR_POWER request.
 * Null response.
 * @author Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v3.0 - Player 3.0 supported
 * </ul>
 */
public class PlayerIrPowerReq implements PlayerConstants {

    // FALSE for power off, TRUE for power on
    private byte state;


    /**
     * @return  FALSE for power off, TRUE for power on
     */
    public synchronized byte getState () {
        return this.state;
    }

    /**
     * @param newState  FALSE for power off, TRUE for power on
     */
    public synchronized void setState (byte newState) {
        this.state = newState;
    }
}