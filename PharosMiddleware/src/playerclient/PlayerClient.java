/*
 *  Player Java Client 2 - PlayerClient.java
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
 * $Id: PlayerClient.java,v 1.9 2006/03/10 19:39:48 veedee Exp $
 *
 */
package playerclient;

import java.net.Socket;
import java.net.SocketException;
import java.io.DataInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Date;
import java.util.Vector;

import playerclient.structures.PlayerConstants;
import playerclient.structures.PlayerDevAddr;
import playerclient.structures.PlayerMsgHdr;
import playerclient.structures.player.PlayerDeviceDevlist;
import playerclient.structures.player.PlayerDeviceDriverInfo;
import playerclient.xdr.*;

/**
 * The PlayerClient is the main Javaclient class. It contains methods for interacting with the 
 * player device. The player device represents the server itself, and is used in configuring 
 * the behavior of the server. There is only one such device (with index 0) and it is always 
 * open.
 * @author Radu Bogdan Rusu, Maxim Batalin, Esben Ostergaard
 * @version
 * <ul>
 *      <li>v2.0 - Player 2.0 supported
 * </ul>
 */
public class PlayerClient extends Thread implements PlayerConstants {

	/**
	 * The amount of time a thread that requests a device waits for the
	 * device to become ready before giving up.
	 */
	public static final int DEVICE_READY_WAIT = 100000;

	public static final boolean isDebugging = 
		(System.getProperty ("PlayerClient.debug") != null) ? true : false;

	//    private static final boolean stopOnEOFException = 
	//        (System.getProperty ("PlayerClient.stopOnEOFException") != null) ? false : true;

	// List of all available devices
	private PlayerDeviceDevlist    pddlist;
	private boolean                readyPDDList       = false;
	// Driver information for a particular device
	private PlayerDeviceDriverInfo pddi;
	private boolean                readyPDDI          = false;

	// Used for creating PlayerDevice type objects on requestDeviceAccess ()
	private PlayerDevice           newpd = null;
	private boolean                readyRequestDevice = false;
	private Object 					reqDevLock = new Object(); // a lock for synchronizing the thread that requests a device

	// Used for lookupName () and lookupCode ()
//	private PlayerClientUtils      pcu = new PlayerClientUtils ();

	protected Socket socket;
	protected BufferedOutputStream buffer;

	/**
	 * The input stream for the socket connected to the player server.
	 */
	protected DataInputStream is;
	/**
	 * The output stream for the socket connected to the player server. 
	 * It's buffered, so remember to flush()!
	 */
	protected DataOutputStream os;

	protected Vector<PlayerDevice> deviceList = new Vector<PlayerDevice>();

	private boolean receivedAuthentication = false;
	private boolean readyPortNumber        = false;

	XdrBufferEncodingStream xdrbuffEnc;

	private int portNumber;

	private long    millis;
	private int     nanos;
	// Timeout for packets
	//    private long    timeout = 100;
	private boolean isThreaded;

	// current data mode
	private int     datamode = PLAYER_DATAMODE_PUSH;

	/**
	 * The PlayerClient constructor. Once called, it will create a socket with the Player server
	 * running on host <b>servername</b> on port <b>portNumber</b>.
	 * 
	 * @param serverName url of the host running Player
	 * @param portNumber the port number of the Player server
	 */
	public PlayerClient (String serverName, int portNumber) {
		try {
			// Initialize network connection
			socket = new Socket (serverName, portNumber);
			log("Connected to Player Server " + serverName + ":" + portNumber);
			
			// Open the proper streams (I/O)
			is     = new DataInputStream (socket.getInputStream ());
			buffer = new BufferedOutputStream (socket.getOutputStream (), 128);
			os     = new DataOutputStream (new DataOutputStream (buffer));
			
			// Read the player version number (manual says version string is 32 chars)
			StringBuffer vStrBuff = new StringBuffer("Version: ");
			for (int i = 0; i < PLAYER_IDENT_STRLEN; i++) {
				char c = (char)is.readByte ();
				vStrBuff.append(c);
			}
			log(vStrBuff.toString());
			
			// Set the data mode to be push (have server constantly send data to us)
			log("Setting the data delivery mode to be PUSH...");
			requestDataDeliveryMode(PLAYER_DATAMODE_PUSH);
			
			log("Starting the thread that receives messages from the server...");
			runThreaded(-1,0);

			// Read and print (on screen) all available devices
//			requestDeviceList ();
//			readAll ();
//			if (isReadyPDDList()) {
//				PlayerDeviceDevlist list = getPDDList ();
//				System.err.println ("selected devices [" + serverName + ":" + 
//						portNumber + "]:");
//				for (int i = 0; i < list.getDeviceCount (); i++)
//				{
//					// Decode the host
//					//		        	XdrBufferEncodingStream xdr = new XdrBufferEncodingStream (4);
//					//		        	xdr.beginEncoding (null, 0);
//					//					xdr.xdrEncodeInt (list.getDevList ()[i].getHost ());
//					//		        	xdr.endEncoding ();
//					//		        	String host = xdr.getXdrData ()[3] + "." + xdr.getXdrData ()[2] +
//					//		        			"." + xdr.getXdrData ()[1] + "." + xdr.getXdrData ()[0];
//
//					//		        	xdr.close ();
//
//					// Print everything on screen
//					System.err.print (" " + //host + ":" +
//							+ list.getDevList ()[i].getRobot () +
//							":" + PlayerClientUtils.lookupName (list.getDevList ()[i].getInterf ()) +
//							":" + list.getDevList ()[i].getIndex ());
//
//					PlayerDevAddr pda = list.getDevList ()[i];
//					// Request additional device information
//					requestDriverInfo (pda);
//					readAll ();
//					if (isReadyPDDI ())
//						System.err.println (" (" + getPDDI ().getDriverName() + ")");
//				}
//			}
		} catch (IOException e) {
			throw new PlayerException ("[PlayerClient]: Error in PlayerClient init: " + e.toString (), e);
		}
	}
	
	private void log(String msg) {
		System.out.println("PlayerClient: " + msg);
	}
	
	private void logErr(String msg) {
		System.err.println("PlayerClient: " + msg);
	}

	/**
	 * The PlayerClient "destructor". Once called, it will close all the open streams/sockets 
	 * with the Player server.
	 */
	public void close () {
		try {
			for (int i = 0; i < deviceList.size (); i++) {
				PlayerDevice pd = (PlayerDevice)deviceList.get (i);
				requestDeviceAccess (pd.getDeviceAddress ().getInterf (), 
						pd.getDeviceAddress ().getIndex (), 
						PLAYER_CLOSE_MODE);
				readAll ();
			}

			// close all sockets
			os.close     ();
			buffer.close ();
			is.close     ();
			socket.close ();
			this.setNotThreaded ();
		} catch (IOException e) {
			throw new PlayerException 
			("[PlayerClient]: Error in PlayerClient stop: " + 
					e.toString (), e);
		}
	}

	/**
	 * Change the mode Javaclient runs to non-threaded.
	 */
	public void setNotThreaded() {
		isThreaded = false;
	}

	/**
	 * Start a threaded copy of Javaclient.
	 * 
	 * @param millis number of milliseconds to sleep between calls 
	 * @param nanos number of nanoseconds to sleep between calls
	 */
	private void runThreaded (long millis, int nanos) {
		if (isThreaded) {
			System.err.println ("[PlayerClient]: A second call for runThreaded, ignoring!");
			return;
		}
		this.millis = millis;
		this.nanos  = nanos;
		isThreaded  = true;
		this.start ();
	}

	/**
	 * The PlayerClient's thread periodically pulls data from the server if running in the "pull" data mode.
	 * and continuously reads in data.
	 */
	public void run () {
		while (isThreaded) {
			if (datamode == PLAYER_DATAMODE_PULL) {
				requestData ();
				while (read() != PLAYER_MSGTYPE_SYNCH && isThreaded);
			} else {
				read();
			}
			try {
				if (millis < 0)
					Thread.yield ();
				else
					if (nanos <= 0)
						Thread.sleep (millis);
					else
						Thread.sleep (millis, nanos);

			} catch (InterruptedException e) { 
				throw new PlayerException (e); 
			}
		}
	}

