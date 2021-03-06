/*
 *  Player Java Client 3 - PlayerPosition3dGeom.java
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
 * $Id: PlayerPosition3dGeom.java 125 2011-03-24 02:24:05Z corot $
 *
 */

package playerclient3.structures.position3d;

import playerclient3.structures.*;

/**
 * Request/reply: Query geometry.
 * To request robot geometry, send a null PLAYER_POSITION3D_GET_GEOM request.
 * @author Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v3.0 - Player 3.0 supported
 * </ul>
 */
public class PlayerPosition3dGeom implements PlayerConstants {

    // Pose of the robot base, in the robot cs [m, m, m, rad, rad, rad].
    private PlayerPose3d pose;
    // Dimensions of the base [m, m, m].
    private PlayerBbox3d size;


    /**
     * @return  Pose of the robot base, in the robot cs [m, m, m, rad, rad, rad].
     */
    public synchronized PlayerPose3d getPose () {
        return this.pose;
    }

    /**
     * @param newPose  Pose of the robot base, in the robot cs [m, m, m, rad, rad, rad].
     */
    public synchronized void setPose (PlayerPose3d newPose) {
        this.pose = newPose;
    }

    /**
     * @return  Dimensions of the base [m, m, m].
     */
    public synchronized PlayerBbox3d getSize () {
        return this.size;
    }

    /**
     * @param newSize  Dimensions of the base [m, m, m].
     */
    public synchronized void setSize (PlayerBbox3d newSize) {
        this.size = newSize;
    }
}