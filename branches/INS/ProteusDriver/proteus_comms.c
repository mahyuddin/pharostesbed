/*
 *  Player - One Hell of a Robot Server
 *  Copyright (C) 2006 -
 *     Brian Gerkey
 *
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
 * Modified by n.a.paine@gmail.com
 * Modified by Chien-Liang Fok on 11/25/2010
 *   - Added comments
 *   - Added O_FSYNC option to the opening of the serial port's file descriptor to make all writes
 *     immediate (no buffering)
 *   - Added method sendToMCU because write() may not actually write all of the bytes to the serial port.
 *     Instead, it returns the number of bytes actually written.
 *   - remove the sleep(...) hack
 *   - Fixed code that makes reading from the serial port blocking in proteus_open(...)
 */

#include <errno.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <termios.h>
#include <math.h>
#include <stdio.h>
#include <unistd.h>
#include <netinet/in.h>

#include "config.h"

#include <player-2.1/libplayercore/playerconfig.h>
#include <player-2.1/libplayercore/error.h>
#include "replace.h"
//#include <sys/poll.h>

#include "proteus_comms.h"

#include <sys/time.h>

/**
 * Allocates memory for a proteus_comm_t object.
 * This object contains the details of the serial connection to the
 * micro-controller.
 * 
 * @return The newly allocated proteus_comm_t object.
 */
proteus_comm_t* proteus_create(const char* serial_port) {
	proteus_comm_t* r;
	r = (proteus_comm_t*)calloc(1,sizeof(proteus_comm_t)); // Liang: why not use malloc?
	assert(r); // terminate program if memory allocation failed
	r->fd = -1; // initially, the serial port is not opened
	r->mode = PROTEUS_MODE_OFF; // initially not connected to micro-controller
	strncpy(r->serial_port,serial_port,sizeof(r->serial_port)-1); // save the name of the serial port.
	r->rxBuffStartIndx = 0;
	r->rxBuffEndIndx = 0;
    
    // Initialize INS stuff
    r-> newINSData = 0;
    r-> statusINSDisplaceX= 0;
    r-> statusINSDisplaceY= 0;
    r-> statusINSDisplaceRad= 0;
	return r;
}

/**
 * The destructor.
 */
void proteus_destroy(proteus_comm_t* r) {
	free(r);
}

/**
 * Counts the number of bytes in the buffer that needs to be escaped.
 *
 */
static inline uint16_t escapedSize(uint8_t* buff, size_t numBytes) {
	uint16_t i, result = 0;
	for (i = 2 /* The first two bytes are actual keys and should not be escaped*/; 
		i < numBytes-1 /* The last byte is also a key and should not be escaped*/; i++) 
	{
		if (buff[i] == PROTEUS_BEGIN || buff[i] == PROTEUS_END || buff[i] == PROTEUS_ESCAPE)
			result++;
	}
	return numBytes + result;
}

/**
 * Sends the data stored in the buffer to the micro-controller attached
 * to the serial port.
 * 
 * @param r The proteus_comm_t object containing a reference to the connection 
 * to the micro-controller.
 * @param buff A pointer to the bytes to send.
 * @param numBytes The number of bytes to send.
 * @return SUCCESS if successful, FAIL otherwise
 */
static inline result_t sendToMCU(proteus_comm_t* r, uint8_t* buff, size_t numBytes) {
	size_t eNumBytes = escapedSize(buff, numBytes); // recalculate numBytes with escape characters added
	if (r != NULL && r->fd >= 0) {
		uint16_t i, eIndx;
		uint8_t eBuff[eNumBytes]; // allocate memory for the escaped buffer
		size_t numBytesWritten = 0;
		#if DEBUG
		//printf("proteus_comms: sendToMCU: numBytes = %i, eNumbytes = %i\n", numBytes, eNumBytes);
		#endif
		eIndx = 2;
		eBuff[0] = buff[0]; // Should be PROTEUS_BEGIN
		eBuff[1] = buff[1]; // Should be an opcode
		for (i = 2; i < numBytes-1; i++) {
			
			// If the next character is special, escape it.
			if (buff[i] == PROTEUS_BEGIN || buff[i] == PROTEUS_END || buff[i] == PROTEUS_ESCAPE) {
				eBuff[eIndx++] = PROTEUS_ESCAPE;
				#if DEBUG
				//printf("proteus_comms: sendToMCU: adding escape byte at index %i\n", eIndx-1);
				#endif
			}
			eBuff[eIndx++] = buff[i];
		}
		eBuff[eNumBytes-1] = buff[numBytes-1]; // Should be PROTEUS_END
		
		// The write(...) method may not actually write all of the bytes on the first pass.
		// Thus, it is enclosed in a while loop.
		// See: http://www.gnu.org/s/libc/manual/html_node/I_002fO-Primitives.html
		while (numBytesWritten < eNumBytes) {
			size_t result = write(r->fd, eBuff + numBytesWritten, eNumBytes - numBytesWritten);
			if (result != -1) {
				numBytesWritten += result;
			} else {
				printf("proteus_comms: sendToMCU: ERROR: Failure to send data over serial port, errno = %i\n",
					errno);
				return FAIL;
			}
		}
		
		
		/*printf("proteus_comms: sendToMCU: Wrote the following bytes: ");
		for (i=0; i < eNumBytes; i++) {
			printf("0x%x ", eBuff[i]);
		}
		printf("\n");*/
			
		return SUCCESS;
	} else {
		printf("proteus_comms: sendToMCU: ERROR: r was null or r->fd < 0\n");
		return FAIL;
	}
}

