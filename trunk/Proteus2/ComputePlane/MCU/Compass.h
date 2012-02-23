// filename ******** Compass.h **************
// A compass driver for the Proteus Robot
// Written for Robot-Electronic's CMPS03 compass

#ifndef _COMPASS_H
#define _COMPASS_H 1

#include "Types.h"

//compass driver for Proteus Robot

#define COMPASS_ADDRESS 0xC0
#define COMPASS_REGISTER_ADDRESS 0x02

/**
 * Initializes the compass.
 */
void Compass_init(void);

/**
 * Initiates the process of obtaining the next compass reading.
 * This is an asynchronous process.  When the compass reading is ready,
 * the I2CDriver will call Compass_headingReady(...), which is defined
 * below.
 */
void Compass_getHeading(void);

/**
 * This is scheduled to be called by I2CDriver after it finishes obtaining
 * a compass sensor reading.
 */
void Compass_headingReady();

/**
 * Called by the TaskHandler whenever a PWM calculation needs to be
 * performed.
 */
void Compass_doPWMCalc(void);

/**
 * Called by I2CDriver whenever an I2C read operation failed.
 */
void Compass_I2C_Read_Failed(void);

#endif /* _COMPASS_H */