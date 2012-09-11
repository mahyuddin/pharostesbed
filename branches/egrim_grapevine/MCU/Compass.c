/**
 * A compass driver for Robot-Electronic's CMPS03 compass
 * 
 * Originally written by Chien-Liang Fok on 12/01/2010
 */

#include <mc9s12dp512.h>
#include "Compass.h"
#include "LED.h"
#include "Types.h"
#include "I2CDriver.h"
#include "Command.h"
#include "TaskHandler.h"
#include <stdio.h>

//bool _pwmEnabled = FALSE;

/**
 * A counter that keeps track of the number of times the timer 
 * has overflowed.
 */
//uint16_t _counterTOF = 0;
//uint16_t _offsetTCNT = 0;
//uint16_t _heading=0; //absolute heading in .1 degrees (359.9 (deg) = 3599 (.1 deg))

//bool _doCalc = FALSE; // Are we in the midst of calculating the pulse width?
//bool _timing = FALSE; // Are we in the midst of measuring the pulse width?

//uint16_t _startTCNT; // The value of TCNT when the rising edge occurs
//uint16_t _startCounterTOF; // The value of the overflow counter when the rising edge occurs
//uint16_t _endTCNT; // The final value of TCNT when the falling edge occurs
//uint16_t _endCounterTOF;  // The final value of the TOF when the falling edge occurs

/**
 * This method is called at the same frequency that timer TCNT
 * overflows from 0xFFFF to 0.  The TCNT timer is 16 bit and is updated every 333ns,
 * meaning it overflows every 0.333us * 0xFFFF (65535) = 21823us, or 21.823ms.
 * Note that it is not called synchronously with TCNT overflowing, 
 * meaning TCNT may not have overflowed at the precise time this 
 * method is called.
 */
/*void incTOF(){
	// TOF_counter keeps track of the number of times TCNT overflows.
	// Since it is a 16-bit value and TCNT is updated every 21.823ms,
	// TOF_counter itself will overflow every 21.823ms * 65535 = 1430.17s
	// = 23.836 minutes.
	_counterTOF++;
	
	// Record the offset between this function being called TCNT actually
	// overflowing
	_offsetTCNT = TCNT;
}*/

/**
 * Remembers whether the timing of a pulse width has started.
 */
//bool _timerArmed = FALSE;

/**
 * Remembers whether for foreground process has finished performing the heading
 * calculation.
 */
//bool _doCalc = FALSE;

/**
 * Initializes the compass.  Since the compass is connected to
 * the I2C bus, it initializes the I2DDriver.
 */
void Compass_init(){
	
	// Initialize the ECT Channel 3 for receiving compass PWM signal
//	TIOS &= ~TIOS_CH3_INT11_BIT;  // Set ECT Channel 3 to be input compare.  This is interrupt 11.
//	DDRT &= ~TIOS_CH3_INT11_BIT;  // Set ECT Channel 3 to be an input
//	TCTL4 |= 0xC0;        // EDG3A = 1, EDG3B = 1 capture on rising/ralling edge of PT3
	
	// Initialize the ECT Channel 7 for timing the pulse width
//	TIOS |= TIOS_CH7_INT15_BIT;  // Set Enhanced Capture Timer Channel 7 to be output compare.  This is interrupt 11.
//	TIE |= TIE_CH7_INT15_BIT;   // Timer Interrupt Enable register, allow interrupt 15 to occur
//	TC7 = TCNT + 5; // first interrupt right away
	
	I2CDriver_init();
}

/**
 * Enable heading calculation based on a PWM signal.
 */
/*void Compass_enablePWM() {
	if (!_pwmEnabled) {
		_timerArmed = FALSE;
		//_doCalc = FALSE;
		
		// reset the value of _counterTOF to prevent overflow conditions from ever occuring
		//_counterTOF = 0;
		
		// turn on the timer that is used to calculate the width of the compass PWM pulse
		//TIE |= TIE_CH7_INT15_BIT;   // Timer Interrupt Enable register, allow interrupt 15 to occur
		//TC7 = TCNT + 5; // first interrupt right away
		
		// Allow interrupt 11 to occur.  This is the interrupt that occurs
		// with each rising or lowering edge of the compass PWM pulse.
		TIE |= TIE_CH3_INT11_BIT;
		
		_pwmEnabled = TRUE;
	}
}*/

/**
 * Disable the heading calculation based on a PWM signal.
 */
/*void Compass_disablePWM() {
	if (_pwmEnabled) {
		TIE &= ~TIE_CH3_INT11_BIT; // disable PWM interrupts
		//TIE &= ~TIE_CH7_INT15_BIT; // disable PWM timer interrupts
		
		_pwmEnabled = FALSE;
		_timerArmed = FALSE;
		//_doCalc = FALSE;
	}
}*/

/**
 * Initiates the process of obtaining the next compass reading.
 * This is an asynchronous process.  When the compass reading is ready,
 * the I2CDriver will call Compass_headingReady(...), which is defined
 * below.
 */
void Compass_getHeading() {
	if (I2CDriver_readCompass() == FALSE) {
		Command_sendMessagePacket("ERROR: Compass: I2C read init failed!");
		//Compass_enablePWM();
	}

	// debug the PWM code
	//Compass_enablePWM();
}

/**
 * Called by I2CDriver whenever an I2C read operation failed.
 */
void Compass_I2C_Read_Failed() {
	Command_sendMessagePacket("ERROR: Compass: I2C read failed!");
	//Command_sendMessagePacket("Compass: I2C read failed, using PWM.");
	//Compass_enablePWM();
}