/**
 * Open the serial connection to the micro-controller.
 * Called by proteus_driver.cc, method Proteus::Setup().
 *
 * @param r The proteus_comm_t object containing a reference to the connection 
 * to the micro-controller.
 * @return SUCCESS if successful, FAIL otherwise
 */
result_t proteus_open(proteus_comm_t* r) {
	struct termios term;
	int flags;
	
	if(r->fd >= 0) {
		printf("proteus_comms: proteus_open: proteus connection already open!\n");
		return SUCCESS;
	}
	
	printf("proteus_comms: proteus_open: opening connection to MCU on %s...\n", r->serial_port);
	
	/*
	 * Open the serial port.  Use non-blocking at first, in case there's no micro-controller attached.
	 */
	if((r->fd = open(r->serial_port, O_RDWR | O_NONBLOCK | O_FSYNC, S_IRUSR | S_IWUSR )) < 0 ) { 
		printf("proteus_comms: proteus_open: ERROR: failed to open port %s...\n", r->serial_port);
		return FAIL;
	}
	
	/*
	 * Flushes data that was received over the serial port but not yet read.
	 * See:  http://opengroup.org/onlinepubs/007908775/xsh/tcflush.html
	 */
	if(tcflush(r->fd, TCIFLUSH) < 0 ) {
		printf("proteus_comms: proteus_open: ERROR: unable to flush serial port.\n");
		close(r->fd);
		r->fd = -1;
		return FAIL;
	}
	
	/*
	 * Get the attribues associated with the serial port
	 * See: http://www.opengroup.org/onlinepubs/009695399/functions/tcgetattr.html
	 */
	if(tcgetattr(r->fd, &term) < 0 ) {
		printf("proteus_comms: proteus_open: ERROR: unable to get serial port attributes.\n");
		close(r->fd);
		r->fd = -1;
		return FAIL;
	}
	
	/*
	 * This function provides an easy way to set up *termios-p for what has traditionally been 
	 * called “raw mode” in BSD. This uses noncanonical input, and turns off most processing 
	 * to give an unmodified channel to the terminal.
	 *
	 * See: http://linux.die.net/man/3/cfmakeraw
	 * And: http://www.gnu.org/s/libc/manual/html_node/Noncanonical-Input.html 
	 */
	cfmakeraw(&term); 
	
	// Set 57600 baud
	cfsetispeed(&term, B57600); 
	cfsetospeed(&term, B57600);
	
	/*
	 * Apply the changes to the serial port.
	 * See: http://www.delorie.com/gnu/docs/glibc/libc_360.html
	 *
	 * TCSAFLUSH tells the underlying OS to wait until the all queued output has been written,
	 * and to discard any queued input
	 */
	if (tcsetattr(r->fd, TCSAFLUSH, &term) < 0 ) {
		printf("proteus_comms: proteus_open: ERROR: unable to set serial port attributes.\n");
		close(r->fd);
		r->fd = -1;
		return FAIL;
	}
	
	// replaced by heart beat
	/*
	 * Initialize the proteus robot by sending it a start message and configuring
	 * its mode of operation, i.e., safe or full.
	 */
	/*if(proteus_init(r, fullcontrol) < 0) {
		printf("proteus_comms: proteus_open: ERROR: failed to initialize connection.\n");
		close(r->fd);
		r->fd = -1;
		return FAIL;
	}*/
	
	/*
	if(proteus_get_sensors(r, 1000, PROTEUS_ODOMETRY) < 0) {
		PLAYER_MSG0(0, "proteus_open():failed to get data");
		close(r->fd);
		r->fd = -1;
		return FAIL;
	}*/
	
	/*
	 * By now we know the robot is there; switch to blocking.
	 */
//	if((flags = fcntl(r->fd, F_GETFL)) < 0) { // get the status flags of the serial port's file descriptor
//		PLAYER_MSG0(0, "proteus_open(): Get Flags");
//		close(r->fd);
//		r->fd = -1;
//		return FAIL;
//	}
//	
//	flags &= ~O_NONBLOCK; // unset the nonblock flag
//	
//	if(fcntl(r->fd, F_SETFL, flags/*flags ^ O_NONBLOCK*/) < 0) {
//		PLAYER_MSG0(0, "proteus_open(): Set Unblock");
//		close(r->fd);
//		r->fd = -1;
//		return FAIL;
//	}
	
	printf("...done!\n");
	return SUCCESS;
} // proteus_open(...)

