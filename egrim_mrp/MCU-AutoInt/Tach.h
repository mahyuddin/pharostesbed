// filename ************** Tach.h **************
// Speed measurement for the Proteus Robot Project
// Written by Paine {n.a.paine@gmail.com}
// Last modified 1/15/10

#ifndef _TACH_H
#define _TACH_H 1

#include "Types.h"

/* Pin connections */
// PP1 PWM output to motor, interfaced in the Servo_PWM file
// PT1 is encoder channel B
// PT0 is encoder channel A   (channels may be reversed)
// Input capture 0, and TOF interrupts are employed
// Motor employs a PWM signal connected to the analog input
// http://www.robot-electronics.co.uk/htm/md03tech.htm
// The motor controllers require about 150us every 109ms
// plus about 5us for every encoder pulse


// The distance traveled is 0.12833 cm per count, 
// To redesign the encoder software, see TachometerDesign.xls
// The tires are 35 cm in circumference with weight of a laptop
// There are 100 slots in the encoder, and a 22/60 gearbox
// 35 cm circumference/100 slots*(22/60 gearbox) = 0.12833 cm per count



#define TACH_RESET_HZ 10 //check if we're stopped every second
                //may need to increase for controller response
                
#define TACH_FILTER_LEN 9

// ************Tach_Init**************************
// Activate Input capture 0
// PT1, PT0 are encoder inputs
// Input:  none
// Output: none
// Errors: assumes TCNT active at 3 MHz
void Tach_init(void);

/**
 * Returns velocity in +-cm/s.
 */
int16_t Tach_getVelocity(void);

/**
 * Returns the distance traveled since this last time this
 * method was called.  The value returned is in units of number of
 * rising pulse edges.
 *
 * TODO: Determine what physical distance this corresponds to.
 */
int16_t Tach_getDistance(void);

#endif //_TACH_H