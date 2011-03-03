/**
 * A PID motor control for the Proteus Robot.
 *
 * Originally written by Nicholas Paine {n.a.paine@gmail.com}
 * Modified by Chien-Liang Fok <liangfok@mail.utexas.edu> on 11/28/2010.
 *  - Removed dependence on Scheduler
 *  - Added Enhanced Capture Timer Channel 3, to generate 20Hz interrupts 
 *    for periodically posting MotorControl_updateSpeed tasks.
 *  - Moved safe mode logic into this component. This uses Enhanced Capture Timer Channel 
 */
#include <hidef.h>           // common defines and macros
#include <mc9s12dp512.h>     // derivative information
#include "Types.h"
#include "TaskHandler.h"
#include "MotorControl.h"
#include "Tach.h"
#include "Servo_PWM.h"
#include "LED.h"


int16_t _targetSpeed = 0;  // units:  cm/sec
int16_t _prevError = 0;     // previous _velocityError

/**
 * The current power value that is being send to the motor.
 */
int16_t _currentMotorPower = 0;
int16_t _previousMotorPower = 0;

/**
 * This is set when the motor power gets too high or low.
 * It is used by Command.c when sending an odometry packet to the x86.
 */
bool _motorSpeedLimited = FALSE;

/**
 * Whether the motor was recently set.  This is used in safe mode to remember
 * whether a command was recently sent to the motor.
 */
bool _motorSafe = FALSE;

/**
 * Records the number of times interrupt 11 has occured.
 * This is for updating the motor's speed.
 */
uint16_t _int14Count = 0;

/**
 * Records the number of times interrupt 13 has occured.
 * This is for performing the safety check when running safe mode.
 */
uint16_t _int13Count = 0;

/**
 * Initializes the motor controller.
 * It uses the Enhanced Capture Timer (ECT) Channel 3, interrupt 11.
 */
void MotorControl_init(void) {
	
	// Configure the motor speed interupt (ECT Channel 3, interrupt 11)
	// TIOS |= TIOS_CH6_INT14_BIT;  // Set Enhanced Capture Timer Channel 3 to be output compare.  This is interrupt 11.
	// TIE |= TIE_CH6_INT14_BIT;   // Timer Interrupt Enable register, allow interrupt 11 to occur
	// TC6 = TCNT + 50; // first interrupt right away
	
	// Configure the safe mode interupt (ECT Channel 5, interupt 13)
	TIOS |= TIOS_CH5_INT13_BIT;  // Set Enhanced Capture Timer Channel 5 to be output compare.  This is interrupt 13.
	MotorControl_enableSafeMode(); // By default use safe mode
}

/**
 * Returns the current motor power being sent to the motor controller.
*/
int16_t MotorControl_getMotorPower() {
	return _currentMotorPower;
}

int16_t MotorControl_getTargetSpeed() {
	return _targetSpeed;
}

/**
 * Sets the desired velocity.
 * 
 * @param vel The velocity in cm/s.
 */
void MotorControl_setTargetSpeed(int16_t vel) { //cm/s
	_targetSpeed = vel;
	_motorSpeedLimited = FALSE;
	_motorSafe = TRUE; // Record the fact that we recently received a motor command
}

/**
 * The greater the error in actual speed vs. desired speed,
 * the greater the increment in the motor's power setting.
 */
int16_t controlledIncrement(int16_t velocityError) {
	
	// The speed is perfect!
	if (velocityError == 0)
		return 0;
	
	if (velocityError > 100)
		return 100;
	else if (velocityError > 80)
		return 75;
	else if (velocityError > 50)
		return 50;
	else if (velocityError > 25)
		return 25;
	else if (velocityError > 10)
		return 10;
	else if (velocityError > 5)
		return 4;
	else if (velocityError > 3)
		return 3;
	else if (velocityError > 2)
		return 2;
	else
		return 1;
}

/**
 * Calculates the power that should be sent to the motor-controller and
 * sends this value to the motor-controller.
 *
 * This should be executed at a frequency less than the rate at which the
 * tachometer updates its speed calculation, which is 10Hz.
 */
