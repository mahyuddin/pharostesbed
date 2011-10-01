/*
 *  Player Java Client 3 - PlayerIrPose.java
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
 * $Id: PlayerIrPose.java 125 2011-03-24 02:24:05Z corot $
 *
 */

package playerclient3.structures.ir;

import playerclient3.structures.*;

/**
 * Request/reply: get pose
 * To query the pose of the IRs, send a null PLAYER_IR_REQ_POSE request.
 * @author Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v3.0 - Player 3.0 supported
 * </ul>
 */
public class PlayerIrPose implements PlayerConstants {

    // the pose of each IR detector on this robot
    private PlayerPose3d[] poses;


    /**
     * @return  the number of IR samples returned by this robot
     */
    public synchronized int getPoses_count () {
        return (this.poses == null)?0:this.poses.length;
    }

    /**
     * @return  the pose of each IR detector on this robot
     */
    public synchronized PlayerPose3d[] getPoses () {
        return this.poses;
    }

    /**
     * @param newPoses  the pose of each IR detector on this robot
     */
    public synchronized void setPoses (PlayerPose3d[] newPoses) {
        this.poses = newPoses;
    }
}