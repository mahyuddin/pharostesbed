/*
 *  Player Java Client 3 - IRInterface.java
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
 * $Id: IRInterface.java 125 2011-03-24 02:24:05Z corot $
 *
 */
package playerclient3;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import playerclient3.structures.PlayerMsgHdr;
import playerclient3.structures.PlayerPose3d;
import playerclient3.structures.ir.PlayerIrData;
import playerclient3.structures.ir.PlayerIrPose;
import playerclient3.xdr.OncRpcException;
import playerclient3.xdr.XdrBufferDecodingStream;
import playerclient3.xdr.XdrBufferEncodingStream;

/**
 * The ir interface provides access to an array of infrared (IR) range sensors.
 * @deprecated Use the ranger instead.
 * @author Radu Bogdan Rusu, Maxim Batalin
 * @version
 * <ul>
 *      <li>v3.0 - Player 3.0 supported
 * </ul>
 */
public class AccelInterface extends PlayerDevice {

    private static final boolean isDebugging = PlayerClient.isDebugging;

    // Logging support
    private Logger logger = Logger.getLogger (IRInterface.class.getName ());

    private PlayerAccelData acceldata;
    private boolean      readyAcceldata = false;
    
    /**
     * Constructor for IRInterface.
     * @param pc a reference to the PlayerClient object
     */
    public AccelInterface (PlayerClient pc) { super(pc); }

    /**
     * Read the IR values.
     */
    public synchronized void readData (PlayerMsgHdr header) {
        try {
            switch (header.getSubtype ()) {
                case PLAYER_IMU_DATA_CALIB: {
                    this.timestamp = header.getTimestamp();

                    // Buffer for reading voltages_count
                    byte[] buffer = new byte[4];

                    // Read voltages_count
                    is.readFully (buffer, 0, 4);

                    // Begin decoding the XDR buffer
                    XdrBufferDecodingStream xdr = new XdrBufferDecodingStream (buffer);
                    xdr.beginDecoding ();
                    short[] accelArray;         
                    accelArray = new short[2];      
                    accelArray[0] = xdr.xdrDecodeShort ();
                    accelArray[1] = xdr.xdrDecodeShort ();
                    accelArray[2] = xdr.xdrDecodeShort ();
                    xdr.endDecoding   ();
                    xdr.close ();

                    acceldata = new PlayerAccelData ();
                    acceldata.setVoltages (voltages);
                    readyAcceldata = true;     
                    break;
                }
            }
        } catch (IOException e) {
            throw new PlayerException
                ("[IR] : Error reading payload: " +
                        e.toString(), e);
        } catch (OncRpcException e) {
            throw new PlayerException
                ("[IR] : Error while XDR-decoding payload: " +
                        e.toString(), e);
        }
    }

    /**
     * Get the state data.
     * @return an object of type PlayerIrData containing the requested data
     */
    public PlayerAccelData getData () { return this.acceldata; }

   
    /**
     * Check if data is available.
     * @return true if ready, false if not ready
     */
    public boolean isDataReady () {
        if (readyAcceldata) {
            readyAcceldata = false;
            return true;
        }
        return false;
    }

}