	/**
	 * Sends a Player message header filled with the given values.
	 * @param type type of message (DATA, CMD, REQ, RESP_ACK, SYNCH, RESP_NACK)
	 * @param subtype subtype of message
	 * @param size size of the payload to follow
	 */
	private void sendHeader (int type, int subtype, int size) {
		try {
			PlayerDevAddr devAddr = new PlayerDevAddr ();
			devAddr.setHost   (0);
			devAddr.setRobot  (0);
			devAddr.setInterf (PLAYER_PLAYER_CODE);
			devAddr.setIndex  (0);
			Date d = new Date ();
			double timestamp = d.getTime () / 1000;

			XdrBufferEncodingStream xdr = 
				new XdrBufferEncodingStream (PlayerMsgHdr.PLAYERXDR_MSGHDR_SIZE);
			xdr.beginEncoding (null, 0);
			/* see player.h / player_msghdr for additional explanations */
			/* The "host" on which the device resides */
			xdr.xdrEncodeInt    (devAddr.getHost   ());
			/* The "robot" or device collection in which the device resides */
			xdr.xdrEncodeInt    (devAddr.getRobot  ());
			/* The interface provided by the device; must be one of PLAYER_*_CODE */
			xdr.xdrEncodeShort  (devAddr.getInterf ());
			/* Which device of that interface */
			xdr.xdrEncodeShort  (devAddr.getIndex  ());
			/* Message type; must be one of PLAYER_MSGTYPE_* */
			xdr.xdrEncodeByte   ((byte)type);
			/* Message subtype; interface specific */
			xdr.xdrEncodeByte   ((byte)subtype);
			/* Time associated with message contents (seconds since epoch) */
			xdr.xdrEncodeDouble (timestamp);
			/* For keeping track of associated messages. */
			xdr.xdrEncodeInt    (0);
			/* Size in bytes of the payload to follow */
			xdr.xdrEncodeInt    (size);
			xdr.endEncoding ();
			os.write (xdr.getXdrData (), 0, PlayerMsgHdr.PLAYERXDR_MSGHDR_SIZE);
			xdr.close ();
		} catch (IOException e) {
			throw new PlayerException 
			("[PlayerClient]: Error sending header: " +
					e.toString (), e);
		} catch (OncRpcException e) {
			throw new PlayerException 
			("[PlayerClient]: Error XDR-encoding header: " +
					e.toString (), e);
		}
	}

	/**
	 * Reads the Player message header from the network.
	 * The header is defined in player-2.1.3/libplayercore/player.h, struct player_msghdr.
	 */
	private PlayerMsgHdr readHeader () {
		// Create two new empty structures to hold the header
		PlayerMsgHdr  header  = new PlayerMsgHdr  ();
		PlayerDevAddr devaddr = new PlayerDevAddr ();

		try {
			byte[] buffer = new byte[PlayerMsgHdr.PLAYERXDR_MSGHDR_SIZE];
			// Read the header from the network
			is.readFully (buffer, 0, PlayerMsgHdr.PLAYERXDR_MSGHDR_SIZE);
			
			/*System.out.println("header start");
			for(int i = 0; i<40; i++){
				System.out.println(buffer[i]);
			}
			System.out.println("header stop");*/

			// Begin decoding the XDR buffer
			XdrBufferDecodingStream xdr = new XdrBufferDecodingStream (buffer);
			xdr.beginDecoding ();

			// Decode the player_devaddr
			devaddr.setHost   (xdr.xdrDecodeInt   ());
			devaddr.setRobot  (xdr.xdrDecodeInt   ());
			devaddr.setInterf (xdr.xdrDecodeShort ());
			devaddr.setIndex  (xdr.xdrDecodeShort ());
			header.setAddr (devaddr);

			// Decode the rest of the player_msghdr
			header.setType      (xdr.xdrDecodeByte   ());
			header.setSubtype   (xdr.xdrDecodeByte   ());
			header.setTimestamp (xdr.xdrDecodeDouble ());
			header.setSeq       (xdr.xdrDecodeInt    ());
			header.setSize      (xdr.xdrDecodeInt    ());
			xdr.endDecoding ();
			xdr.close ();
		} catch (IOException e) {
			throw new PlayerException
			("[PlayerClient]: Error reading header: " 
					+ e.toString (), e);
		} catch (OncRpcException e) {
			throw new PlayerException 
			("[PlayerClient]: Error XDR-decoding header: " +
					e.toString (), e);
		}
		return header;
	}

	/**
	 * Request/reply: Get the list of available devices.
	 * <br><br>
	 * It's useful for applications such as viewer programs and test suites 
	 * that tailor behave differently depending on which devices are 
	 * available. To request the list, send a null PLAYER_PLAYER_REQ_DEVLIST.
	 */
	public void requestDeviceList () {
		try {
			XdrBufferEncodingStream xdr = new XdrBufferEncodingStream (8);
			xdr.beginEncoding  (null, 0);
			xdr.xdrEncodeInt   (0);		// devices_count
			xdr.xdrEncodeInt   (0);		// array count
			xdr.endEncoding ();
			int size = xdr.getXdrLength ();

			sendHeader ((int)PLAYER_MSGTYPE_REQ, PLAYER_PLAYER_REQ_DEVLIST, size);

			os.write (xdr.getXdrData (), 0, size);
			os.flush  ();
			xdr.close ();
		} catch (IOException e) {
			throw new PlayerException 
			("[PlayerClient]: Couldn't request device list: " + 
					e.toString (), e);
		} catch (OncRpcException e) {
			throw new PlayerException 
			("[PlayerClient]: Error XDR-encoding request DEVLIST: " +
					e.toString (), e);
		}
	}

	/**
	 * Request/reply: Get the driver name for a particular device.
	 * <br><br>
	 * To get a name, send a PLAYER_PLAYER_REQ_DRIVERINFO request that
	 * specifies the address of the desired device in the addr field. Set
	 * driver_name_count to 0 and leave driver_name empty. The response will 
	 * contain the driver name.
	 * @param device the device
	 */
	public void requestDriverInfo (PlayerDevAddr device) {
		try {
			// Encode the data into XDR format
			XdrBufferEncodingStream xdr = new XdrBufferEncodingStream 
			(PlayerDevAddr.PLAYERXDR_DEVADDR_SIZE + 8);
			xdr.beginEncoding  (null, 0);
			xdr.xdrEncodeInt   (device.getHost   ());
			xdr.xdrEncodeInt   (device.getRobot  ());
			xdr.xdrEncodeShort (device.getInterf ());
			xdr.xdrEncodeShort (device.getIndex  ());
			xdr.xdrEncodeInt   (0);		// driver_name_count
			xdr.xdrEncodeInt   (0);		// array count
			xdr.endEncoding ();
			int size = xdr.getXdrLength ();

			sendHeader ((int)PLAYER_MSGTYPE_REQ, PLAYER_PLAYER_REQ_DRIVERINFO, 
					size);
			os.write (xdr.getXdrData (), 0, size);
			os.flush  ();
			xdr.close ();
		} catch (IOException e) {
			throw new PlayerException 
			("[PlayerClient]: Couldn't request device info: " + 
					e.toString (), e);
		} catch (OncRpcException e) {
			throw new PlayerException 
			("[PlayerClient]: Error XDR-encoding request DRIVERINFO: " +
					e.toString (), e);
		}
	}

