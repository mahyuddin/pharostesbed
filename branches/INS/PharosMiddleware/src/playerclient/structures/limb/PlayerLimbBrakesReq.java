/*
 *  Player Java Client 2 - PlayerLimbBrakesReq.java
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
 * $Id: PlayerLimbBrakesReq.java,v 1.1.1.1 2006/02/15 17:51:18 veedee Exp $
 *
 */

package playerclient.structures.limb;

import playerclient.structures.*;

/**
 * Request/reply: Brakes.
 * Turn the brakes of the limb on or off by sending a PLAYER_LIMB_BRAKES_REQ
 * request. Null response
 * @author Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v2.0 - Player 2.0 supported
 * </ul>
 */
public class PlayerLimbBrakesReq implements PlayerConstants {

    // Brakes setting; 0 for off, 1 for on. 
    private byte value;


    /**
     * @return  Brakes setting; 0 for off, 1 for on. 
     **/
    public synchronized byte getValue () {
        return this.value;
    }

    /**
     * @param newValue  Brakes setting; 0 for off, 1 for on. 
     *
     */
    public synchronized void setValue (byte newValue) {
        this.value = newValue;
    }

}