void MotorControl_updateSpeed(void) {
	if (_targetSpeed == 0) {
	  // Exponential decrease in speed
	  if (_currentMotorPower != 0) {
	    // since we cannot multiple by 0.8, we instead do the following
		  _currentMotorPower = (_currentMotorPower * 4) / 5;
	  }
		Motor_setSpeed(_currentMotorPower);
	} else {
		// All of these are in units of cm/s
		int16_t currentSpeed = Tach_getVelocity();
	
		// Calculate the error in velocity.  
		//   - Negative means we are going too fast,
		//   - Positive means we are going too slow.
		int16_t velocityError = _targetSpeed - currentSpeed;
	
		if (velocityError > 0) {
			_currentMotorPower += controlledIncrement(velocityError);
		} else {
			_currentMotorPower -= controlledIncrement(-velocityError);
		}
		
		// Do some sanity checks... see if the tach has a resonable value
		if (_previousMotorPower > 500 && currentSpeed < 5) {
			_currentMotorPower = 0;
			//LED_ORANGE2 ^= 1;
		}
		
		// clip the power to be within the max
		if(_currentMotorPower > MAX_SPEED) {
			_currentMotorPower = MAX_SPEED;
			_motorSpeedLimited = TRUE;
		} else if (_currentMotorPower < -MAX_SPEED) {
			_currentMotorPower = -MAX_SPEED;
			_motorSpeedLimited = TRUE;
		} else {
			_motorSpeedLimited = FALSE;
		}
		
		Motor_setSpeed(_currentMotorPower); // this is defined in Servo_PWM
		
		_previousMotorPower = _currentMotorPower;
	}
}

bool MotorControl_getMotorThrottled() {  
  return _motorSpeedLimited;
}

/**
 * Enable interrupt 13.  This results in safe mode operation.
 */
void MotorControl_enableSafeMode() {
	asm sei
	TIE |= TIE_CH5_INT13_BIT; // Timer Interrupt Enable Register, allow interrupt 13 (output compare 5) to occur
	TC5 = TCNT + 50; // first interrupt right away
	asm cli
}

/**
 * Disable interrupt 13.  This results in full mode operation (robot continues to run even
 * if motor command was not recently received).
 */
void MotorControl_disableSafeMode() {
	asm sei
	TIE &= ~TIE_CH5_INT13_BIT; // Timer Interrupt Enable Register, disallow interrupt 13 (output compare 5) to occur
	asm cli
}

/**
 * This method should only be called when the robot is operating in safe mode.
 * It checks whether a motor command was received within the last second.
 * If not, it submits a command that halts the motor.
 */
void MotorControl_doSafetyCheck() {
	//LED_GREEN1 ^= 1; // Toggle LED4 to indicate the micro-controller is performing a safety check.
	if(_motorSafe) {
		// A recent motor command was received and thus we are still safe.
		// Reset the _motorSafe variable.
		_motorSafe = FALSE;
	} else {
		//LED_ORANGE1 ^= 1; // Toggle LED7 to indicate motor being shutdown due to unsafe situation
		_targetSpeed = 0;
		_currentMotorPower = MOTOR_STOP;
		Motor_setSpeed(_currentMotorPower); // Stop the motor
		Servo_setSteeringAngle(SERVO_CENTER_CALIB); // straighten the front wheels
	}
}

/**
 * This interrupt periodically posts the MotorControl_updateSpeed task.
 *
 * It is called every 10ms. Since we want a 50ms period, we need to wait five
 * interrupts before posting the task.
 */
/*interrupt 14*/ void motorSpeedInterrupt(void) {
	
	// TFLG1 is the "Main Timer Interrupt Flag 1".  See the ECT Block Diagram Document.
	TFLG1 = TFLG1_CH6_INT14_BIT; // Acknowledge ECT Channel 6 interrupt
	
	_int14Count++;
	if (_int14Count == 10) { // 20ms * 10 = 200ms (5Hz)
		TaskHandler_postTask(&MotorControl_updateSpeed);
		_int14Count = 0;
	}
	
	TC6 = TCNT + TCNT_20MS_INTERVAL;
}

/**
 * This interrupt periodically posts the doSafetyCheck() task.
 * It occurs every 200ms (5Hz).
 */
interrupt 13 void safeModeInterrupt(void) {
	TFLG1 = TFLG1_CH5_INT13_BIT; // Acknowledge ECT Channel 5 interrupt

	_int13Count++;
	if (_int13Count == 100) { // 20ms * 100 = 2s (0.5Hz)
		TaskHandler_postTask(&MotorControl_doSafetyCheck);
		_int13Count = 0;
	}
	
	TC5 = TCNT + TCNT_20MS_INTERVAL;
}
