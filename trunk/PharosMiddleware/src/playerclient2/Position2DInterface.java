/*
 *  Player Java Client 2 - Position2DInterface.java
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
 * $Id: Position2DInterface.java,v 1.6 2006/03/10 19:05:00 veedee Exp $
 *
 */
package playerclient2;

import java.io.IOException;
import java.util.*;

import playerclient2.structures.PlayerBbox;
import playerclient2.structures.PlayerMsgHdr;
import playerclient2.structures.PlayerPose;
import playerclient2.structures.position2d.PlayerPosition2dData;
import playerclient2.structures.position2d.PlayerPosition2dGeom;
import playerclient2.xdr.OncRpcException;
import playerclient2.xdr.XdrBufferDecodingStream;
import playerclient2.xdr.XdrBufferEncodingStream;


/**
 * The position2d interface is used to control a mobile robot bases in 2D. 
 * 
 * @author Chien-Liang Fok
 * @author Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v2.0 - Player 2.0 supported
 * </ul>
 */
public class Position2DInterface extends AbstractPositionDevice {
    
    private static final boolean isDebugging = PlayerClient.isDebugging;

    private PlayerPosition2dData pp2ddata = null;
    private boolean newXdata = false;
    private boolean newYdata = false;
    private boolean newYawData = false;
    
	private PlayerPosition2dGeom pp2dgeom;
	private boolean newGeomData = false;
	
	private Vector<Position2DListener> pos2dListeners = new Vector<Position2DListener>();
    
    /**
     * Constructor for Position2DInterface.
     * @param pc a reference to the PlayerClient object
     */
    protected Position2DInterface (PlayerClient pc) { super(pc); }
    
    /**
     * Read the position2d data values (state or geom).
     */
    public synchronized void readData (PlayerMsgHdr header) {
        try {
        	switch (header.getSubtype ()) {
        		case PLAYER_POSITION2D_DATA_STATE: {
        			// Buffer for reading pos, vel and stall
        			byte[] buffer = new byte[24+24+4];
        			// Read pos, vel and stall
        			is.readFully (buffer, 0, 24+24+4);
        			
        			pp2ddata = new PlayerPosition2dData (); 
        			PlayerPose pos = new PlayerPose ();
        			PlayerPose vel = new PlayerPose ();
        			
        			// Begin decoding the XDR buffer
        			XdrBufferDecodingStream xdr = new XdrBufferDecodingStream (buffer);
        			xdr.beginDecoding ();
        			
        			// position [m, m, rad]
        			pos.setPx (xdr.xdrDecodeDouble ());
        			pos.setPy (xdr.xdrDecodeDouble ());
        			pos.setPa (xdr.xdrDecodeDouble ());
        			pp2ddata.setPos (pos);
        			// translational velocities [m/s, m/s, rad/s]
        			vel.setPx (xdr.xdrDecodeDouble ());
        			vel.setPy (xdr.xdrDecodeDouble ());
        			vel.setPa (xdr.xdrDecodeDouble ());
        			pp2ddata.setVel (vel);
        			// motors stall
        			pp2ddata.setStall (xdr.xdrDecodeByte ());
        			xdr.endDecoding   ();
        			xdr.close ();
        			
        			newXdata = true;
        		    newYdata = true;
        		    newYawData = true;
        		    
        			notifyP2DListeners();
        			break;
        		}
        		case PLAYER_POSITION2D_DATA_GEOM: {
        			readGeom ();
        			notifyAll();
//        	    	readyPp2dgeom = true;
        			break;
        		}
        	}
        } catch (IOException e) {
        	throw new PlayerException 
        		("[Position2D] : Error reading payload: " + 
        				e.toString(), e);
        } catch (OncRpcException e) {
        	throw new PlayerException 
        		("[Position2D] : Error while XDR-decoding payload: " + 
        				e.toString(), e);
        }
    }
    
    /**
     * Add a listener to this object.  This listener is notified each time a new PlayerPosition2dData 
     * is available.
     * 
     * @param listener  The listener to add.
     */
    public void addPos2DListener(Position2DListener listener) {
    	pos2dListeners.add(listener);
    }
    
