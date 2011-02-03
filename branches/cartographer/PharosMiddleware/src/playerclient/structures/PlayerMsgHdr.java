/*
 *  Player Java Client 2 - PlayerMsgHdr.java
 *  Copyright (C) 2002-2006 Radu Bogdan Rusu, Maxim Batalin, Esben Ostergaard
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
 * $Id: PlayerMsgHdr.java,v 1.2 2006/02/20 22:44:57 veedee Exp $
 *
 */
package playerclient.structures;

import playerclient.PlayerClientUtils;

/**
 * Every message starts with this header.
 * (see the player_msghdr structure from player.h)
 * @author Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v2.0 - Player 2.0 supported
 * </ul>
 */
public class PlayerMsgHdr {
	
//    public static final int PLAYER_MSGHDR_SIZE =
//    	PlayerDevAddr.PLAYER_DEVADDR_SIZE + 12;
    
	/**
	 * The message header is defined in player-2.1.3/libplayercore/player.h as follows:
	 * 
	 * typedef struct player_msghdr {
	 *   player_devaddr_t addr; // Device to which this message pertains
	 *   uint8_t type; // Message type; must be one of PLAYER_MSGTYPE_*
	 *   uint8_t subtype; // Message subtype; interface specific
	 *   double timestamp; // Time associated with message contents (seconds since epoch)
	 *   uint32_t seq; // For keeping track of associated messages.  Transport-specific.
	 *   uint32_t size; Size in bytes of the payload to follow
	 * } player_msghdr_t;
	 */
    public static final int PLAYERXDR_MSGHDR_SIZE =
    	PlayerDevAddr.PLAYERXDR_DEVADDR_SIZE + 24;     
    
	/* Device to which this message pertains */
	private PlayerDevAddr addr;
	/* Message type; must be one of PLAYER_MSGTYPE_* */
	private byte          type;
    /* Message subtype; interface specific */
	private byte          subtype;
    /* Time associated with message contents (seconds since epoch) */
	private double        timestamp;
    /* For keeping track of associated messages */
	private int           seq;
    /* Size in bytes of the payload to follow */
	private int           size;
    
    /**
     * 
     * @return Device to which this message pertains
     */
    public synchronized PlayerDevAddr getAddr () {
        return this.addr;
    }
    
    /**
     * 
     * @param newAddr Device to which this message pertains
     */
    public synchronized void setAddr (PlayerDevAddr newAddr) {
        this.addr = newAddr;
    }
    
    /**
     * 
     * @return Message type; must be one of PLAYER_MSGTYPE_*
     */
    public synchronized byte getType () {
        return this.type;
    }
    
    /**
     * 
     * @param newType Message type; must be one of PLAYER_MSGTYPE_*
     */
    public synchronized void setType (int newType) {
        this.type = (byte)newType;
    }
    
    /**
     * 
     * @return Message subtype; interface specific
     */
    public synchronized byte getSubtype () {
        return this.subtype;
    }
    
    /**
     * 
     * @param newSubtype Message subtype; interface specific
     */
    public synchronized void setSubtype (int newSubtype) {
        this.subtype = (byte)newSubtype;
    }

    /**
     * 
     * @return Time associated with message contents (seconds since epoch)
     */
    public synchronized double getTimestamp () {
        return this.timestamp;
    }
    
    /**
     * 
     * @param newTimestamp Time associated with message contents (seconds 
     * since epoch)
     */
    public synchronized void setTimestamp (double newTimestamp) {
        this.timestamp = newTimestamp;
    }

    /**
     * 
     * @return For keeping track of associated messages
     */
    public synchronized int getSeq () {
        return this.seq;
    }
    
    /**
     * 
     * @param newSeq For keeping track of associated messages
     */
    public synchronized void setSeq (int newSeq) {
        this.seq = newSeq;
    }

    /**
     * 
     * @return Size in bytes of the payload to follow
     */
    public synchronized int getSize () {
        return this.size;
    }
    
    /**
     * 
     * @param newSize Size in bytes of the payload to follow
     */
    public synchronized void setSize (int newSize) {
        this.size = newSize;
    }
    
	/**
	 * Returns a String representation of this class.
	 */
	public String toString() {
		StringBuffer result = new StringBuffer("PlayerMsgHdr:\n");
		result.append("\tAddress: " + addr + "\n");
		result.append("\tType: " + PlayerClientUtils.lookupNameType(getType()) + "/" + getType() + "\n");
		result.append("\tSubtype: " + PlayerClientUtils.lookupNameSubtype(getSubtype()) + "/" + getSubtype() + "\n");
		result.append("\tTimestamp: " + getTimestamp() + "\n");
		result.append("\tSequence #: " + getSeq() + "\n");
		result.append("\tSize: " + getSize());
		return result.toString();
//		return "(PlayerMsgHdr " + addr + " type=" + PlayerClientUtils.lookupNameType(getType()) 
//			+ "/" + getType() + " Subtype=" + getSubtype() + " Timestamp=" + getTimestamp()
//			+ ", Seq=" + getSeq() + ", size=" + getSize() + ")";
	}
}
