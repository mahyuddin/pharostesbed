/**
 * A compass driver for Robot-Electronic's CMPS03 compass
 * 
 * Originally written by Chien-Liang Fok on 12/01/2010.
 * Updated by Chien-Liang Fok on 09/19/2011.  Modified to
 * support generic I2C driver.
 */

#include <mc9s12dp512.h>
#include "Compass.h"
#include "LED.h"
#include "Types.h"
#include "I2CDriver.h"
#include "Command.h"
#include "TaskHandler.h"
#include <stdio.h>

uint16_t heading;

/**
 * Initializes the compass.
 */
void Compass_init(){
}

/**
 * Initiates the process of obtaining the next compass reading.
 * This is an asynchronous process.  When the compass reading is ready,
 * the I2CDriver will call Compass_headingReady(...), which is defined
 * below.
 */
void Compass_getHeading() {
	uint16_t numBytes = 2;
	if (I2CDriver_read(COMPASS_ADDRESS, COMPASS_REGISTER_ADDRESS, numBytes, (uint8_t*)(&heading), &Compass_headingReady) == FALSE) {
	  // I commented out the following line to prevent flooding the serial link to the x86
	  // and causing high latency.  Every 10 failures, the I2C driver will send a "busy" message.  
		//Command_sendMessagePacket("ERROR: Compass: I2C read init failed!");
	} 
	//else {
	  //Command_sendMessagePacket("Compass: I2C rcving.");
	//}
}

/**
 * Called by I2CDriver whenever an I2C read operation failed.
 */
//void Compass_I2C_Read_Failed() {
//	Command_sendMessagePacket("ERROR: Compass: I2C read failed!");
//}


/**
 * This is called by I2CDriver after it finishes obtaining
 * a compass sensor reading.
 * 
 * @param heading The most recent heading measurement from the compass
 */
void Compass_headingReady() {
	Command_sendCompassPacket(I2C_DATA, heading);
}
