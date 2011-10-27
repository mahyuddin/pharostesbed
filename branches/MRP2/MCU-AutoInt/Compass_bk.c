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

bool _pwmEnabled = FALSE;

uint16_t _startCNT, _endCNT;

/**
 * The number of times interrupt 15 occured during
 * a pulse width.
 */
uint16_t _compassCNT = 0;

/**
 * Remembers whether the timing of a pulse width has started.
 */
bool _timerArmed = FALSE;

/**
 * Remembers whether for foreground process has finished performing the heading
 * calculation.
 */
bool _doCalc = FALSE;

/**
 * Initializes the compass.  Since the compass is connected to
 * the I2C bus, it initializes the I2DDriver.
 */
void Compass_init(){
	
	// Initialize the ECT Channel 3 for receiving compass PWM signal
	TIOS &= ~TIOS_CH3_INT11_BIT;  // Set ECT Channel 3 to be input compare.  This is interrupt 11.
	DDRT &= ~TIOS_CH3_INT11_BIT;  // Set ECT Channel 3 to be an input
	TCTL4 |= 0xC0;        // EDG3A = 1, EDG3B = 1 capture on rising/ralling edge of PT3
	
	// Initialize the ECT Channel 7 for timing the pulse width
	TIOS |= TIOS_CH7_INT15_BIT;  // Set Enhanced Capture Timer Channel 7 to be output compare.  This is interrupt 11.
	
	I2CDriver_init();
}

/**
 * Enable heading calculation based on a PWM signal.
 */
void Compass_enablePWM() {
	if (!_pwmEnabled) {
		_timerArmed = FALSE;
		_doCalc = FALSE;
		
		// reset the value of _compassCNT to prevent overflow conditions from ever occuring
		_compassCNT = 0;
		
		// turn on the timer that is used to calculate the width of the compass PWM pulse
		TIE |= TIE_CH7_INT15_BIT;   // Timer Interrupt Enable register, allow interrupt 15 to occur
		TC7 = TCNT + 5; // first interrupt right away
		
		// Allow interrupt 11 to occur.  This is the interrupt that occurs
		// with each rising or lowering edge of the compass PWM pulse.
		//TIE |= TIE_CH3_INT11_BIT;   
		
		_pwmEnabled = TRUE;
	}
}

/**
 * Disable the heading calculation based on a PWM signal.
 */
void Compass_disablePWM() {
	if (_pwmEnabled) {
		TIE &= ~TIE_CH3_INT11_BIT; // disable PWM interrupts
		TIE &= ~TIE_CH7_INT15_BIT; // disable PWM timer interrupts
		
		_pwmEnabled = FALSE;
		_timerArmed = FALSE;
		_doCalc = FALSE;
	}
}

/**
 * Initiates the process of obtaining the next compass reading.
 * This is an asynchronous process.  When the compass reading is ready,
 * the I2CDriver will call Compass_headingReady(...), which is defined
 * below.
 */
void Compass_getHeading() {
	//if (I2CDriver_readCompass() == FALSE) {
		//Command_sendMessagePacket("Compass: I2C read init failed, using PWM.");
		//Compass_enablePWM();
	//}

	// debug the PWM code
	Compass_enablePWM();
}

/**
 * Called by I2CDriver whenever an I2C read operation failed.
 */
void Compass_I2C_Read_Failed(void) {
	Command_sendMessagePacket("Compass: I2C read failed, using PWM.");
	Compass_enablePWM();
}


/**
 * This is called by I2CDriver after it finishes obtaining
 * a compass sensor reading.
 * 
 * @param heading The most recent heading measurement from the compass
 */
void Compass_headingReady(uint16_t heading) {
	if (_pwmEnabled) {
		// The I2C actually worked, disable PWM computation
		Compass_disablePWM();
	}
	Command_sendCompassPacket(I2C_DATA, heading);
}

/**
 * Calculates the heading based on the width of a PWM signal.
 * It relies on variables _startCNT, _endCNT, and _doCalc.
 */
void Compass_doPWMCalc() {
	// This is the number of 0.1ms ticks that occured during the width of a pulse
	uint16_t deltaCNT = _endCNT - _startCNT;
	uint16_t heading;
	
	// According to the compass' manual, every degree is 100us with zero degrees being 1ms.
	//
	// deltaCNT * 100 = number of μs that have passed
	// (deltaCNT * 100) - 1000 = number of μs normalized to 0ns = 0 degrees
	// ((deltaCNT * 100) - 1000) / 100 = number of degrees (0-360)
	// ((deltaCNT * 100) - 1000) / 10 = number of 0.1 degrees (0-3600)
	heading = ((deltaCNT * 100) - 1000) / 10;
	Command_sendCompassPacket(PWM_DATA, heading);
	_doCalc = FALSE;
}

/**
 * This is signaled on the rising and lowering edge of the compass PWM signal
 */
interrupt 11 void CompassPWM(void) {
	TFLG1 = TFLG1_CH3_INT11_BIT; // acknowledge ECT, Channel 3

	if (!_doCalc) {
		if (PTT_PTT3) {
			_startCNT = _compassCNT;
			_timerArmed = TRUE;
		} else {
			// This is a falling edge...
			if (_timerArmed) {
				_endCNT = _compassCNT;
				_doCalc = TRUE;
				TaskHandler_postTask(&Compass_doPWMCalc);
				
				// The width of the pulse has now been measured, disable
				// both the timer interupt and the pulse interrupt
				Compass_disablePWM();
			}
		}
	}  // else this component is still busy calculating the previous peak (ignore new interrupt)
}

/**
 * This interrupt increments the _compassTCNT variable every 0.1ms.  This is used to
 * calculate the width of a pulse from the compass.
 *
 * _compassCNT overflows 0xFFFF * 0.1ms = 6553.5ms = 6.5s, which is more than the maximum width of
 * a compass PWM pulse (36.99ms).
 *
 */
interrupt 15 void PWMTimer(void) {
	
	// TFLG1 is the "Main Timer Interrupt Flag 1".  See the ECT Block Diagram Document.
	TFLG1 = TFLG1_CH7_INT15_BIT; // Acknowledge ECT Channel 7 interrupt
	
	// Since the PWMTimer is now running, enable the interrupt that occurs
	// on the rising and lowering edge of the compass PWM signal.
	//
	// Allow interrupt 11 to occur.  This is the interrupt that occurs
	// with each rising or lowering edge of the compass PWM pulse.
	TIE |= TIE_CH3_INT11_BIT;
	
	_compassCNT++;
	//_timerReady = TRUE;
	
	TC7 = TCNT + TCNT_100US_INTERVAL;
}