/**
 * Sends a command to the micro-controller.  A command consists of
 * 3 bytes: PROTEUS_BEGIN, the opcode, and PROTEUS_END.
 *
 * @param r The proteus_comm_t object containing a reference to the connection 
 * to the micro-controller.
 * @param op The command's operation
 * @return SUCCESS if successful, FAIL otherwise
 */
static inline result_t sendOp(proteus_comm_t* r, uint8_t op) {
	uint8_t cmdbuf[3];
	
	if (r == NULL) {
		printf("proteus_comms: sendOp: ERROR: r is NULL.\n");
		return FAIL;
	}
	
	cmdbuf[0] = PROTEUS_BEGIN;
	cmdbuf[1] = op;
	cmdbuf[2] = PROTEUS_END;
	
	if (sendToMCU(r, cmdbuf, sizeof(cmdbuf)) == FAIL) {
		printf("proteus_comms: sendOp: ERROR: Failure to send op %i to micro-controller.\n", op);
		return FAIL;
	}
	
	return SUCCESS;
}

/**
 * Sends a heartbeat to the micro-controller.  This should be called
 * periodically at 1Hz.  It informs the micro-controller of the presence
 * of the x86.
 *
 * @param r The proteus_comm_t object containing a reference to the connection 
 * to the micro-controller.
 * @return SUCCESS if successful, FAIL otherwise
 */
result_t proteus_sendHeartBeat(proteus_comm_t* r) {
	//printf("proteus_comms: sendHeartBeat: sending heartbeat.\n");
	return sendOp(r, PROTEUS_OPCODE_HEARTBEAT);
}

/**
 * Closes the connection to the micro-controller.
 * Tells the robot to stop moving, and then issues a STOP command.
 * 
 * @param r The proteus_comm_t object containing a reference to the connection 
 * to the micro-controller.
 * @return SUCCESS if successful, FAIL otherwise
 */
result_t proteus_close(proteus_comm_t* r) {
	if (r != NULL) {
		printf("proteus_comm: proteus_close: Stopping the robot...\n");
		proteus_set_speeds(r, 0.0, 0.0);
		
		// No need to send STOP opcode since the lack of a heartbeat
		// indicates stop condition.
		/*if (sendOp(r, PROTEUS_OPCODE_STOP) == FAIL) {
			printf("proteus_comms.c: proteus_close: Failed to send stop command.\n");
			return FAIL;
		}*/
		
		printf("proteus_comm: Closing the connection to the micro-controller...\n");
		
		if(close(r->fd) < 0) {
			printf("proteus_comm: proteus_close: Problem closing serial port.\n");
			r->fd = -1;
			return FAIL;
		} else {
			r->fd = -1;
			return SUCCESS;
		}
	} else {
		// parameter r is already NULL, assume it was already closed
		return SUCCESS;
	}
}

/**
 * This is called by proteus_close and proteus_driver.cc, 
 * method Proteus::ProcessMessage(...).
 *
 * @param r The proteus_comm_t object containing a reference to the connection 
 * to the micro-controller.
 * @param velocity The wheel velocity (m/s)
 * @param angle The steering angle (rad)
 * @return SUCCESS if successful, FAIL otherwise
 */
result_t proteus_set_speeds(proteus_comm_t* r, double velocity, double angle) {
	if (r != NULL) {
		uint8_t cmdbuf[7];
		int16_t vel_16 = (int16_t)rint(velocity*100); //convert to cm/s and store as 16b
		int16_t ang_16 = (int16_t)rint(angle*10000);  //convert to .0001rad and store as 16b
		
		printf("proteus_comms: proteus_set_speeds: Sending speed command: %icm/s, angle=%i\n",
			vel_16, ang_16);
		
		cmdbuf[0] = PROTEUS_BEGIN;
		cmdbuf[1] = PROTEUS_OPCODE_DRIVE;
		cmdbuf[2] = (uint8_t)(vel_16 >> 8);
		cmdbuf[3] = (uint8_t)(vel_16 & 0xFF);
		cmdbuf[4] = (uint8_t)(ang_16 >> 8);
		cmdbuf[5] = (uint8_t)(ang_16 & 0xFF);
		cmdbuf[6] = PROTEUS_END;
		
		if (sendToMCU(r, cmdbuf, sizeof(cmdbuf)) == FAIL) {
			printf("proteus_comms.c: proteus_set_speeds: Failed to send speed command.\n");
			return FAIL;
		} else
			return SUCCESS;
	} else {
		printf("proteus_comms: proteus_set_speeds: ERROR: r was NULL\n");
		return FAIL; // r was null!
	}
}