    /**
     * Removes a listener from this object.
     * 
     * @param listener The listener to remove.
     */
    public void removePos2DListener(Position2DListener listener) {
    	pos2dListeners.remove(listener);
    }
    
    /**
     * Notifies each of the registered Position2DListeners that a new PlayerPosition2dData is available.
     */
    private void notifyP2DListeners() {
    	Enumeration<Position2DListener> e = pos2dListeners.elements();
    	while (e.hasMoreElements()) {
    		e.nextElement().newPlayerPosition2dData(pp2ddata);
    	}
    }
    
    /**
     * The position interface accepts new positions for the robot's motors 
     * (drivers may support position control, speed control or both).
     * <br><br>
     * See the player_position2d_cmd_pos structure from player.h
     * @param pos a PlayerPose structure containing the position data 
     * (x, y, yaw) [m, m, rad]
     * @param state motor state (zero is either off or locked, depending on the driver)
     */
    public void setPosition (PlayerPose pos, int state) {
        try {
        	sendHeader (PLAYER_MSGTYPE_CMD, PLAYER_POSITION2D_CMD_POS, 12+4);
        	XdrBufferEncodingStream xdr = new XdrBufferEncodingStream (12+4);
        	xdr.beginEncoding (null, 0);
        	xdr.xdrEncodeDouble (pos.getPx ()); // modified by liang
        	xdr.xdrEncodeDouble (pos.getPy ());
        	xdr.xdrEncodeDouble (pos.getPa ());
        	xdr.xdrEncodeByte ((byte)state);
        	xdr.endEncoding ();
        	os.write (xdr.getXdrData (), 0, xdr.getXdrLength ());
        	xdr.close ();
        	os.flush ();
        } catch (IOException e) {
        	throw new PlayerException 
        		("[Position2D] : Couldn't send position command: " + 
        				e.toString(), e);
        } catch (OncRpcException e) {
        	throw new PlayerException 
        		("[Position2D] : Error while XDR-encoding position command: " + 
        				e.toString(), e);
        }
    }

    /**
     * The position interface accepts new velocities for the robot's motors 
     * (drivers may support position control, speed control or both).
     * <br><br>
     * See the player_position2d_cmd_vel structure from player.h
     * @param vel a PlayerPose structure containing the translational 
     * velocities (x, y, yaw) [m/s, m/s, rad/s]
     * @param state motor state (zero is either off or locked, depending on the driver)
     */
    public void setVelocity (PlayerPose vel, int state) {
        try {
        	sendHeader (PLAYER_MSGTYPE_CMD, PLAYER_POSITION2D_CMD_VEL, 8*3+4);
        	XdrBufferEncodingStream xdr = new XdrBufferEncodingStream (8*3+4);
        	xdr.beginEncoding (null, 0);
        	xdr.xdrEncodeDouble (vel.getPx ()); // modified by liang
        	xdr.xdrEncodeDouble (vel.getPy ());
        	xdr.xdrEncodeDouble (vel.getPa ());
        	xdr.xdrEncodeByte ((byte)state);
        	xdr.endEncoding ();
        	os.write (xdr.getXdrData (), 0, xdr.getXdrLength ());
        	xdr.close ();
        	os.flush ();
        } catch (IOException e) {
        	throw new PlayerException 
        		("[Position2D] : Couldn't send velocity command: " + 
        				e.toString(), e);
        } catch (OncRpcException e) {
        	throw new PlayerException 
        		("[Position2D] : Error while XDR-encoding velocity command: " + 
        				e.toString(), e);
        }
    }
    
    /**
     * The position interface accepts new carlike velocity (speed and turning angle)
     * for the robot's motors (only supported by some drivers).
     * <br><br>
     * See the player_position2d_cmd_car structure from player.h
     * @param velocity forward velocity (m/s)
     * @param angle turning angle (rad)
     */
    public void setCarCMD (double velocity, double angle) {
        try {
        	sendHeader (PLAYER_MSGTYPE_CMD, PLAYER_POSITION2D_CMD_CAR, 8*2);
        	XdrBufferEncodingStream xdr = new XdrBufferEncodingStream (8*2);
        	xdr.beginEncoding (null, 0);
        	xdr.xdrEncodeDouble (velocity);
        	xdr.xdrEncodeDouble (angle);
        	xdr.endEncoding ();
        	os.write (xdr.getXdrData (), 0, xdr.getXdrLength ());
        	xdr.close ();
        	os.flush ();
        } catch (IOException e) {
        	throw new PlayerException 
        		("[Position2D] : Couldn't send carlike command: " + 
        				e.toString(), e);
        } catch (OncRpcException e) {
        	throw new PlayerException 
        		("[Position2D] : Error while XDR-encoding carlike command: " + 
        				e.toString(), e);
        }
    }
    
