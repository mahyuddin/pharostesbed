/*
 *  Player Java Client 2 - GPSInterface.java
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
 * $Id: GPSInterface.java,v 1.4 2006/03/10 19:04:59 veedee Exp $
 *
 */
package playerclient2;

import java.io.IOException;
import java.util.Enumeration;

import playerclient2.structures.PlayerMsgHdr;
import playerclient2.structures.gps.PlayerGpsData;
import playerclient2.xdr.OncRpcException;
import playerclient2.xdr.XdrBufferDecodingStream;



/**
 * The gps interface provides access to an absolute position system, such as GPS.
 * This interface accepts no commands. 
 * @author Radu Bogdan Rusu, Maxim Batalin
 * @version
 * <ul>
 *      <li>v2.0 - Player 2.0 supported
 * </ul>
 */
public class GPSInterface extends PlayerDevice {
	/** 
	 * The maximum amount of time to wait for GPS data before giving up.
	 */
	public static final int GPS_DATA_TIMEOUT = 5000;
    private PlayerGpsData pgdata = null;
    private boolean readyPgdata = false;
    private java.util.Vector<GPSListener> gpsListeners = new java.util.Vector<GPSListener>();

    /**
     * Constructor for GPSInterface.
     * @param pc a reference to the PlayerClient object
     */
    public GPSInterface (PlayerClient pc) { super (pc); }
    
    /**
     * Read the current global position and heading information.
     */
    public synchronized void readData (PlayerMsgHdr header) {
        try {
        	switch (header.getSubtype ()) {
        		case PLAYER_GPS_DATA_STATE: {
        			synchronized(this) {
        				pgdata = new PlayerGpsData ();

        				// Buffer for player_gps_data
        				byte[] buffer = new byte[68];
        				// Read player_gps_data
        				is.readFully (buffer, 0, 68);

        				
        				/*
        				 * The GPS struct is defined in libplayercore/player_interfaces.h:
        				 * 
        				 * typedef struct player_gps_data {
        				 *   uint32_t time_sec;  // 4 bytes
        				 *   uint32_t time_usec; // 4 bytes
        				 *   int32_t latitude;   // 4 bytes
        				 *   int32_t longitude;  // 4 bytes
        				 *   int32_t altitude;   // 4 bytes
        				 *   double utm_e;       // 8 bytes
        				 *   double utm_n;       // 8 bytes
        				 *   uint32_t quality;   // 4 bytes
        				 *   uint32_t num_sats;  // 4 bytes
        				 *   uint32_t hdop;      // 4 bytes
        				 *   uint32_t vdop;      // 4 bytes
        				 *   double err_horz;    // 8 bytes
        				 *   double err_vert;    // 8 bytes
        				 * }
        				 * 
        				 * Total: 68 bytes
        				 */
        				// Begin decoding the XDR buffer
        				XdrBufferDecodingStream xdr = new XdrBufferDecodingStream (buffer);
        				xdr.beginDecoding ();
        				pgdata.setTime_sec  (xdr.xdrDecodeInt ());
        				pgdata.setTime_usec (xdr.xdrDecodeInt ());
        				pgdata.setLatitude  (xdr.xdrDecodeInt ());
        				pgdata.setLongitude (xdr.xdrDecodeInt ());
        				pgdata.setAltitude  (xdr.xdrDecodeInt ());
        				pgdata.setUtm_e     (xdr.xdrDecodeDouble ());
        				pgdata.setUtm_n     (xdr.xdrDecodeDouble ());
        				pgdata.setQuality   (xdr.xdrDecodeInt ());
        				pgdata.setNum_sats  (xdr.xdrDecodeInt ());
        				pgdata.setHdop      (xdr.xdrDecodeInt ());
        				pgdata.setVdop      (xdr.xdrDecodeInt ());
        				pgdata.setErr_horz  (xdr.xdrDecodeDouble ());
        				pgdata.setErr_vert  (xdr.xdrDecodeDouble ());
        				xdr.endDecoding   ();
        				xdr.close ();

        				if (PlayerClient.isDebugging) {
        					System.out.println("[GPSInterface.readData(...)] " + pgdata);
        				}
        				readyPgdata = true;
        				notifyP2DListeners();
        			}
        			break;
        		}
        	}
        } catch (IOException e) {
        	throw new PlayerException 
        		("[GPS] : Error reading payload: " + 
        				e.toString(), e);
        } catch (OncRpcException e) {
        	throw new PlayerException 
        		("[GPS] : Error while XDR-decoding payload: " + 
        				e.toString(), e);
        }
    }
    
    /**
     * Adds a listener for GPS data.  All listeners are notified whenever
     * a new GPS data arrives.
     * 
     * @param gpsl  The listener to add.
     */
    public void addGPSListener(GPSListener gpsl) {
    	gpsListeners.add(gpsl);
    }
    
    /**
     * Removes a listener from this object.
     * 
     * @param gpsl  The listener to remove.
     */
    public void removeGPSListener(GPSListener gpsl) {
    	gpsListeners.remove(gpsl);
    }
    
    /**
     * Notifies each of the registered GPSListener objects that a new PlayerGpsData is available.
     */
    private void notifyP2DListeners() {
    	Enumeration<GPSListener> e = gpsListeners.elements();
    	while (e.hasMoreElements()) {
    		e.nextElement().newGPSData(pgdata);
    	}
    }
    
    /**
     * Get the GPS data.
     * 
     * @return The GPS data.
     * @throws NoNewDataException If no new GPS data was received since the last time this method was called.
     */
    public PlayerGpsData getData() throws NoNewDataException {
    	if (pgdata == null || !readyPgdata)
    		throw new NoNewDataException();
    	else {
    		readyPgdata = false;
    		return pgdata;
    	}
    }
    
//    /**
//     * Check if data is available.
//     * @return true if ready, false if not ready 
//     */
//    public boolean isDataReady () {
//        if (readyPgdata) {
//        	readyPgdata = false;
//            return true;
//        }
//        return false;
//    }
}