/**
 * Calculates the next index into the serial rx buffer.
 */
uint16_t nextRxBuffIndx(uint16_t indx) {
	return (indx + 1) % SERIAL_RX_BUFFER_SIZE;
}

/**
 * Adds a new byte to the Rx serial data buffer.
 */
void addToRxSerialBuffer(proteus_comm_t* r, uint8_t data) {
	uint16_t nxtEndIndx = nextRxBuffIndx(r->rxBuffEndIndx);
	if (nxtEndIndx != r->rxBuffStartIndx) {
		r->serialRxBuffer[r->rxBuffEndIndx] = data; // save the data
		r->rxBuffEndIndx = nxtEndIndx; // advance the Rx buffer's end index
	} else {
		printf("proteus_comms: addToRxSerialBuffer: Rx buffer overflow!\n");
	}
}

/**
 * Returns the number of elements in the Rx serial buffer.
 */
int rxSerialBufferSize(proteus_comm_t* r) {
	if (r->rxBuffStartIndx <= r->rxBuffEndIndx) {
		return r->rxBuffEndIndx - r->rxBuffStartIndx;
	} else {
		return SERIAL_RX_BUFFER_SIZE - r->rxBuffStartIndx + r->rxBuffEndIndx;
	}
}

/**
 * Returns a byte of data from the rxSerialBuffer.
 *
 * @param offset the offset from the start of the buffer.  This value must be less
 * than the number of elements in the buffer.
 * @param data A pointer to where the data should be stored
 * @return SUCCESS if successful, FAIL otherwise
 */
result_t getRxSerialBuff(proteus_comm_t* r, int offset, uint8_t* data) {
	if (offset > rxSerialBufferSize(r)) {
		printf("proteus_comms: getRxSerialBuff: ERROR: tried to reach beyond end of buffer (size=%i, offset=%i)!\n",
			rxSerialBufferSize(r), offset);
		return FAIL;
	} else {
		 (*data) = r->serialRxBuffer[(r->rxBuffStartIndx + offset) % SERIAL_RX_BUFFER_SIZE];
		return SUCCESS;
	}
}

result_t popRxSerialBuff(proteus_comm_t* r, uint8_t* data) {
	if (rxSerialBufferSize(r) > 0) {
		if (data != NULL) {
			(*data) = r->serialRxBuffer[r->rxBuffStartIndx];
		}
		r->rxBuffStartIndx = nextRxBuffIndx(r->rxBuffStartIndx);
		return SUCCESS;
	} else {
		printf("proteus_comms: popRxSerialBuff: buffer underrun!\n");
		return FAIL;
	}
}

/**
 * Print the contents of the serial data Rx buffer.
 */
void printRxSerialBuff(proteus_comm_t* r) {
	int buffSize = rxSerialBufferSize(r);
	int i = r->rxBuffStartIndx;
	int j = 0;
	
	printf("-----------------------------------------------------------------------------\n");
	printf("SERIAL BUFFER STATE: size=%i, startIndx=%i, endIndx=%i, data:\n\t",
	 	rxSerialBufferSize(r), r->rxBuffStartIndx, r->rxBuffEndIndx);
	for (j = 0; j < buffSize; j++) {
		printf("0x%.2x ", r->serialRxBuffer[i]);
		if (j != 0 && j % 10 == 0)
			printf("\n\t");
		i = nextRxBuffIndx(i);
	}
	printf("\n-----------------------------------------------------------------------------\n");
}

result_t processOdometryPacket(proteus_comm_t* r) {
	if (rxSerialBufferSize(r) >= PROTEUS_ODOMETRY_PACKET_SIZE + PROTEUS_PACKET_OVERHEAD) {
		int16_t distance = 0;
		int16_t angle = 0;
		uint8_t data = 0; // temporary variable for holding data from the serial Rx buffer.
		
		popRxSerialBuff(r, NULL); // pop PROTEUS_BEGIN
		popRxSerialBuff(r, NULL); // pop message type
		
		// The first two bytes are the distance in big endian format
		popRxSerialBuff(r, &data);
		distance = (data << 8);
		popRxSerialBuff(r, &data);
		distance += (data & 0x00FF);
		//printf("proteus_comms.c: distance = %d\n", distance);
		r->distance = distance * .00128333; //distance is in units of .128333 cm or .00128333 mm
			
		// The second two bytes are the steering angle
		popRxSerialBuff(r, &data);
		angle = (data << 8);
		popRxSerialBuff(r, &data);
		angle += (data & 0x00FF);
		//printf("read> steering_ang: %d\n", signed_int);
		r->steering_angle = angle * .0001; //convert to radians and store
			
		// Byte 5 is whether the motor is stalled.
		popRxSerialBuff(r, &r->motor_stall);
		
		popRxSerialBuff(r, NULL); // pop PROTEUS_END
		
		r->newOdometryData = 1;
		
		//printf("proteus_comms: OdometryPacket: distance = %f, steering angle = %f, motor limited = %i\n",
			//	r->distance, r->steering_angle, r->motor_stall);
		
		return SUCCESS;
	} else {
		//printf("proteus_comms: processOdometryPacket: Insufficient data (have %i, need %i)\n", rxSerialBufferSize(),
		//	PROTEUS_ODOMETRY_PACKET_SIZE + PROTEUS_PACKET_OVERHEAD);
		//printRxSerialBuff();
		return FAIL;
	}
}