	/**
	 * Request/reply: (un)subscribe to a device
	 * <br><br>
	 * This is the most important request! Before interacting with a device, 
	 * the client must request appropriate access. Valid access modes are: <br>
	 * <ul>
	 * 		<li>PLAYER_OPEN_MODE : subscribe to the device. You will receive 
	 *          any data published by the device and you may send it commands 
	 *          and/or requests.
	 *      </li>
	 *      <li>PLAYER_CLOSE_MODE : unsubscribe from the device.
	 *      </li>
	 *      <li>PLAYER_ERROR_MODE : the requested access was not granted (only
	 *          appears in responses)
	 *      </li>
	 * </ul>
	 * <br><br>
	 * To request access, send a PLAYER_PLAYER_REQ_DEV request that specifies 
	 * the desired device address in the addr field and the desired access mode 
	 * in access. Set driver_name_count to 0 and leave driver_name empty.
	 * <br>
	 * The response will indicate the granted access in the access field and 
	 * the name of the underlying driver in the driver_name field. Note that 
	 * the granted access may not be the same as the requested access (e.g. if 
	 * initialization of the driver failed).
	 * 
	 * @param code the interface code
	 * @param index the index for the device
	 * @param access the requested access
	 * @return an object of PlayerDevice type
	 */
	private void requestDeviceAccess (int code, int index, int access) {
		if (isDebugging)
			log("Subscribing to " + PlayerClientUtils.lookupName ((short)code) + ":" + index);
		try {
			PlayerDevAddr devAddr = new PlayerDevAddr ();
			devAddr.setHost   (0);
			devAddr.setRobot  (0);
			devAddr.setInterf (code);
			devAddr.setIndex  (index);

			// Encode the data into XDR format
			XdrBufferEncodingStream xdr = new XdrBufferEncodingStream 
			(PlayerDevAddr.PLAYERXDR_DEVADDR_SIZE + 12);
			xdr.beginEncoding  (null, 0);
			xdr.xdrEncodeInt   (devAddr.getHost   ());
			xdr.xdrEncodeInt   (devAddr.getRobot  ());
			xdr.xdrEncodeShort (devAddr.getInterf ());
			xdr.xdrEncodeShort (devAddr.getIndex  ());
			xdr.xdrEncodeByte  ((byte)access);	// requested access
			xdr.xdrEncodeInt   (0);		        // driver_name_count
			xdr.xdrEncodeInt   (0);		        // array count
			xdr.endEncoding ();
			int size = xdr.getXdrLength ();

			sendHeader ((int)PLAYER_MSGTYPE_REQ, PLAYER_PLAYER_REQ_DEV, size);
			os.write (xdr.getXdrData (), 0, size);
			os.flush  ();
			xdr.close ();

			// Commented this out because the PlayerClient thread will read it in
			// Check if a negative acknowledgment was received.
//			int result;
//			while ((result = read ()) != PLAYER_MSGTYPE_RESP_ACK) {
//				if (result == PLAYER_MSGTYPE_RESP_NACK)
//					throw new PlayerException 
//						("[PlayerClient] Negative acknowledgment received " +
//							"for " + PlayerClientUtils.lookupName ((short)code) + ":" + 
//							index);
//			}
		} catch (IOException e) {
			throw new PlayerException 
			("[PlayerClient]: Couldn't request device access: " + 
					e.toString (), e);
		} catch (OncRpcException e) {
			throw new PlayerException 
			("[PlayerClient]: Error XDR-encoding request DEV: " +
					e.toString (), e);
		}
	}	

	/**
	 * Configuration request: Get data.
	 * <br><br>
	 * When the server is in a PLAYER_DATAMODE_PULL data delivery mode, 
	 * the client can request a single round of data by sending a 
	 * PLAYER_PLAYER_REQ_DATA request. 
	 */
	public void requestData () {
		try {
			sendHeader ((int)PLAYER_MSGTYPE_REQ, PLAYER_PLAYER_REQ_DATA, 0);
			os.flush  ();
		} catch (IOException e) {
			throw new PlayerException 
			("[PlayerClient]: Couldn't request data: " + 
					e.toString (), e);
		}
	}


	/**
	 * Configuration request: Change data delivery mode.
	 * <br><br>
	 * The Player server supports 2 data modes, PUSH and PULL. 
	 * To switch to a different mode send a request with the format given 
	 * below. The server's reply will be a zero-length acknowledgment.
	 * 
	 * @param mode the requested mode
	 */
	private void requestDataDeliveryMode (int mode) {
		this.datamode = mode;
		try {
			// Encode the data into XDR format
			XdrBufferEncodingStream xdr = new XdrBufferEncodingStream (4);
			xdr.beginEncoding  (null, 0);
			xdr.xdrEncodeInt   (mode);	// the requested mode
			xdr.endEncoding ();
			int size = xdr.getXdrLength ();

			sendHeader ((int)PLAYER_MSGTYPE_REQ, PLAYER_PLAYER_REQ_DATAMODE, size);
			os.write (xdr.getXdrData (), 0, size);
			os.flush  ();
			xdr.close ();
		} catch (IOException e) {
			throw new PlayerException 
			("[PlayerClient]: Couldn't request change of datamode: " + 
					e.toString (), e);
		} catch (OncRpcException e) {
			throw new PlayerException 
			("[PlayerClient]: Error XDR-encoding request DATAMODE: " +
					e.toString (), e);
		}
	}

	/**
	 * [NEEDS TESTING, OBSOLETE? - NOT IMPLEMENTED IN PLAYER2]
	 * <br><br>
	 * Configuration request: Authentication.
	 * <br><br>
	 * If server authentication has been enabled (by providing '-key <key>' 
	 * on the command-line; see Command line options); then each client must 
	 * authenticate itself before otherwise interacting with the server. To 
	 * authenticate, send a request with this format.
	 * <br><br>
	 * If the key matches the server's key then the client is authenticated, 
	 * the server will reply with a zero-length acknowledgement, and the 
	 * client can continue with other operations. If the key does not match, 
	 * or if the client attempts any other server interactions before 
	 * authenticating, then the connection will be closed immediately. It is 
	 * only necessary to authenticate each client once.
	 * <br><br>
	 * Note that this support for authentication is NOT a security mechanism. 
	 * The keys are always in plain text, both in memory and when transmitted 
	 * over the network; further, since the key is given on the command-line, 
	 * there is a very good chance that you can find it in plain text in the 
	 * process table (in Linux try 'ps -ax | grep player'). Thus you should not 
	 * use an important password as your key, nor should you rely on Player 
	 * authentication to prevent bad guys from driving your robots (use a 
	 * firewall instead). Rather, authentication was introduced into Player to 
	 * prevent accidentally connecting one's client program to someone else's 
	 * robot. This kind of accident occurs primarily when Stage is running in a 
	 * multi-user environment. In this case it is very likely that there is a 
	 * Player server listening on port 6665, and clients will generally connect 
	 * to that port by default, unless a specific option is given.
	 * <br><br>
	 * This mechanism was never really used, and may be removed. 
	 * @param key the authentication key
	 */
	public void requestAuthentication (byte[] key) {
		try {
			if (key.length > PLAYER_KEYLEN) 
				throw new PlayerException ("[PlayerClient]: Supplied " +
						"authentication key is " + key.length + 
						" but should be <= " + PLAYER_KEYLEN + 
				" bytes");

			// Encode the data into XDR format
			XdrBufferEncodingStream xdr = new 
			XdrBufferEncodingStream (8);
			xdr.beginEncoding  (null, 0);
			xdr.xdrEncodeInt   (key.length);	    // length of key
			xdr.xdrEncodeByte  ((byte)key.length);	// length of key
			xdr.endEncoding ();
			int size = xdr.getXdrLength ();

			sendHeader ((int)PLAYER_MSGTYPE_REQ, PLAYER_PLAYER_REQ_AUTH, size);
			os.write (xdr.getXdrData (), 0, size);
			xdr.close ();

			int leftOvers = 0;
			// Take care of the residual zero bytes
			if ((key.length % 4) != 0)
				leftOvers = 4 - (key.length % 4);
			byte[] buf = new byte[leftOvers];

			os.write (key);
			os.write (buf, 0, leftOvers);
			os.flush ();
		} catch (IOException e) {
			throw new PlayerException 
			("[PlayerClient]: Couldn't request authentication: " +
					e.toString (), e);
		} catch (OncRpcException e) {
			throw new PlayerException 
			("[PlayerClient]: Error XDR-encoding request AUTH: " +
					e.toString (), e);
		}
	}

	/**
	 * [NEEDS TESTING, returns NACK - NOT FINISHED IN PLAYER2]
	 * (warning : player interface discarding message of unsupported subtype 8)
	 * <br><br>
	 * Use nameservice to get the corresponding port for a robot name 
	 * (only with Stage).
	 * @param name the robot name
	 */
	public void requestNameService (char[] name) {
		try {
			if (name.length > PLAYER_MAX_DRIVER_STRING_LEN) 
				throw new PlayerException ("[PlayerClient]: Supplied " +
						"robot name " + name.length + 
						" but should be <= " + 
						PLAYER_MAX_DRIVER_STRING_LEN + " bytes");

			// Encode the data into XDR format
			XdrBufferEncodingStream xdr = new XdrBufferEncodingStream (8);
			xdr.beginEncoding  (null, 0);
			xdr.xdrEncodeInt   (name.length);	    // length of name
			xdr.xdrEncodeByte  ((byte)name.length);	// length of name
			xdr.endEncoding ();
			int size = xdr.getXdrLength ();

			sendHeader ((int)PLAYER_MSGTYPE_REQ, PLAYER_PLAYER_REQ_NAMESERVICE, 
					size);
			os.write (xdr.getXdrData (), 0, size);
			xdr.close ();

			int leftOvers = 0;
			// Take care of the residual zero bytes
			if ((name.length % 4) != 0)
				leftOvers = 4 - (name.length % 4);
			byte[] buf = new byte[leftOvers];

			os.write (new String (name).getBytes ());
			os.write (buf, 0, leftOvers);
			os.flush ();
		} catch (IOException e) {
			throw new PlayerException 
			("[PlayerClient]: Couldn't request name service: " +
					e.toString (), e);
		} catch (OncRpcException e) {
			throw new PlayerException 
			("[PlayerClient]: Error XDR-encoding request NAMESERVICE: " +
					e.toString (), e);
		}
	}


