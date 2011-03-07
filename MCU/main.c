/**
 * Spins the motor at a particular speed.  An oscilloscope can then be attached
 * to channel A of the tachometer to measure the pulses its sending to the 
 * micro-controller.
 *
 * Author: Chien-Liang Fok <liangfok@mail.utexas.edu>
 * Last updated: 11/29/2010
 */

#include <hidef.h>           /* common defines and macros */
#include <mc9s12dp512.h>     /* derivative information */
#include "PLL.h"
#include "Timer.h"
#include "Types.h"
#include "LED.h"
#include "Command.h"
#include "SerialDriver.h"
#include "Servo_PWM.h"
#include "TaskHandler.h"
#include "Tach.h"
#include "Compass.h"

void main(void) {
	PLL_Init();   // Eclk @ 24MHz
	Timer_Init(); // TCNT @ 333.3ns, TOF @ 21.84ms
	LED_Init();
	
	TaskHandler_init();
	// Servo_init();
	// Tach_init();
	// Compass_init();
	SerialDriver_init(57600);//57600 //115200
	Command_init();
	INS_Init(); // in INS.h
	
	asm cli  // Enable interrupts
	
	for(;;) { // Foreground loop
		/*
		 * Multiple incoming bytes may be queued up.  Handle them. 
		 */
		SerialDriver_processRxBytes();
		
		/*
		 * Only one instance of each task can exist at a single
		 * point in time in the TaskHandler.
		 */
		TaskHandler_processNextTask();
		INSPeriodicFG();
	} 
}