/**
 * This is called by I2CDriver after it finishes obtaining
 * a compass sensor reading.
 * 
 * @param heading The most recent heading measurement from the compass
 */
void Compass_headingReady(uint16_t heading) {
	//if (_pwmEnabled) {
		// The I2C actually worked, disable PWM computation
	//	Compass_disablePWM();
	//}
	
	/*{ // for debugging
		char message[100];
		if (sprintf(message, "Compass_headingReady: heading = %i", heading) > 0)
			Command_sendMessagePacket(message);
	}*/
	
	Command_sendCompassPacket(I2C_DATA, heading);
}

/**
 * Calculates the difference between t1 and t2.  It is assumed
 * that t1 occured before t2, and that the time did not exceed
 * a single wrap-around, i.e., the difference did not exceed 
 * 0xffff units.
 * 
 * @param t1 The start time.
 * @param t2 The end time
 * @return The difference between t1 and t2.
 */
/*uint16_t calcDelta(uint16_t t1, uint16_t t2) {
	uint16_t result;
	if (t2 > t1) {
		// there was no wrap-around
		result = t2-t1;
	} else {
		// there was a wrap around
		result = (0xffff - t1 + 1) + t2;
	}
	return result;
}*/

/**
 * Calculates the heading based on the width of a PWM signal.
 * It relies on variables _startCNT, _endCNT, and _doCalc.
 */
/*void Compass_doPWMCalc() {
	uint32_t deltaTCNT;
	//uint16_t numOverflows;
	uint16_t heading;
	
	if (_endCounterTOF == _startCounterTOF) {
		// It is guaranteed that the pulse width is less than 0xffff TCNT ticks.
		deltaTCNT = calcDelta(_startTCNT, _endTCNT);
	} else {
		uint16_t t1;
		uint32_t t2;
		uint16_t t3;
		
		// Calculate t1, the time between the positive pulse and the incTOF() event
		t1 = calcDelta(_startTCNT, _offsetTCNT);
		
		// Calculate t2, the number of times TCNT wrapped around
		t2 = 0xffff * (uint32_t)(calcDelta(_startCounterTOF, _endCounterTOF) - 1);
		
		// Calculate t3, the time between the incTOF() event and the negative edge of the pulse
		t3 = calcDelta(_offsetTCNT, _endTCNT);
		
		// Sum up t1, t2, and t3 to get the total deltaTCNT
		deltaTCNT = (uint32_t)t1 + t2 + (uint32_t)t3;
	}

	// deltaTCNT is the number of 'ticks' that occurred between the rising and falling
	// edge of a pulse from the compass.  Each tick is 333ns.  According to the compass'
	// manual, every degree is 100us with zero degrees being 1ms.
	
	// (deltaCNT * 333) == number of nanoseconds that have passed
	// (deltaCNT * 333) - 1000000 == number of nano seconds normalized to 0ns = 0 degrees
	// ((deltaCNT * 333) - 1000000) / 100000 = number of degrees (0-360)
	// ((deltaCNT * 333) - 1000000) / 10000 = number of 0.1 degrees (0-3600)
	
	// Sanity check:  suppose the correct measurement is 180 degrees
	// In this case, the period is 1ms + 180 * 0.1ms = 19ms.
	// Assuming deltaTCNT was calculated correctly, deltaTCNT = 19ms/333ns/tick = 57057 ticks
	// Plugging this in the equation:
	// ((57057*333) - 1000000) / 10000 = 1799.998 tenth degrees ~ 180 degrees
	heading = ((deltaTCNT * 333) - 1000000) / 10000;
	//_heading = (deltaTCNT - 3000)/30;
	Command_sendCompassPacket(PWM_DATA, heading);
	
	_doCalc = FALSE;
}*/

/**
 * This is signaled on the rising and lowering edge of the compass PWM signal
 */
/*interrupt 11 void CompassPWM(void) {
	TFLG1 = TFLG1_CH3_INT11_BIT; // acknowledge ECT, Channel 3

	if (!_doCalc) {
		if (PTT_PTT3) {
			_startTCNT = TCNT;
			_startCounterTOF = _counterTOF;
			_timerArmed = TRUE;
		} else {
			// This is a falling edge...
			if (_timerArmed) {
				_endTCNT = TCNT;
				_endCounterTOF = _counterTOF;
				_doCalc = TRUE;
				TaskHandler_postTask(&Compass_doPWMCalc);
				
				// The width of the pulse has now been measured, disable
				// both the timer interupt and the pulse interrupt
				Compass_disablePWM();
			}
		}
	}  // else this component is still busy calculating the previous peak (ignore new interrupt)
}*/

/**
 * This interrupt at the same frequency that TCNT overflows.
 *
 */
/*interrupt 15 void PWMTimer(void) {
	
	// TFLG1 is the "Main Timer Interrupt Flag 1".  See the ECT Block Diagram Document.
	TFLG1 = TFLG1_CH7_INT15_BIT; // Acknowledge ECT Channel 7 interrupt
	
	incTOF();
	
	// Since the PWMTimer is now running, enable the interrupt that occurs
	// on the rising and lowering edge of the compass PWM signal.
	//
	// Allow interrupt 11 to occur.  This is the interrupt that occurs
	// with each rising or lowering edge of the compass PWM pulse.
	//TIE |= TIE_CH3_INT11_BIT;
	
	//_counterTOF++;
	//_timerReady = TRUE;
	
	TC7 = TCNT + 0xFFFF;
}*/