	/**
	 * [NOT IMPLEMENTED IN PLAYER2 YET?]
	 */
	public void requestIdent () {
		try {
			sendHeader ((int)PLAYER_MSGTYPE_REQ, PLAYER_PLAYER_REQ_IDENT, 0);
			os.flush  ();
		} catch (IOException e) {
			throw new PlayerException 
			("[PlayerClient]: Couldn't request ident: " +
					e.toString (), e);
		}
	}


	/**
	 * Configuration request: Add client queue replace rule.
	 * <br><br>
	 * Allows the client to add a replace rule to their server queue. Replace 
	 * rules define which messages will be replaced when new data arrives. 
	 * If you are not updating frequently from the server then the use of 
	 * replace rules for data packets will stop any queue overflow messages.
	 * <br><br>
	 * Each field in the request corresponds to the equivalent field in the 
	 * message header (use -1 for a "don't care" value).
	 * 
	 * @param interf interface to set replace rule for (-1 for wildcard)
	 * @param index index to set replace rule for (-1 for wildcard)
	 * @param type message type to set replace rule for (-1 for wildcard), 
	 * i.e. PLAYER_MSGTYPE_DATA
	 * @param subtype message subtype to set replace rule for (-1 for wildcard)
	 * @param replace should we replace these messages
	 */
	public void requestAddReplaceRule (int interf, int index, int type, 
			int subtype, int replace) {
		try {
			// Encode the data into XDR format
			XdrBufferEncodingStream xdr = new XdrBufferEncodingStream (20);
			xdr.beginEncoding  (null, 0);
			xdr.xdrEncodeInt   (interf);
			xdr.xdrEncodeInt   (index);
			xdr.xdrEncodeInt   (type);
			xdr.xdrEncodeInt   (subtype);
			xdr.xdrEncodeInt   (replace);
			xdr.endEncoding ();
			int size = xdr.getXdrLength ();

			sendHeader ((int)PLAYER_MSGTYPE_REQ, 
					PLAYER_PLAYER_REQ_ADD_REPLACE_RULE, size);
			os.write (xdr.getXdrData (), 0, size);
			os.flush  ();
			xdr.close ();
		} catch (IOException e) {
			throw new PlayerException 
			("[PlayerClient]: Couldn't request ADD_REPLACE_RULE: " +
					e.toString (), e);
		} catch (OncRpcException e) {
			throw new PlayerException 
			("[PlayerClient]: Error XDR-encoding request ADD_REPLACE_RULE: "
					+ e.toString (), e);
		}
	}


	/**
	 * Read the Player server replies.
	 * 
	 * @return the message type code
	 */
	private short read () {
		PlayerMsgHdr header;
		
		try {
			if (isDebugging) log("read(): Waiting for next header to arrive... ");
			// Read the Player header
			header = readHeader();
			
			//System.out.println(header.toString());

			if (isDebugging) log("read(): " + header);

			// verify the message type code - see "Message Formats" from the Player manual
			switch (header.getType()) {
			// Data message
			case PLAYER_MSGTYPE_DATA: {
				if (header.getAddr ().getInterf () == 0) break;
				
//				if (isDebugging)
//					System.err.println ("[PlayerClient][Debug]: Data for " 
//							+ PlayerClientUtils.lookupName (header.getAddr ().getInterf ()) 
//							+ ":" + header.getAddr ().getIndex ());
				
				if (header.getAddr ().getInterf () != PLAYER_PLAYER_CODE)
					readDataDevice (header);
				
				break;
			}

			// Command message - should never receive it
			case PLAYER_MSGTYPE_CMD: {
				if (isDebugging)
					System.err.println ("[PlayerClient][Debug]: Error! Client should never receive a CMD !");
				break;
			}

			// Request message - should never receive it
			case PLAYER_MSGTYPE_REQ: {
				if (isDebugging)
					System.err.println ("[PlayerClient][Debug]: Error! Client should never receive a REQ !");
				break;
			}

			// Acknowledgment response message
			case PLAYER_MSGTYPE_RESP_ACK: {
				if (header.getAddr().getInterf() != PLAYER_PLAYER_CODE) {
					handleRequestsDevice (header);
				} else {
					handlePlayerResponses(header);
				}
				break;
			}

			// Synchronization message
			case PLAYER_MSGTYPE_SYNCH: { 
				if (isDebugging)
					System.err.println ("[PlayerClient][Debug]: Synchronization received");
				break;
			}

			// Negative acknowledgment response message
			case PLAYER_MSGTYPE_RESP_NACK: {
				if (isDebugging) 
					System.err.println ("[PlayerClient][Debug]: Negative acknowledgement received");
				break;
			}

			default: {
				if ((isDebugging) && (header.getType () != 0))
					System.err.println ("[PlayerClient][Debug]: Unknown message type " + header.getType () + " received in read()");
				break;
			}
			}
		} catch (EOFException e) {
			throw new PlayerException ("[PlayerClient]: java.io.EOFException : Is the Player server still running?");
		} catch (SocketException e) {
			throw new PlayerException ("[PlayerClient]: java.Socket.EOFException : Is the Player server still running?");
		} catch (IOException e) {
			throw new PlayerException ("[PlayerClient]: Read error: " + e.toString (), e);
		} catch (OncRpcException e) {
			throw new PlayerException  ("[PlayerClient]: Error XDR-decoding data : " + e.toString(), e);
		}
		return header.getType();
	}
	
