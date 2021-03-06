/*
 *  Player Java Client 3 - PlayerSonarGeom.java
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
 * $Id: PlayerSonarGeom.java 125 2011-03-24 02:24:05Z corot $
 *
 */

package playerclient3.structures.sonar;

import playerclient3.structures.*;

/**
 * Data AND Request/reply: geometry.
 * To query the geometry of the sonar transducers, send a null
 * PLAYER_SONAR_REQ_GET_GEOM request.  Depending on the underlying
 * driver, this message can also be sent as data with the subtype
 * PLAYER_SONAR_DATA_GEOM.
 * @author Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v3.0 - Player 3.0 supported
 * </ul>
 */
public class PlayerSonarGeom implements PlayerConstants {

    // Pose of each sonar, in robot cs
    private PlayerPose3d[] poses;


    /**
     * @return  The number of valid poses.
     */
    public synchronized int getPoses_count () {
        return (this.poses == null)?0:this.poses.length;
    }

    /**
     * @return  Pose of each sonar, in robot cs
     */
    public synchronized PlayerPose3d[] getPoses () {
        return this.poses;
    }

    /**
     * @param newPoses  Pose of each sonar, in robot cs
     */
    public synchronized void setPoses (PlayerPose3d[] newPoses) {
        this.poses = newPoses;
    }
}