result_t processCompassPacket(proteus_comm_t* r) {
	timeval _currTime;
	if (rxSerialBufferSize(r) >= PROTEUS_COMPASS_PACKET_SIZE + PROTEUS_PACKET_OVERHEAD) {
		uint8_t data; // temporary variable for holding data from the serial Rx buffer.
		uint16_t heading = 0;
		float headingRadians;
		uint8_t dataType; // whether the data came from I2C or PWM data
		
		popRxSerialBuff(r, NULL); // pop PROTEUS_BEGIN
		popRxSerialBuff(r, NULL); // pop message type
		
		// The first byte is either I2C_DATA or PWM_DATA
		popRxSerialBuff(r, &dataType);
		
		// The next two bytes are the heading in big endian format
		popRxSerialBuff(r, &data);
		heading = ((data << 8) & 0xFF00);
		popRxSerialBuff(r, &data);
		heading += (data & 0x00FF);
		
		headingRadians = (heading * 2 * PI) / 3600.0; //convert to radians from .1 degrees  
		
		// clip rotation to +/-PI
		if (headingRadians > 2*PI) {
			// bad data, notify the x86 so it can be recorded
			sprintf((char*)r->messageBuffer, "ERROR: Invalid heading: %f", headingRadians);
			r->newMessage = 1;
		} else if (headingRadians > PI) { 
			r->compass_heading = headingRadians - 2*PI;
			r->newCompassData = 1;
		} else { 
			r->compass_heading = headingRadians;
			r->newCompassData = 1;
		}
		
		popRxSerialBuff(r, NULL); // pop PROTEUS_END
		
		//if (r->newCompassData) {
		//	printf("proteus_comms: CompassPacket: heading: %f radians, data type = %s\n",
		//		r->compass_heading, (dataType == I2C_DATA ? "I2C" : "PWM"));
		//}
		
		return SUCCESS;
	} else {
		return FAIL;  // else not enough data has arrived yet, wait till next time
	}
}

/**
 * This is sent by the MCU for debugging the tachometer.  See the MCU code, Tach.c.
 */
result_t processTachPacket(proteus_comm_t* r) {
	timeval _currTime;
	if (rxSerialBufferSize(r) >= PROTEUS_TACH_PACKET_SIZE + PROTEUS_PACKET_OVERHEAD) {
		int16_t tachSpeed = 0;
		int16_t targetSpeed = 0;
		uint8_t data; // temporary variable for holding data from the serial Rx buffer.
		
		popRxSerialBuff(r, NULL); // pop PROTEUS_BEGIN
		popRxSerialBuff(r, NULL); // pop message type
		
		// The first two bytes are the tach speed in big endian format
		popRxSerialBuff(r, &data);
		tachSpeed = (data << 8);
		popRxSerialBuff(r, &data);
		tachSpeed += data;
		
		// The second two bytes are the target speed in big endian format
		popRxSerialBuff(r, &data);
		targetSpeed = (data << 8);
		popRxSerialBuff(r, &data);
		targetSpeed += data;
		
		popRxSerialBuff(r, NULL); // pop PROTEUS_END
		
		printf("proteus_comms: TachPacket: tach speed = %i cm/s, target speed = %i cm/s\n", 
			tachSpeed, targetSpeed);
		return SUCCESS;
	} else {
		//printf("proteus_comms: processTachPacket: Insufficient data (have %i, need %i)\n", rxSerialBufferSize(),
		//	PROTEUS_TACH_PACKET_SIZE + PROTEUS_PACKET_OVERHEAD);
		//printRxSerialBuff();
		return FAIL;
	}
}

/**
 * This is sent periodically by the MCU to notify the x86 of its status.
 */