	private void handlePlayerResponses(PlayerMsgHdr header) throws IOException, OncRpcException {
		byte[] buffer;
		XdrBufferDecodingStream xdr;
		
		// Handle acknowledgment response messages
		switch (header.getSubtype ()) {
		case 0: break;

		// get the list of available devices
		case PLAYER_PLAYER_REQ_DEVLIST: {
			pddlist = new PlayerDeviceDevlist ();

			// Temporary buffer for reading devices_count
			buffer = new byte[8];
			// Read devices_count and array count (4+4)
			is.readFully (buffer, 0, 8);

			// Begin decoding the XDR buffer
			xdr = new XdrBufferDecodingStream (buffer);
			xdr.beginDecoding ();
			// The number of devices
			pddlist.setDeviceCount (xdr.xdrDecodeInt ());
			xdr.endDecoding   ();
			xdr.close ();

			PlayerDevAddr[] devAddrList = new PlayerDevAddr[pddlist.getDeviceCount ()];

			// Read the list of available devices
			for (int i = 0; i < pddlist.getDeviceCount (); i++) {
				buffer = new byte[PlayerDevAddr.PLAYERXDR_DEVADDR_SIZE];
				//while (is.available() == 0);
				is.readFully (buffer, 0, PlayerDevAddr.PLAYERXDR_DEVADDR_SIZE);
				devAddrList[i] = decodeDevAddr (buffer);
			}
			pddlist.setDevList (devAddrList);
			readyPDDList = true;
			break;
		}

		// get the driver name for a particular device.
		case PLAYER_PLAYER_REQ_DRIVERINFO: {
			pddi = new PlayerDeviceDriverInfo ();

			// Read the device identifier
			buffer = new byte[PlayerDevAddr.PLAYERXDR_DEVADDR_SIZE];
			is.readFully (buffer, 0, PlayerDevAddr.PLAYERXDR_DEVADDR_SIZE);
			PlayerDevAddr devAddr = decodeDevAddr (buffer);
			pddi.setAddr (devAddr);                			

			// Temporary buffer for reading driver_name_count
			buffer = new byte[8];
			// Read devices_count and array count (4+4)
			is.readFully (buffer, 0, 8);

			// Begin decoding the XDR buffer
			xdr = new XdrBufferDecodingStream (buffer);
			xdr.beginDecoding ();
			// Length of the driver name
			pddi.setDriverNameCount (xdr.xdrDecodeInt ());
			xdr.endDecoding   ();
			xdr.close ();

			// Read the driver name
			buffer = new byte[pddi.getDriverNameCount ()];
			is.readFully (buffer, 0, (pddi.getDriverNameCount ()));

			pddi.setDriverName (new String (buffer));

			// Take care of the residual zero bytes
			if ((pddi.getDriverNameCount () % 4) != 0)
				is.readFully (buffer, 0, 4 - (pddi.getDriverNameCount () % 4));

			readyPDDI = true;
			break;
		}

		// (un)subscribe to device
		case PLAYER_PLAYER_REQ_DEV: { // subtype 3
			
			// We are now decoding the data defined by player_device_req_t in 
			// libplayercore/player_interface.h as:
			// 
			// typedef struct player_device_req {
			//  player_devaddr_t addr; 								// Address of the device
			//  uint8_t access; 									// The requested / granted access
			//  uint32_t driver_name_count; 						// Length of driver name
			//  char driver_name[PLAYER_MAX_DRIVER_STRING_LEN]; 	// The name of the underlying driver
			// } player_device_req_t;
			//
			// The sender is performing the encoding in libplayertcp/remote_driver.cc, method 
			// TCPRemoteDriver::SubscribeRemote
			
			// First read in the device address (first field in struct player_device_req)
			buffer = new byte[PlayerDevAddr.PLAYERXDR_DEVADDR_SIZE];
			is.readFully(buffer, 0, PlayerDevAddr.PLAYERXDR_DEVADDR_SIZE);
			PlayerDevAddr devAddr = decodeDevAddr(buffer);
			
			// Next read in the next two fields (access and driver_name_count)
			//
			// QUESTION: why is this 12 bytes?  access is a byte and is encoded using
			// four bytes, driver_name_count is a uint32_t and is also encoded as four bytes.
			// Thus, the buffer below should be 8 bytes (4 bytes for the encoded "byte access" 
			// and 4 bytes for the "uint32_t driver_name_count" variables)
			// 
			// HOWEVER, it seems like there is a new line (\n) character after the uint32_t...
			
			// Read the granted access and driver name count
			buffer = new byte[12];
			// Read access, driver_name_count, array_count
			is.readFully (buffer, 0, 12);

			// Begin decoding the XDR buffer
			xdr = new XdrBufferDecodingStream (buffer);
			xdr.beginDecoding ();
			byte access = xdr.xdrDecodeByte();
			int driverNameCount = xdr.xdrDecodeInt();
			xdr.endDecoding();
			xdr.close();
			
			/*log("handlePlayerResponses():\n"
					+ "\tMessage type: PLAYER_MSGTYPE_RESP_ACK\n"
					+ "\tMessage subtype: PLAYER_PLAYER_REQ_DEV\n"
					+ "\tDevice Address: " + devAddr + "\n"
					+ "\tAccess: " + access + "\n"
					+ "\tdriverNameCount: " + driverNameCount);*/
			
			// The maximum length of the driver name is 64 bytes, as defined by
			// PLAYER_MAX_DRIVER_STRING_LEN in libplayercore/player.h and in
			// javaclient2.structures.PlayerConstants.java
			if (driverNameCount > PLAYER_MAX_DRIVER_STRING_LEN) {
				logErr("handlePlayerResposnes(): driverNameCount > PLAYER_MAX_DRIVER_STRING_LEN (" + driverNameCount + ")");
				System.exit(1); // fatal error
			}
			
			// Read the driver name
			buffer = new byte[driverNameCount];
			is.readFully (buffer, 0, driverNameCount);

			if (access == PLAYER_ERROR_MODE)
				throw new PlayerException ("[PlayerClient]: " +
						"Error subscribing to : " + 
						PlayerClientUtils.lookupName (devAddr.getInterf ()) + 
						":" + devAddr.getIndex ());

			if (isDebugging)
				log("handlePlayerResponses():\n" +
						"Got response: " + 
						PlayerClientUtils.lookupName (devAddr.getInterf ()) + 
						":" + devAddr.getIndex () +
						"(" + new String (buffer) + ")" +
						" size of payload: " + header.getSize ());

			// Create an instance of the actual object interface
			requestSatisfy (devAddr, access, new String (buffer));

			// Take care of the residual zero bytes
			if ((driverNameCount % 4) != 0)
				is.readFully (buffer, 0, 4 - (driverNameCount % 4));

			readyRequestDevice = true;

			// Notify any thread that is waiting for this to become ready
			synchronized(reqDevLock) {
				reqDevLock.notifyAll();
			}
			break;
		}

		// request data
		case PLAYER_PLAYER_REQ_DATA: {
			break;
		}

		// change data delivery mode
		case PLAYER_PLAYER_REQ_DATAMODE: { // subclass 5 (zero-length body)
			break;
		}

		// authentication
		// [OBSOLETE?]
		case PLAYER_PLAYER_REQ_AUTH: {
			receivedAuthentication = true;
			break;
		}

		case PLAYER_PLAYER_REQ_NAMESERVICE: {
			//                            portNumber = is.readShort ();
			readyPortNumber = true;
			break;
		}

		// [NOTIMPLEMENTED?]
		case PLAYER_PLAYER_REQ_IDENT: {
			break;
		}

		case PLAYER_PLAYER_REQ_ADD_REPLACE_RULE: {
			if (isDebugging)
				System.err.println ("[PlayerClient][Debug]: " +
						"PLAYER_PLAYER_REQ_ADD_REPLACE_RULE " +
				"was succesful!");
			break;
		}
		default: {
			System.err.println ("[PlayerClient]: Unknown message subtype received in read ()");
		}
		}
	
	}

	/**
	 * XDR-Decode the PlayerDevAddr structure.
	 * @param buffer an array of bytes containing raw read data 
	 * @return an object of type PlayerDevAddr containing the decoded structure
	 */
	private PlayerDevAddr decodeDevAddr (byte[] buffer) {
		PlayerDevAddr devAddr = new PlayerDevAddr ();

		try {
			XdrBufferDecodingStream xdr = new XdrBufferDecodingStream (buffer);
			xdr.beginDecoding ();
			devAddr.setHost   (xdr.xdrDecodeInt   ());
			devAddr.setRobot  (xdr.xdrDecodeInt   ());
			devAddr.setInterf (xdr.xdrDecodeShort ());
			devAddr.setIndex  (xdr.xdrDecodeShort ());
			xdr.endDecoding   ();
			xdr.close ();
		} catch (IOException e) {
			throw new PlayerException 
			("[PlayerClient]: Error reading PlayerDevAddr data: " + 
					e.toString (), e);
		} catch (OncRpcException e) {
			throw new PlayerException 
			("[PlayerClient]: Error XDR-decoding PlayerDevAddr data: " + 
					e.toString (), e);
		}
		return devAddr;
	}

	/**
	 * Read the Player server replies in non-threaded mode.
	 */
	public int readAll () {
		if (isThreaded) return 0;
		if (this.datamode == PLAYER_DATAMODE_PULL) {
			requestData ();
			while (read () != PLAYER_MSGTYPE_SYNCH);
			return PLAYER_MSGTYPE_SYNCH;
		}

		int type = 0;
		//    	try {
			/*	        long start = System.currentTimeMillis ();
	    	while (is.available () == 0) {
	    		if (System.currentTimeMillis () > (start + timeout)) break;
	    	}
	    	while (is.available () != 0) {*/
		type = read ();
		//	    		if (type == PLAYER_MSGTYPE_SYNCH) break;
		//	    	}
		/*    	} catch (IOException e) {
	    	throw new PlayerException 
			("[PlayerClient]: Error reading data: " + 
					e.toString (), e);
    	}*/
		return type;
	}
	
	/**
	 * Searches through the deviceList and tries to find the device
	 * with the specified address.
	 * 
	 * @param devAddr The address of the device
	 * @return The device with the specified address, or null if no such device
	 * is found
	 */
	private PlayerDevice findDevice(PlayerDevAddr devAddr) {
		PlayerDevice result = null;
		
		for (int i = 0; i < deviceList.size (); i++) {
			PlayerDevice currDevice = deviceList.get (i);
			if (currDevice.getDeviceAddress().matches(devAddr)) {
				result = currDevice;
				break; // no need to keep searching since each address should be unique
			}
		}
		return result;
	}

