/*
 *  Player Java Client 3 - SoundInterface.java
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
 * $Id: SoundInterface.java 125 2011-03-24 02:24:05Z corot $
 *
 */
package playerclient3;

import java.io.IOException;

import playerclient3.xdr.OncRpcException;
import playerclient3.xdr.XdrBufferEncodingStream;

/**
 * The sound interface allows playback of a pre-recorded sound (e.g., on an Amigobot).
 * This interface provides no data.
 * @author Radu Bogdan Rusu, Josh Bers
 * @version
 * <ul>
 *      <li>v3.0 - Player 3.0 supported
 * </ul>
 * TODO Move functionality to the (still inexistent in javaclient) AudioInterface
 */
public class SoundInterface extends PlayerDevice {

    /**
     * Constructor for SoundInterface.
     * @param pc a reference to the PlayerClient object
     */
    public SoundInterface (PlayerClient pc) { super (pc); }

    /**
     * The sound interface accepts an index of a pre-recorded sound file to
     * play.
     * @param index index of sound to be played
     */
    public void play (int index) {
        try {
            sendHeader (PLAYER_MSGTYPE_CMD, PLAYER_SOUND_CMD_IDX, 4);
            XdrBufferEncodingStream xdr = new XdrBufferEncodingStream (4);
            xdr.beginEncoding (null, 0);
            xdr.xdrEncodeInt (index);
            xdr.endEncoding ();
            os.write (xdr.getXdrData (), 0, xdr.getXdrLength ());
            xdr.close ();
            os.flush ();
        } catch (IOException e) {
            throw new PlayerException
                ("[Sound] : Couldn't send play command request: " +
                    e.toString (), e);
        } catch (OncRpcException e) {
            throw new PlayerException
                ("[Sound] : Couldn't XDR-encode play command request: " +
                    e.toString (), e);
        }
    }
}