result_t processStatusPacket(proteus_comm_t* r) {
	timeval _currTime;
	if (rxSerialBufferSize(r) >= PROTEUS_STATUS_PACKET_SIZE + PROTEUS_PACKET_OVERHEAD) {
		uint8_t data; // temporary variable for holding data from the serial Rx buffer.
		
		popRxSerialBuff(r, NULL); // pop PROTEUS_BEGIN
		popRxSerialBuff(r, NULL); // pop message type
		
		// The first two bytes are the tach speed in big endian format
		popRxSerialBuff(r, &data);
		r->statusTachSpeed = (data << 8);
		popRxSerialBuff(r, &data);
		r->statusTachSpeed += data;
		
		// The second two bytes are the target speed in big endian format
		popRxSerialBuff(r, &data);
		r->statusTargetSpeed = (data << 8);
		popRxSerialBuff(r, &data);
		r->statusTargetSpeed += data;
		
		// Bytes 5 and 6 are the motor power in big endian format
		popRxSerialBuff(r, &data);
		r->statusMotorPower = (data << 8);
		popRxSerialBuff(r, &data);
		r->statusMotorPower += data;
		
		// Bytes 7 and 8 are the steering angle in big endian format, in units of 0.0001 radians
		popRxSerialBuff(r, &data);
		r->statusSteeringAngle = (data << 8);
		popRxSerialBuff(r, &data);
		r->statusSteeringAngle += data;
		
		popRxSerialBuff(r, NULL); // pop PROTEUS_END
		
		//sprintf(textMessage, "proteus_comms: StatusPacket: tach speed = %i cm/s, target speed = %i cm/s, motor power = %i, steering angle = %f\n", 
		//	tachSpeed, targetSpeed, motorPower, steeringAngle/1000.0);
		
		r->newStatusData = 1; // tells the proteus_driver that there is new status data to publish
		return SUCCESS;
	} else {
		//printf("proteus_comms: processStatusPacket: Insufficient data (have %i, need %i)\n", rxSerialBufferSize(),
		//	PROTEUS_STATUS_PACKET_SIZE + PROTEUS_PACKET_OVERHEAD);
		//printRxSerialBuff();
		return FAIL;
	}
}

/**
 * This is called whenever the MCU's motor controller reports a safety event.
 * See the MCU's code, file Command.c, method Command_sendMotorSafetyMsg.
 */
result_t processMotorSafetyPacket(proteus_comm_t* r) {
	if (rxSerialBufferSize(r) >= PROTEUS_MOTOR_SAFETY_PACKET_SIZE + PROTEUS_PACKET_OVERHEAD) {
		int16_t previousMotorPower;
		int16_t currentSpeed;
		uint8_t data; // temporary variable for holding data from the serial Rx buffer.
		
		popRxSerialBuff(r, NULL); // pop PROTEUS_BEGIN
		popRxSerialBuff(r, NULL); // pop message type
		
		// The first two bytes are the previous motor power in big endian format
		popRxSerialBuff(r, &data);
		previousMotorPower = (data << 8);
		popRxSerialBuff(r, &data);
		previousMotorPower += data;
		
		// The second two bytes are the current speed in big endian format
		popRxSerialBuff(r, &data);
		currentSpeed = (data << 8);
		popRxSerialBuff(r, &data);
		currentSpeed += data;
		
		popRxSerialBuff(r, NULL); // pop PROTEUS_END
		
		printf("proteus_comms: processMotorSafetyPacket: previous motor power = %i, current speed = %i cm/s\n", 
			previousMotorPower, currentSpeed);
		return SUCCESS;
	} else {
		//printf("proteus_comms: processStatusPacket: Insufficient data (have %i, need %i)\n", rxSerialBufferSize(),
		//	PROTEUS_STATUS_PACKET_SIZE + PROTEUS_PACKET_OVERHEAD);
		//printRxSerialBuff();
		return FAIL;
	}
}

/**
 * This is sent from the MCU for debugging purposes.
 */
result_t processTextMessagePacket(proteus_comm_t* r) {
	uint8_t strSize;
	getRxSerialBuff(r, 2, &strSize); // two byte after the begin message
	
	if (rxSerialBufferSize(r) >= 3 /* Make sure there is enough data for the string size */ 
		&& rxSerialBufferSize(r) >= strSize + PROTEUS_PACKET_OVERHEAD)
	{
		uint8_t data; // temporary variable for holding data from the serial Rx buffer.
		char textMessage[PROTEUS_MAX_TEXT_MESSAGE_LENGTH] = ""; // temporary buffer for storing the message
		uint16_t i;
		
		popRxSerialBuff(r, NULL); // pop PROTEUS_BEGIN
		popRxSerialBuff(r, NULL); // pop message type
		popRxSerialBuff(r, NULL); // pop message size
		
		for (i = 0; i < strSize; i++) {
			char tempStr[] = "";
			popRxSerialBuff(r, &data);
			//printf("popped character %c.\n", data);
			textMessage[strlen(textMessage)] = data;
		}
		textMessage[strlen(textMessage)] = '\0';
		
		printf("proteus_comms: MCU_MSG: StrSize: %i, Message: \"%s\"\n", strSize, textMessage);
		
		strcpy((char*)r->messageBuffer, textMessage);
		r->newMessage = 1; // indicate that a new message was received
		
		return SUCCESS;
	} else {
		printf("proteus_comms: processTextMessagePacket: Insufficient data (have %i, need %i)\n", rxSerialBufferSize(r),
			strSize + PROTEUS_PACKET_OVERHEAD);
		printRxSerialBuff(r);
		return FAIL;
	}
}