	/**
	 * Find the device that is the destination for a data message and
	 * allow it to read in the data.
	 * 
	 * @param header The header of the incoming message.
	 */
	private void readDataDevice(PlayerMsgHdr header) {
		PlayerDevice device = findDevice(header.getAddr());
		if (device != null) {
			device.readData (header);
		} else {
			logErr("readDataDevice(): Unable to find device with address: " + header.getAddr());
			System.exit(1); // fatal error
		}
	}

	/**
	 * Find the device that is the destination for a request message and
	 * allow it to read in the data.
	 * 
	 * @param header The header of the incoming message.
	 */
	private void handleRequestsDevice(PlayerMsgHdr header) {
		PlayerDevice device = findDevice(header.getAddr());
		if (device != null) {
			device.handleResponse(header);
		} else {
			logErr("handleRequestsDevice(): Unable to find device with address: " + header.getAddr());
			System.exit(1); // fatal error
		}
	}

	/**
	 * Handle several Player replies. If PLAYER_MSGTYPE_RESP_ACK after a requestDeviceAccess (), 
	 * creates a newpd object of a PlayerDevice type.
	 * 
	 * @return the message type that Player replied with.
	 */
	private short requestSatisfy (PlayerDevAddr devAddr, byte access, String driverName) {
		if (access == PLAYER_CLOSE_MODE)
			return 0;
		switch (devAddr.getInterf ()) {
		case PLAYER_NULL_CODE: {                // /dev/null analogue
			break;
		}
		case PLAYER_PLAYER_CODE: {              // the server itself
			break;
		}
		case PLAYER_POWER_CODE: {               // power subsystem
			newpd = new PowerInterface (this);
			break;
		}
		case PLAYER_GRIPPER_CODE: {             // gripper
			newpd = new GripperInterface (this);
			break;
		}
		case PLAYER_POSITION2D_CODE: {          // device that moves
			newpd = new Position2DInterface (this);
			break;
		}
		case PLAYER_SONAR_CODE: {               // fixed range-finder
			newpd = new SonarInterface (this);
			break;
		}
		case PLAYER_LASER_CODE: {               // scanning range-finder
			newpd = new LaserInterface (this);
			break;
		}
		case PLAYER_BLOBFINDER_CODE: {          // visual blobfinder
			newpd = new BlobfinderInterface (this);
			break;
		}
		case PLAYER_PTZ_CODE: {                 // pan-tilt-zoom unit
			newpd = new PtzInterface (this);
			break;
		}
		case PLAYER_FIDUCIAL_CODE: {            // fiducial detector
			newpd = new FiducialInterface (this);
			break;
		}
		case PLAYER_SPEECH_CODE: {              // speech I/O
			newpd = new SpeechInterface (this);
			break;
		}
		case PLAYER_GPS_CODE: {                 // GPS unit
			newpd = new GPSInterface (this);
			break;
		}
		case PLAYER_BUMPER_CODE: {              // bumper array
			newpd = new BumperInterface (this);
			break;
		}
		case PLAYER_DIO_CODE: {                 // digital I/O
			newpd = new DIOInterface (this);
			break;
		}
		case PLAYER_AIO_CODE: {                 // analog I/O
			newpd = new AIOInterface (this);
			break;
		}
		case PLAYER_IR_CODE: {                  // IR array
			newpd = new IRInterface (this);
			break;
		}
		case PLAYER_WIFI_CODE: {                // wifi card status
			newpd = new WiFiInterface (this);
			break;
		}
		case PLAYER_WAVEFORM_CODE: {            // fetch raw waveforms
			newpd = new WaveformInterface (this);
			break;
		}
		case PLAYER_LOCALIZE_CODE: {            // localization
			newpd = new LocalizeInterface (this);
			break;
		}
		case PLAYER_MCOM_CODE: {                // multicoms
			newpd = new MComInterface (this);
			break;
		}
		case PLAYER_SOUND_CODE: {               // sound file playback
			newpd = new SoundInterface (this);
			break;
		}
		case PLAYER_AUDIODSP_CODE: {            // audio DSP I/O
			newpd = new AudioDSPInterface (this);
			break;
		}
		case PLAYER_AUDIOMIXER_CODE: {          // audio I/O
			newpd = new AudioMixerInterface (this);
			break;
		}
		case PLAYER_POSITION3D_CODE: {          // 3-D position
			newpd = new Position3DInterface (this);
			break;
		}
		case PLAYER_SIMULATION_CODE: {          // simulators
			newpd = new SimulationInterface (this);
			break;
		}
		case PLAYER_SERVICE_ADV_CODE: {         // LAN advertisement
			// Player support obsolete?
			break;
		}
		case PLAYER_BLINKENLIGHT_CODE: {        // blinking lights
			newpd = new BlinkenlightInterface (this);
			break;
		}
		case PLAYER_CAMERA_CODE: {              // camera device (gazebo)
			newpd = new CameraInterface (this);
			break;
		}
		case PLAYER_MAP_CODE: {                 // get a map
			newpd = new MapInterface (this);
			break;
		}
		case PLAYER_PLANNER_CODE: {             // 2D motion planner
			newpd = new PlannerInterface (this);
			break;
		}
		case PLAYER_LOG_CODE: {                 // log R/W control
			newpd = new LogInterface (this);
			break;
		}
		case PLAYER_JOYSTICK_CODE: {            // joystick
			newpd = new JoystickInterface (this);
			break;
		}
		case PLAYER_SPEECH_RECOGNITION_CODE: {  // speech recognition I/O
			newpd = new SpeechRecognitionInterface (this);
			break;
		}
		case PLAYER_OPAQUE_CODE: {              // plugin interface
			newpd = new OpaqueInterface(this);
			break;
		}
		case PLAYER_POSITION1D_CODE: {          // 1-D position
			newpd = new Position1DInterface (this);
			break;
		}
		case PLAYER_ACTARRAY_CODE: {            // actuator array interface
			newpd = new ActarrayInterface (this);
			break;
		}
		case PLAYER_LIMB_CODE: {                // limb interface
			newpd = new LimbInterface (this);
			break;
		}
		case PLAYER_GRAPHICS2D_CODE: {          // graphics2d interface
			break;
		}
		case PLAYER_RFID_CODE: {                // rfid interface
			newpd = new RFIDInterface (this);
			break;
		}
		default: {
			System.err.println ("[PlayerClient]: Unsupported device error! - " + devAddr.getInterf ());
			newpd = null;
			break;
		}
		}
		if (newpd != null) {
			newpd.setDeviceAddress    (devAddr);
			newpd.setDeviceAccess     (access);
			newpd.setDeviceDriverName (driverName);
			// add the device to the list
			deviceList.add (newpd);
		}
		return 0;
	}

	/*    /**
	 * Get the current read timeout in milliseconds.
	 * @return the current read timeout in milliseconds
     /
    public long getReadTimeout () {
    	return this.timeout;
    }

    /**
	 * Set the read timeout in milliseconds.
	 * @param newTimeout new timeout in milliseconds
     /
    public void setReadTimeout (long newTimeout) {
    	this.timeout = newTimeout;
    }*/

	/**
	 * Check to see if the Player server replied with a 
	 * PLAYER_PLAYER_REQ_DEVLIST successfully.
	 * @return true if the PLAYER_PLAYER_REQ_DEVLIST occured, false otherwise
	 * @see #getPDDList()
	 */
	public boolean isReadyPDDList () {
		if (readyPDDList) {
			readyPDDList = false;
			return true;
		}
		return false;
	}


	/**
	 * Check to see if the Player server replied with a 
	 * PLAYER_PLAYER_DRIVERINFO_REQ successfully.
	 * @return true if the PLAYER_PLAYER_DRIVERINFO_REQ occured, false 
	 * otherwise
	 * @see #getPDDI()
	 */
	public boolean isReadyPDDI () {
		if (readyPDDI) {
			readyPDDI = false;
			return true;
		}
		return false;
	}


	/**
	 * Get the list of available devices after a 
	 * PLAYER_PLAYER_REQ_DEVLIST request.
	 * @return an object of PlayerDeviceDevlist type
	 * @see #isReadyPDDList()
	 */
	public PlayerDeviceDevlist   getPDDList () { return pddlist; }