    private synchronized void readGeom () {
    	try {
    		// Buffer for reading pose and size
    		byte[] buffer = new byte[12+8];
    		// Read pose and size
    		is.readFully (buffer, 0, 12+8);
    		
    		pp2dgeom = new PlayerPosition2dGeom (); 
    		PlayerPose pose = new PlayerPose ();
    		PlayerBbox size = new PlayerBbox ();
    		
    		// Begin decoding the XDR buffer
    		XdrBufferDecodingStream xdr = new XdrBufferDecodingStream (buffer);
    		xdr.beginDecoding ();
    		
    		// pose of the robot base [m, m, rad]
    		pose.setPx (xdr.xdrDecodeFloat ());
    		pose.setPy (xdr.xdrDecodeFloat ());
    		pose.setPa (xdr.xdrDecodeFloat ());
    		pp2dgeom.setPose (pose);
    		// dimensions of the base [m, m]
    		size.setSw (xdr.xdrDecodeFloat ());
    		size.setSl (xdr.xdrDecodeFloat ());
    		pp2dgeom.setSize (size);
    		xdr.endDecoding   ();
    		xdr.close ();
    		
    		newGeomData = true;
        } catch (IOException e) {
        	throw new PlayerException 
        		("[Position2D] : Error reading geometry data: " + 
        				e.toString(), e);
        } catch (OncRpcException e) {
        	throw new PlayerException 
        		("[Position2D] : Error while XDR-decoding geometry data: " + 
        				e.toString(), e);
        }
    }
    