/**
 * Process the serial data was received from the MCU.
 * The data is stored in the serialRxBuffer.
 * Each call to this method processes one message.  If there is a possibility
 * that there are more messages in the buffer, SUCCESS is returned.
 * Otherwise, FAIL is returned.  Ideally, after reading data from the serial
 * port (by calling proteusReceiveSerialData), this method should be repeatedly
 * called until it returns FAIL.
 *
 * @param r The proteus_comm_t object containing a reference to the connection 
 * to the micro-controller.
 * @return SUCCESS if there may be more messages in the buffer, FAIL otherwise
 */
result_t proteusProcessRxData(proteus_comm_t* r) {
	//bool done = false;
	//int numPktsProcessed = 0;
	
	if (r == NULL) {
		printf("proteus_comms: proteusProcessRxData: ERROR: r = NULL\n");
		return FAIL;
	}
	
	// Remove extraneous bytes from the front the rxSerialBuffer.
	while (rxSerialBufferSize(r) > 0 && r->serialRxBuffer[r->rxBuffStartIndx] != PROTEUS_BEGIN) {
		//uint8_t garbageData; // The front of the serial Rx buffer is not a message header,
		popRxSerialBuff(r, NULL);// remove the extraneous byte
	}
	
	if (rxSerialBufferSize(r) < 3) {
		// Not enough space in rxSerialBuffer to hold a message.
		return FAIL;
	}
	#if DEBUG
	//printf("proteus_comms: proteusProcessRxData: processing received bytes...\n");
	#endif
	//printRxSerialBuff();
	
	//while (!done &&  /* Make sure there is at least enough bytes for a command header*/ 
	//	&& numPktsProcessed < 100 /* process up to 100 packets each time this method is invoked */) 
	//{
	
	if (r->serialRxBuffer[r->rxBuffStartIndx] == PROTEUS_BEGIN) {
		uint8_t msgType;
		getRxSerialBuff(r, 1, &msgType); // the message type is the byte after the begin message
		switch(msgType) {
			case PROTEUS_ODOMETRY_PACKET:
				printf("proteus_comms: odometry packet!\n");
				if (processOdometryPacket(r) == FAIL) return FAIL;
					//done = true; // no point in processing more packets from the MCU
				//numPktsPproteusReceiveSerialDatarocessed++;
				break;
			case PROTEUS_COMPASS_PACKET:
				printf("proteus_comms: compass packet!\n");
				if (processCompassPacket(r) == FAIL) return FAIL;
					//done = true; // no point in processing more packets from the MCU
				//numPktsProcessed++;
				break;
			case PROTEUS_TACHOMETER_PACKET:
				printf("proteus_comms: tachometer packet!\n");
				if (processTachPacket(r) == FAIL) return FAIL;
					//done = true; // no point in processing more packets from the MCU
				//numPktsProcessed++;
				break;
			case PROTEUS_STATUS_PACKET:
				printf("proteus_comms: processing status packet!\n");
				if (processStatusPacket(r) == FAIL) return FAIL;
					//done = true; // no point in processing more packets from the MCU
				//numPktsProcessed++;
				break;
			case PROTEUS_MOTOR_SAFETY_PACKET:
				printf("proteus_comms: motor safety packet!\n");
				if (processMotorSafetyPacket(r) == FAIL) return FAIL;
					//done = true; // no point in processing more packets from the MCU
				//numPktsProcessed++;
				break;
			case PROTEUS_TEXT_MESSAGE_PACKET:
				printf("proteus_comms: text message packet!\n");
				if (processTextMessagePacket(r) == FAIL) return FAIL;
					//done = true; // no point in processing more packets from the MCU
				//numPktsProcessed++;
				break;
			case PROTEUS_ACCELEROMETER_PACKET:
				//printf("proteus_comms: accelerometer compass packet!\n");
				if (processAccelerometerPacket(r) == FAIL) return FAIL;
				break;
			default:
				printf("proteus_comms: proteusProcessRxData: Unknown message type 0x%.2x\n", msgType);
				// The first byte does not constitute a valid message header.
				// Continuously remove bytes until a PROTEUS_END is found or there is nothing
				// left in the buffer.
				while (rxSerialBufferSize(r) > 0 && r->serialRxBuffer[r->rxBuffStartIndx] != PROTEUS_END) {
					popRxSerialBuff(r, NULL);
				}
				if (rxSerialBufferSize(r) != 0) {
					popRxSerialBuff(r, NULL); // remove final PROTEUS_END byte
					return SUCCESS;
				} else
					return FAIL;
		} // switch msg type
	} 
	else {
		uint8_t garbageData; // The front of the serial Rx buffer is not a message header,
		
		popRxSerialBuff(r, &garbageData);// remove the extraneous byte
		printf("proteus_comms: proteusProcessRxData: discarding junk byte 0x%.2x\n", garbageData);
	}
	// printf("Process return success\n");
	return SUCCESS;
}