	/**
	 * Get the driver name for a particular device after a 
	 * PLAYER_PLAYER_DRIVERINFO_REQ request.
	 * @return an object of PlayerDeviceDriverInfo type
	 * @see #isReadyPDDI()
	 */
	public PlayerDeviceDriverInfo getPDDI () { return pddi; }


	/**
	 * Get the port number for the specified robot after a 
	 * PLAYER_PLAYER_NAMESERVICE_REQ request.
	 * @return the port number the specified robot runs on
	 * @see #requestNameService(char[])
	 * @see #isReadyPortNumber()
	 */
	public int getPortNumber () { return portNumber; }


	/**
	 * Check to see if the client has authenticated successfully.
	 * @return true if client has authenticated, false otherwise
	 */
	public boolean isAuthenticated () { 
		if (receivedAuthentication) {
			receivedAuthentication = false;
			return true;
		}
		return false;
	}
	/**
	 * Check to see if the port number has been identified. 
	 * @return true if the port is ready to be read, false otherwise
	 * @see #getPortNumber()
	 */
	public boolean isReadyPortNumber () {
		if (readyPortNumber) {
			readyPortNumber = false;
			return true;
		}
		return false; 
	}

	/**
	 * Check to see if the Player server replied with a 
	 * PLAYER_PLAYER_REQ_DEV successfully.
	 * @return true if the PLAYER_PLAYER_REQ_DEV occurred, false 
	 * otherwise
	 */
	public boolean isReadyRequestDevice () {

		synchronized(reqDevLock) {
			if (!readyRequestDevice) {
				// If the device is not yet ready, wait up to a second for it
				// to become ready...

				if (isDebugging)
					log("isReadyRequestDevice(): Requested device not ready, waiting for up to " + 
							DEVICE_READY_WAIT + "ms...");

				try {
					reqDevLock.wait(DEVICE_READY_WAIT);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		
		if (readyRequestDevice) {
			log("isReadyRequestDevice(): Requested device is ready!");
			readyRequestDevice = false;
			return true;
		} else {
			log("isReadyRequestDevice(): Requested device is not ready!");
			return false;
		}
		
	}

	/**
	 * Request a Power device. 
	 * @param index the device index
	 * @param access access mode
	 * @return a Power device if successful, null otherwise
	 */
	public PowerInterface requestInterfacePower (int index, int access) {
		requestDeviceAccess (PLAYER_POWER_CODE, index, access);
		if (isReadyRequestDevice ())
			return (PowerInterface)newpd;
		else
			return null;
	}

	/**
	 * Request a Gripper device. 
	 * @param index the device index
	 * @param access access mode
	 * @return a Gripper device if successful, null otherwise
	 */
	public GripperInterface requestInterfaceGripper (int index, int access) {
		requestDeviceAccess (PLAYER_GRIPPER_CODE, index, access);
		if (isReadyRequestDevice ())
			return (GripperInterface)newpd;
		else
			return null;
	}


	/**
	 * Request a Position2D device. 
	 * @param index the device index
	 * @param access access mode
	 * @return a Position2D device if successful, null otherwise
	 */
	public Position2DInterface requestInterfacePosition2D (int index, int access) {
		requestDeviceAccess (PLAYER_POSITION2D_CODE, index, access);

		if (isReadyRequestDevice ())
			return (Position2DInterface)newpd;
		else
			return null;
	}
	
	/**
	 * Request an Opaque device. 
	 * @param index the device index
	 * @param access access mode
	 * @return an Opaque device if successful, null otherwise
	 */
	public OpaqueInterface requestInterfaceOpaque(int index, int access) {
		requestDeviceAccess(PLAYER_OPAQUE_CODE, index, access);
		
		if (isReadyRequestDevice())
			return (OpaqueInterface)newpd;
		else
			return null;
	}

	/**
	 * Request a Sonar device. 
	 * @param index the device index
	 * @param access access mode
	 * @return a Sonar device if successful, null otherwise
	 */
	public SonarInterface requestInterfaceSonar (int index, int access) {
		requestDeviceAccess (PLAYER_SONAR_CODE, index, access);
		if (isReadyRequestDevice ())
			return (SonarInterface)newpd;
		else
			return null;
	}

	/**
	 * Request a Laser device. 
	 * @param index the device index
	 * @param access access mode
	 * @return a Laser device if successful, null otherwise
	 */
	public LaserInterface requestInterfaceLaser (int index, int access) {
		requestDeviceAccess (PLAYER_LASER_CODE, index, access);
		if (isReadyRequestDevice ())
			return (LaserInterface)newpd;
		else
			return null;
	}

	/**
	 * Request a Blobfinder device. 
	 * @param index the device index
	 * @param access access mode
	 * @return a Blobfinder device if successful, null otherwise
	 */
	public BlobfinderInterface requestInterfaceBlobfinder (int index, int access) {
		requestDeviceAccess (PLAYER_BLOBFINDER_CODE, index, access);
		if (isReadyRequestDevice ())
			return (BlobfinderInterface)newpd;
		else
			return null;
	}

	/**
	 * Request a Ptz device. 
	 * @param index the device index
	 * @param access access mode
	 * @return a Ptz device if successful, null otherwise
	 */
	public PtzInterface requestInterfacePtz (int index, int access) {
		requestDeviceAccess (PLAYER_PTZ_CODE, index, access);
		if (isReadyRequestDevice ())
			return (PtzInterface)newpd;
		else
			return null;
	}

	/**
	 * Request a Fiducial device. 
	 * @param index the device index
	 * @param access access mode
	 * @return a Fiducial device if successful, null otherwise
	 */
	public FiducialInterface requestInterfaceFiducial (int index, int access) {
		requestDeviceAccess (PLAYER_FIDUCIAL_CODE, index, access);
		if (isReadyRequestDevice ())
			return (FiducialInterface)newpd;
		else
			return null;
	}

	/**
	 * Request a Speech device. 
	 * @param index the device index
	 * @param access access mode
	 * @return a Speech device if successful, null otherwise
	 */
	public SpeechInterface requestInterfaceSpeech (int index, int access) {
		requestDeviceAccess (PLAYER_SPEECH_CODE, index, access);
		if (isReadyRequestDevice ())
			return (SpeechInterface)newpd;
		else
			return null;
	}

	/**
	 * Request a GPS device. 
	 * @param index the device index
	 * @param access access mode
	 * @return a GPS device if successful, null otherwise
	 */
	public GPSInterface requestInterfaceGPS (int index, int access) {
		requestDeviceAccess (PLAYER_GPS_CODE, index, access);
		if (isReadyRequestDevice ())
			return (GPSInterface)newpd;
		else
			return null;
	}

	/**
	 * Request a Bumper device. 
	 * @param index the device index
	 * @param access access mode
	 * @return a Bumper device if successful, null otherwise
	 */
	public BumperInterface requestInterfaceBumper (int index, int access) {
		requestDeviceAccess (PLAYER_BUMPER_CODE, index, access);
		if (isReadyRequestDevice ())
			return (BumperInterface)newpd;
		else
			return null;
	}

	/**
	 * Request a DIO device. 
	 * @param index the device index
	 * @param access access mode
	 * @return a DIO device if successful, null otherwise
	 */
	public DIOInterface requestInterfaceDIO (int index, int access) {
		requestDeviceAccess (PLAYER_DIO_CODE, index, access);
		if (isReadyRequestDevice ())
			return (DIOInterface)newpd;
		else
			return null;
	}

	/**
	 * Request an AIO device. 
	 * @param index the device index
	 * @param access access mode
	 * @return an AIO device if successful, null otherwise
	 */
	public AIOInterface requestInterfaceAIO (int index, int access) {
		requestDeviceAccess (PLAYER_AIO_CODE, index, access);
		if (isReadyRequestDevice ())
			return (AIOInterface)newpd;
		else
			return null;
	}

	/**
	 * Request an IR device. 
	 * @param index the device index
	 * @param access access mode
	 * @return an IR device if successful, null otherwise
	 */
	public IRInterface requestInterfaceIR (int index, int access) {
		requestDeviceAccess (PLAYER_IR_CODE, index, access);
		if (isReadyRequestDevice ())
			return (IRInterface)newpd;
		else
			return null;
	}

	/**
	 * Request a WiFi device. 
	 * @param index the device index
	 * @param access access mode
	 * @return a WiFi device if successful, null otherwise
	 */
	public WiFiInterface requestInterfaceWiFi (int index, int access) {
		requestDeviceAccess (PLAYER_WIFI_CODE, index, access);
		if (isReadyRequestDevice ())
			return (WiFiInterface)newpd;
		else
			return null;
	}

	/**
	 * Request a Waveform device. 
	 * @param index the device index
	 * @param access access mode
	 * @return a Waveform device if successful, null otherwise
	 */
	public WaveformInterface requestInterfaceWaveform (int index, int access) {
		requestDeviceAccess (PLAYER_WAVEFORM_CODE, index, access);
		if (isReadyRequestDevice ())
			return (WaveformInterface)newpd;
		else
			return null;
	}

	/**
	 * Request a Localize device. 
	 * @param index the device index
	 * @param access access mode
	 * @return a Localize device if successful, null otherwise
	 */
	public LocalizeInterface requestInterfaceLocalize (int index, int access) {
		requestDeviceAccess (PLAYER_LOCALIZE_CODE, index, access);
		if (isReadyRequestDevice ())
			return (LocalizeInterface)newpd;
		else
			return null;
	}

	/**
	 * Request a MComm device. 
	 * @param index the device index
	 * @param access access mode
	 * @return a MComm device if successful, null otherwise
	 */
	public MComInterface requestInterfaceMCom (int index, int access) {
		requestDeviceAccess (PLAYER_MCOM_CODE, index, access);
		if (isReadyRequestDevice ())
			return (MComInterface)newpd;
		else
			return null;
	}

	/**
	 * Request a Sound device. 
	 * @param index the device index
	 * @param access access mode
	 * @return a Sound device if successful, null otherwise
	 */
	public SoundInterface requestInterfaceSound (int index, int access) {
		requestDeviceAccess (PLAYER_SOUND_CODE, index, access);
		if (isReadyRequestDevice ())
			return (SoundInterface)newpd;
		else
			return null;
	}

	/**
	 * Request an AudioDSP device. 
	 * @param index the device index
	 * @param access access mode
	 * @return an AudioDSP device if successful, null otherwise
	 */
	public AudioDSPInterface requestInterfaceAudioDSP (int index, int access) {
		requestDeviceAccess (PLAYER_AUDIODSP_CODE, index, access);
		if (isReadyRequestDevice ())
			return (AudioDSPInterface)newpd;
		else
			return null;
	}

	/**
	 * Request an AudioMixer device. 
	 * @param index the device index
	 * @param access access mode
	 * @return an AudioMixer device if successful, null otherwise
	 */
	public AudioMixerInterface requestInterfaceAudioMixer (int index, int access) {
		requestDeviceAccess (PLAYER_AUDIOMIXER_CODE, index, access);
		if (isReadyRequestDevice ())
			return (AudioMixerInterface)newpd;
		else
			return null;
	}

	/**
	 * Request a Position3D device. 
	 * @param index the device index
	 * @param access access mode
	 * @return a Position3D device if successful, null otherwise
	 */
	public Position3DInterface requestInterfacePosition3D (int index, int access) {
		requestDeviceAccess (PLAYER_POSITION3D_CODE, index, access);
		if (isReadyRequestDevice ())
			return (Position3DInterface)newpd;
		else
			return null;
	}

	/**
	 * Request a Simulation device. 
	 * @param index the device index
	 * @param access access mode
	 * @return a Simulation device if successful, null otherwise
	 */
	public SimulationInterface requestInterfaceSimulation (int index, int access) {
		requestDeviceAccess (PLAYER_SIMULATION_CODE, index, access);
		if (isReadyRequestDevice ())
			return (SimulationInterface)newpd;
		else
			return null;
	}

	/**
	 * Request a Blinkenlight device. 
	 * @param index the device index
	 * @param access access mode
	 * @return a Blinkenlight device if successful, null otherwise
	 */
	public BlinkenlightInterface requestInterfaceBlinkenlight (int index, int access) {
		requestDeviceAccess (PLAYER_BLINKENLIGHT_CODE, index, access);
		if (isReadyRequestDevice ())
			return (BlinkenlightInterface)newpd;
		else
			return null;
	}

	/**
	 * Request a Camera device. 
	 * @param index the device index
	 * @param access access mode
	 * @return a Camera device if successful, null otherwise
	 */
	public CameraInterface requestInterfaceCamera (int index, int access) {
		requestDeviceAccess (PLAYER_CAMERA_CODE, index, access);
		if (isReadyRequestDevice ())
			return (CameraInterface)newpd;
		else
			return null;
	}

	/**
	 * Request a Map device. 
	 * @param index the device index
	 * @param access access mode
	 * @return a Map device if successful, null otherwise
	 */
	public MapInterface requestInterfaceMap (int index, int access) {
		requestDeviceAccess (PLAYER_MAP_CODE, index, access);
		if (isReadyRequestDevice ())
			return (MapInterface)newpd;
		else
			return null;
	}

	/**
	 * Request a Planner device. 
	 * @param index the device index
	 * @param access access mode
	 * @return a Planner device if successful, null otherwise
	 */
	public PlannerInterface requestInterfacePlanner (int index, int access) {
		requestDeviceAccess (PLAYER_PLANNER_CODE, index, access);
		if (isReadyRequestDevice ())
			return (PlannerInterface)newpd;
		else
			return null;
	}

	/**
	 * Request a Log device. 
	 * @param index the device index
	 * @param access access mode
	 * @return a Log device if successful, null otherwise
	 */
	public LogInterface requestInterfaceLog (int index, int access) {
		requestDeviceAccess (PLAYER_LOG_CODE, index, access);
		if (isReadyRequestDevice ())
			return (LogInterface)newpd;
		else
			return null;
	}

	/**
	 * Request a Joystick device. 
	 * @param index the device index
	 * @param access access mode
	 * @return a Joystick device if successful, null otherwise
	 */
	public JoystickInterface requestInterfaceJoystick (int index, int access) {
		requestDeviceAccess (PLAYER_JOYSTICK_CODE, index, access);
		if (isReadyRequestDevice ())
			return (JoystickInterface)newpd;
		else
			return null;
	}

	/**
	 * Request a Speech Recognition device. 
	 * @param index the device index
	 * @param access access mode
	 * @return a Speech Recognition device if successful, null otherwise
	 */
	public SpeechRecognitionInterface requestInterfaceSpeechRecognition (int index, int access) {
		requestDeviceAccess (PLAYER_SPEECH_RECOGNITION_CODE, index, access);
		if (isReadyRequestDevice ())
			return (SpeechRecognitionInterface)newpd;
		else
			return null;
	}
	
	/**
	 * Request a Position1D device. 
	 * @param index the device index
	 * @param access access mode
	 * @return a Position1D device if successful, null otherwise
	 */
	public Position1DInterface requestInterfacePosition1D (int index, int access) {
		requestDeviceAccess (PLAYER_POSITION1D_CODE, index, access);
		if (isReadyRequestDevice ())
			return (Position1DInterface)newpd;
		else
			return null;
	}

	/**
	 * Request an Actarray device. 
	 * @param index the device index
	 * @param access access mode
	 * @return an Actarray device if successful, null otherwise
	 */
	public ActarrayInterface requestInterfaceActarray (int index, int access) {
		requestDeviceAccess (PLAYER_ACTARRAY_CODE, index, access);
		if (isReadyRequestDevice ())
			return (ActarrayInterface)newpd;
		else
			return null;
	}

	/**
	 * Request a Limb device. 
	 * @param index the device index
	 * @param access access mode
	 * @return a Limb device if successful, null otherwise
	 */
	public LimbInterface requestInterfaceLimb (int index, int access) {
		requestDeviceAccess (PLAYER_LIMB_CODE, index, access);
		if (isReadyRequestDevice ())
			return (LimbInterface)newpd;
		else
			return null;
	}

	/**
	 * Request a RFID device. 
	 * @param index the device index
	 * @param access access mode
	 * @return a RFID device if successful, null otherwise
	 */
	public RFIDInterface requestInterfaceRFID (int index, int access) {
		requestDeviceAccess (PLAYER_RFID_CODE, index, access);
		if (isReadyRequestDevice ())
			return (RFIDInterface)newpd;
		else
			return null;
	}
}
