/*
 *  Player Java Client 2 - AbstractPositionDevice.java
 *  Copyright (C) 2002-2006 Radu Bogdan Rusu, Maxim Batalin
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
 * $Id: AbstractPositionDevice.java,v 1.3 2006/03/06 08:33:30 veedee Exp $
 *
 */
package playerclient;

/**
 * Abstract class for all Player Position* interfaces. Used for 
 * PositionControl and HeadingControl.
 * @author Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v2.0 - Player 2.0 supported
 * </ul>
 */
public abstract class AbstractPositionDevice extends PlayerDevice {

    /**
     * Abstract constructor for each AbstractPositionDevice.
     * @param plc a reference to the PlayerClient object
     */
    public AbstractPositionDevice (PlayerClient plc) { super (plc); }
    
    public abstract double getX() throws NoNewDataException;
    public abstract double getY() throws NoNewDataException;
    public abstract double getYaw() throws NoNewDataException;
    public abstract void  setSpeed (double speed, double turnrate);
}