/**
 * Reads some bytes from the serial port and store it in the RxSerialBuffer.
 *
 * @param r The proteus_comm_t object containing a reference to the MCU connection.
 * @return SUCCESS if successful, FAIL otherwise
 */
result_t proteusReceiveSerialData(proteus_comm_t* r) {
	struct pollfd ufd[1];
	int timeout = -1;
	uint8_t databuf[MAX_CMD_LEN];
	#if DEBUG
	//printf("Pt1\n");
	#endif
	if (r == NULL || r->fd < 0) {
		printf("proteus_comms: proteusReceiveSerialData: ERROR: r = NULL or r->fd < 0\n");
		return FAIL;
	}
	
	ufd[0].fd = r->fd;
	ufd[0].events = POLLIN; // data may be read without waiting
	
	int retval = poll(ufd,1,timeout); // see if there is data ready to be read from the serial port
	
	if(retval < 0) {
		printf("PTFail");
		if(errno == EINTR) {
			// The call was interrupted, ignore the error, we will try again later...
		} else if(errno == EINTR) {
			// Allocation of internal data structures failed, but the request may be attempted again.
			// Ignore the error, we will try again later...
		} else {
			printf("proteus_comms:  proteusReceiveSerialData: Problem w/ poll, errno = %i\n", errno);
			return FAIL;
		}
	} else if(retval == 0) {
		printf("proteus_comms: proteusReceiveSerialData: poll timeout\n");
		return FAIL;
	} else {
		#if DEBUG
		//printf("Pt2\n");
		#endif
		if (ufd[0].revents & POLLIN) {  // if serial port data is ready to be received
			int numread;
			if((numread = read(r->fd, databuf, MAX_CMD_LEN)) < 0) {
				printf("proteus_comms: proteusReceiveSerialData: problems while reading from serial port!\n");
				return FAIL;
			} else {
				int i;
				
				//printf("proteus_comms: proteusReceiveSerialData: Received the following bytes:\n\t");
				// save the received data
				for (i = 0; i < numread; i++) {
					addToRxSerialBuffer(r, databuf[i]);
				//	printf("0x%.2x ", databuf[i]);
				}
				//printf("\n");
				
				//printRxSerialBuff();
			}
		}
	}
	return SUCCESS;
}

#define DEG2RADIAN 3.14159265/180
// Added by Francis Rei Lao Israel for processing accelerometer packets.
result_t processAccelerometerPacket(proteus_comm_t* r) {
    uint8_t data;
    // Forward Displacement 
    signed short FDisp;
    float        FDispF;
    // Rotational Displacement
    signed short RDisp;
    float        RDispF;
	#if DEBUG
	//printf("INS DEBUG: Processing accelerometer packet.");
	#endif
	// Pull information from packet
	if (rxSerialBufferSize(r) >= PROTEUS_INS_PACKET_SIZE + PROTEUS_PACKET_OVERHEAD) {
		popRxSerialBuff(r, NULL); // pop PROTEUS_BEGIN
		popRxSerialBuff(r, NULL); // pop message type
		
		// Figure out tick number (so we can figure out speed later)
		
		// Pop Forward displacement
		popRxSerialBuff(r, &data);
		FDisp = ((data << 8) & 0xFF00);
		popRxSerialBuff(r, &data);
		FDisp += (data & 0x00FF);
		
		// Pop Gyro
		popRxSerialBuff(r, &data);
		RDisp = ((data << 8) & 0xFF00);
		popRxSerialBuff(r, &data);
		RDisp += (data & 0x00FF);
        
        popRxSerialBuff(r, NULL); // pop PROTEUS_END
	} else{
		return FAIL;
	}
    FDispF = (float)FDisp * .001;  // Go from .001 M  to 1 m
    RDispF = ((float)RDisp * .01) * DEG2RADIAN; // From 100ths of a degree to radians
    r -> statusINSDisplaceX += sin(RDispF) * FDispF;
    r -> statusINSDisplaceY += cos(RDispF) * FDispF;
    r -> statusINSVel = FDispF / .01;
    
    
    r->newINSData = 1;
    
	return SUCCESS;
}