    /**
     * Request/reply: Query geometry.
     */
    public void queryGeometry () {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, PLAYER_POSITION2D_REQ_GET_GEOM, 0);
            os.flush ();
        } catch (IOException e) {
        	throw new PlayerException 
        		("[Position2D] : Couldn't request " + 
        				"PLAYER_POSITION2D_REQ_GET_GEOM: " + 
        				e.toString(), e);
        }
    }

    /**
     * Configuration request: Motor power.
     * <br><br>
     * On some robots, the motor power can be turned on and off from software.
     * <br><br>
     * Be VERY careful with this command! You are very likely to start the robot 
     * running across the room at high speed with the battery charger still attached.
     * @param state 0 for off, 1 for on 
     */
    public void setMotorPower (int state) {
        try {
        	sendHeader (PLAYER_MSGTYPE_REQ, PLAYER_POSITION2D_REQ_MOTOR_POWER, 4);
        	XdrBufferEncodingStream xdr = new XdrBufferEncodingStream (4);
        	xdr.beginEncoding (null, 0);
        	xdr.xdrEncodeByte ((byte)state);
        	xdr.endEncoding ();
        	os.write (xdr.getXdrData (), 0, xdr.getXdrLength ());
        	xdr.close ();
        	os.flush ();
        } catch (IOException e) {
        	throw new PlayerException 
        		("[Position2D] : Couldn't request " + "" +
        				"PLAYER_POSITION2D_REQ_MOTOR_POWER: " + 
        				e.toString(), e);
        } catch (OncRpcException e) {
        	throw new PlayerException 
        		("[Position2D] : Error while XDR-encoding POWER request: " + 
        				e.toString(), e);
        }
    }
    
    /**
     * Configuration request: Change velocity control.
     * <br><br>
     * Some robots offer different velocity control modes.
     * <br><br>
     * The p2os driver offers two modes of velocity control: separate translational and rotational 
     * control and direct wheel control. When in the separate mode, the robot's microcontroller 
     * internally computes left and right wheel velocities based on the currently commanded 
     * translational and rotational velocities and then attenuates these values to match a nice 
     * predefined acceleration profile. When in the direct mode, the microcontroller simply passes 
     * on the current left and right wheel velocities. Essentially, the separate mode offers 
     * smoother but slower (lower acceleration) control, and the direct mode offers faster but 
     * jerkier (higher acceleration) control. Player's default is to use the direct mode. Set mode 
     * to zero for direct control and non-zero for separate control.
     * <br><br>
     * For the reb driver, 0 is direct velocity control, 1 is for velocity-based heading PD 
     * controller. 
     * @param mode driver-specific mode
     */
    public void setVelocityControl (int mode) {
        try {
        	sendHeader (PLAYER_MSGTYPE_REQ, PLAYER_POSITION2D_REQ_VELOCITY_MODE, 4);
        	XdrBufferEncodingStream xdr = new XdrBufferEncodingStream (4);
        	xdr.beginEncoding (null, 0);
        	xdr.xdrEncodeByte ((byte)mode);
        	xdr.endEncoding ();
        	os.write (xdr.getXdrData (), 0, xdr.getXdrLength ());
        	xdr.close ();
        	os.flush ();
        } catch (IOException e) {
        	throw new PlayerException 
        		("[Position2D] : Couldn't request " + "" +
        				"PLAYER_POSITION2D_REQ_VELOCITY_MODE: " + 
        				e.toString(), e);
        } catch (OncRpcException e) {
        	throw new PlayerException 
        		("[Position2D] : Error while XDR-encoding VELOCITY_MODE " + 
        				"request: " + e.toString(), e);
        }
    }
    
    /**
     * Configuration request: Reset odometry.
     * <br><br>
     * Resets the robot's odometry to (x,y,theta) = (0,0,0).
     */
    public void resetOdometry () {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, PLAYER_POSITION2D_REQ_RESET_ODOM, 0);
            os.flush ();
        } catch (IOException e) {
        	throw new PlayerException 
        		("[Position2D] : Couldn't request " + "" +
        				"PLAYER_POSITION2D_RESET_ODOM_REQ: " + 
        				e.toString(), e);
        }
    }
    
    /**
     * Configuration request: Change control mode.
     * @param state 0 for velocity mode, 1 for position mode
     */
    public void setControlMode (int state) {
        try {
        	sendHeader (PLAYER_MSGTYPE_REQ, PLAYER_POSITION2D_REQ_POSITION_MODE, 4);
        	XdrBufferEncodingStream xdr = new XdrBufferEncodingStream (4);
        	xdr.beginEncoding (null, 0);
        	xdr.xdrEncodeInt (state);
        	xdr.endEncoding ();
        	os.write (xdr.getXdrData (), 0, xdr.getXdrLength ());
        	xdr.close ();
        	os.flush ();
        } catch (IOException e) {
        	throw new PlayerException 
        		("[Position2D] : Couldn't request " + "" +
        				"PLAYER_POSITION2D_REQ_POSITION_MODE: " + 
        				e.toString(), e);
        } catch (OncRpcException e) {
        	throw new PlayerException 
        		("[Position2D] : Error while XDR-encoding POSITION_MODE " + 
        				"request: " + e.toString(), e);
        }
    }
    
    /**
     * Configuration request: Set odometry.
     * @param pose (x, y, yaw) [m, m, rad]
     */
    public void setOdometry (PlayerPose pose) {
        try {
        	sendHeader (PLAYER_MSGTYPE_REQ, PLAYER_POSITION2D_REQ_SET_ODOM, 12);
        	XdrBufferEncodingStream xdr = new XdrBufferEncodingStream (12);
        	xdr.beginEncoding (null, 0);
        	xdr.xdrEncodeDouble (pose.getPx ());
        	xdr.xdrEncodeDouble (pose.getPy ());
        	xdr.xdrEncodeDouble (pose.getPa ());
        	xdr.endEncoding ();
        	os.write (xdr.getXdrData (), 0, xdr.getXdrLength ());
        	xdr.close ();
        	os.flush ();
        } catch (IOException e) {
        	throw new PlayerException 
        		("[Position2D] : Couldn't request " + "" +
        				"PLAYER_POSITION2D_REQ_SET_ODOM: " + 
        				e.toString(), e);
        } catch (OncRpcException e) {
        	throw new PlayerException 
        		("[Position2D] : Error while XDR-encoding SET_ODOM request:" + 
        				e.toString(), e);
        }
    }
    
    /**
     * Configuration request: Set velocity PID parameters.
     * @param kp P parameter
     * @param ki I parameter
     * @param kd D parameter
     */
    public void setVelocityPIDParams (float kp, float ki, float kd) {
        try {
        	sendHeader (PLAYER_MSGTYPE_REQ, PLAYER_POSITION2D_REQ_SPEED_PID, 12);
        	XdrBufferEncodingStream xdr = new XdrBufferEncodingStream (12);
        	xdr.beginEncoding (null, 0);
        	xdr.xdrEncodeFloat (kp);
        	xdr.xdrEncodeFloat (ki);
        	xdr.xdrEncodeFloat (kd);
        	xdr.endEncoding ();
        	os.write (xdr.getXdrData (), 0, xdr.getXdrLength ());
        	xdr.close ();
        	os.flush ();
        } catch (IOException e) {
        	throw new PlayerException 
        		("[Position2D] : Couldn't request " + "" +
        				"PLAYER_POSITION2D_REQ_SPEED_PID: " + 
        				e.toString(), e);
        } catch (OncRpcException e) {
        	throw new PlayerException 
        		("[Position2D] : Error while XDR-encoding SPEED_PID request:" + 
        				e.toString(), e);
        }
    }
    
    /**
     * Configuration request: Set position PID parameters.
     * @param kp P parameter
     * @param ki I parameter
     * @param kd D parameter
     */
    public void setPositionPIDParams (float kp, float ki, float kd) {
        try {
        	sendHeader (PLAYER_MSGTYPE_REQ, PLAYER_POSITION2D_REQ_POSITION_PID, 12);
        	XdrBufferEncodingStream xdr = new XdrBufferEncodingStream (12);
        	xdr.beginEncoding (null, 0);
        	xdr.xdrEncodeFloat (kp);
        	xdr.xdrEncodeFloat (ki);
        	xdr.xdrEncodeFloat (kd);
        	xdr.endEncoding ();
        	os.write (xdr.getXdrData (), 0, xdr.getXdrLength ());
        	xdr.close ();
        	os.flush ();
        } catch (IOException e) {
        	throw new PlayerException 
        		("[Position2D] : Couldn't request " + "" +
        				"PLAYER_POSITION2D_REQ_POSITION_PID: " + 
        				e.toString(), e);
        } catch (OncRpcException e) {
        	throw new PlayerException 
        		("[Position2D] : Error while XDR-encoding POSITION_PID " +
        				"request:" + e.toString(), e);
        }
    }
    
    /**
     * Configuration request: Set speed profile parameters.
     * @param speed max speed [m/s] 
     * @param acc max acceleration [m/s^2] 
     */
    public void setSpeedProfileParams (float speed, float acc) {
        try {
        	sendHeader (PLAYER_MSGTYPE_REQ, PLAYER_POSITION2D_REQ_SPEED_PROF, 8);
        	XdrBufferEncodingStream xdr = new XdrBufferEncodingStream (8);
        	xdr.beginEncoding (null, 0);
        	xdr.xdrEncodeFloat (speed);
        	xdr.xdrEncodeFloat (acc);
        	xdr.endEncoding ();
        	os.write (xdr.getXdrData (), 0, xdr.getXdrLength ());
        	xdr.close ();
        	os.flush ();
        } catch (IOException e) {
        	throw new PlayerException 
        		("[Position2D] : Couldn't request " + "" +
        				"PLAYER_POSITION2D_REQ_SPEED_PROF: " + 
        				e.toString(), e);
        } catch (OncRpcException e) {
        	throw new PlayerException 
        		("[Position2D] : Error while XDR-encoding SPEED_PROF " +
        				"request:" + e.toString(), e);
        }
    }
    
    
    /**
     * Handle acknowledgment response messages.
     * 
     * @param header Player header
     */
    protected void handleResponse (PlayerMsgHdr header) {
        switch (header.getSubtype ()) {
            case PLAYER_POSITION2D_REQ_GET_GEOM: {
            	readGeom ();
//    	    	readyPp2dgeom = true;
            	break;
            }
            case PLAYER_POSITION2D_REQ_MOTOR_POWER: {
            	// null response
            	break;
            }
            case PLAYER_POSITION2D_REQ_VELOCITY_MODE: {
            	// null response
            	break;
            }
            case PLAYER_POSITION2D_REQ_POSITION_MODE: {
            	// null response
            	break;
            }
            case PLAYER_POSITION2D_REQ_SET_ODOM: {
            	// null response
                break;
            }
            case PLAYER_POSITION2D_REQ_RESET_ODOM: {
            	// null response
                break;
            }
            case PLAYER_POSITION2D_REQ_SPEED_PID: {
            	// null response
                break;
            }
            case PLAYER_POSITION2D_REQ_POSITION_PID: {
            	// null response
                break;
            }
            case PLAYER_POSITION2D_REQ_SPEED_PROF: {
            	// null response
                break;
            }
            default:{
            	if (isDebugging)
            		System.err.println ("[Position2D][Debug] : " +
            				"Unexpected response " + header.getSubtype () + 
            				" of size = " + header.getSize ());
                break;
            }
        }
    }
    
