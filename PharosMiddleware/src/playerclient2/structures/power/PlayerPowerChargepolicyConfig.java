/*
 *  Player Java Client 2 - PlayerPowerChargepolicyConfig.java
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
 * $Id: PlayerPowerChargepolicyConfig.java,v 1.1.1.1 2006/02/15 17:51:18 veedee Exp $
 *
 */

package playerclient2.structures.power;

import playerclient2.structures.*;

/**
 * Request/reply: set charging policy
 *  
 * @author Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v2.0 - Player 2.0 supported
 * </ul>
 */
public class PlayerPowerChargepolicyConfig implements PlayerConstants {

    // uint8_tean controlling recharging. If FALSE, recharging is
//       disabled. Defaults to TRUE 
    private byte enable_input;
    // uint8_tean controlling whether others can recharge from this
//       device. If FALSE, charging others is disabled. Defaults to TRUE.
    private byte enable_output;


    /**
     * @return  uint8_tean controlling recharging. If FALSE, recharging is
     *       disabled. Defaults to TRUE 
     **/
    public synchronized byte getEnable_input () {
        return this.enable_input;
    }

    /**
     * @param newEnable_input  uint8_tean controlling recharging. If FALSE, recharging is
     *       disabled. Defaults to TRUE 
     *
     */
    public synchronized void setEnable_input (byte newEnable_input) {
        this.enable_input = newEnable_input;
    }
    /**
     * @return  uint8_tean controlling whether others can recharge from this
     *       device. If FALSE, charging others is disabled. Defaults to TRUE.
     **/
    public synchronized byte getEnable_output () {
        return this.enable_output;
    }

    /**
     * @param newEnable_output  uint8_tean controlling whether others can recharge from this
     *       device. If FALSE, charging others is disabled. Defaults to TRUE.
     *
     */
    public synchronized void setEnable_output (byte newEnable_output) {
        this.enable_output = newEnable_output;
    }

}