//    /**
//     * Get the Position2D data.  If the data is not available, wait for it to arrive.
//     * 
//     * @return an object of type PlayerPosition2DData containing the requested data 
//     */
//    private synchronized PlayerPosition2dData getData () {
//    	while (pp2ddata == null) {
//    		if (isDebugging)
//    			System.err.println ("[Position2D][Debug][Position2DInterface.getData]: Waiting for pp2ddata to arrive...");
//    		try {
//    			wait(1000);  // wait for pp2data to arrive
//    		} catch (InterruptedException e) {
//    			e.printStackTrace();
//    		}
//    	}
//    	return pp2ddata;
//    }
    
    /**
     * Get the geometry data.
     * 
     * @return an object of type PlayerPosition2DGeom containing the requested data
     * @throws NoNewDataException of no new geometry data was received since the last time this method was called.
     */
    public synchronized PlayerPosition2dGeom getGeom () throws NoNewDataException { 
    	if (pp2dgeom == null || !newGeomData) 
    		throw new NoNewDataException();
    	else {
    		newGeomData = false;
    		return pp2dgeom;
    	}
    }
    
    /**
     * Returns the X location coordinate.  If no new C-coordinate information was received since the last time this
     * method was called, throw an exception.
     * 
     * @return The X location coordinate.
     * @throws NoNewDataException If no new X-coordinate data was received since the last time this method was called.
     */
    public synchronized double getX() throws NoNewDataException {
    	if (pp2ddata == null || !newXdata) 
    		throw new NoNewDataException();
    	else {
    		newXdata = false;
    		return pp2ddata.getPos().getPx();
    	}
    }
    
    /**
     * Returns the Y location coordinate.  If no new Y-coordinate information was received since the last time this
     * method was called, throw an exception.
     * 
     * @return The Y location coordinate.
     * @throws NoNewDataException If no new Y-coordinate data was received since the last time this method was called.
     */
    public synchronized double getY() throws NoNewDataException {
    	if (pp2ddata == null || !newYdata) 
    		throw new NoNewDataException();
    	else {
    		newYdata = false;
    		return pp2ddata.getPos().getPy();
    	}
    }
    
    /**
     * Returns the yaw in radians.  If no new yaw information was received since the last time this
     * method was called, throw an exception.
     * 
     * @return The yaw in radians (-pi to +pi)
     * @throws NoNewDataException If no new yaw data was received since the last time this method was called.
     */
    public synchronized double getYaw() throws NoNewDataException {
    	if (pp2ddata == null || !newYawData) 
    		throw new NoNewDataException();
    	else {
    		newYawData = false;
    		return pp2ddata.getPos().getPa();
    	}
    }
    
    /**
     * Sets the speed of the robot.
     * 
     * @param speed The speed of the robot
     * @param turnrate The rate at which the robot should turn.
     */
    public void setSpeed (double speed, double turnrate) {
    	PlayerPose pp = new PlayerPose ();
    	pp.setPx (speed);
    	pp.setPa (turnrate);
    	setVelocity (pp, 1);
    }
}
