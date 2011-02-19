/**
 * Servo driver for HITEC servos and 9S12DP512s.
 *
 * Written by Nicholas Paine
 * Last modified 3/27/08
 * Modified by Valvano 7/24/08 making Channel 1 94.1kHz
 * Modified by Chien-Liang Fok <liangfok@mail.utexas.edu> on 11/28/2010.
 *  - Removed dependence on Scheduler
 *  - Added Enhanced Capture Timer, Channel 2, to generate 20Hz interrupts 
 *    for setting _setMotor to be TRUE.
 * Modified by Chien-Liang Fok <liangfok@mail.utexas.edu> on 02/09/2011.
 *  - Added method Servo_calcNewSetPoint that simplifies the code within
 *    Servo_updatePosition.
 * Modified by Chien-Liang Fok <liangfok@mail.utexas.edu> on 02/19/2011.
 *  - Changed motor control (Servo 1) to be a second steering servo for tilting
 *    the camera.
 */

#include <mc9s12dp512.h>     /* derivative information */
#include "Servo_PWM.h"
#include "LED.h"
#include "Types.h"
#include "TaskHandler.h"
#include "Command.h"
#include <stdio.h>

uint8_t ActualSetpoint1 = 128, ActualSetpoint2 = 128, ActualSetpoint3 = 128, ActualSetpoint4 = 128;
uint8_t TargetSetpoint1 = 128, TargetSetpoint2 = 128, TargetSetpoint3 = 128, TargetSetpoint4 = 128;
uint8_t MaxChangePerPeriod = 6;

bool Servo1Enabled, Servo2Enabled, Servo3Enabled, Servo4Enabled;

/**
 * Enable PWM channels 0-7.
 * Inputs: none
 * Outputs: none
 * Concatenates 2/3, 4/5, 6/7
 */
void Servo_init() {
	DDRT |= 0x04;     // set directional bit PT2--output
	PWME = 0x00;      // disable channels 0-7
	PWMPOL |= 0xFF;   // PP0-7 high then low
	
	//PWMCLK |= 0xFC;   // use clock SA,SB on channels 7-2 (1111 1100)
	//PWMCLK &= ~0x02;  // use clock A on channel1 (motor) (1111 1101)
	PWMCLK |= 0xFF;   // use clock SA or SB on PWM channels 0-7
	
	PWMPRCLK = 0x00;  // A,B = ECLK = 24MHz
	PWMCAE = 0x00;    // all left aligned
	PWMCTL = 0xF0;    // 4 16-bit channels
	PWMSCLA = 12;     // 1MHz SA clk
	PWMSCLB = 12;     // 1MHz SB clk
	
	PWMPER01 = SERVO_PULSE_PERIOD;
	PWMPER23 = SERVO_PULSE_PERIOD;  
	PWMPER45 = SERVO_PULSE_PERIOD;  
	PWMPER67 = SERVO_PULSE_PERIOD;
	
	_Servo1_set(128+SERVO_CENTER_CALIB); // camera tilt
	_Servo2_set(128+SERVO_CENTER_CALIB); // camera pan
	_Servo3_set(128);
	_Servo4_set(128); 
	
	PWME = 0xFF;       // enable channels 0-7
	Servo1Enabled = 1;
	Servo2Enabled = 1;
	Servo3Enabled = 1;
	Servo4Enabled = 1;
	
	// Start the 50Hz periodic interrupts
	TIOS |= 0x04;  // activate TC3 as output compare
	TIE |= 0x04;   // imer Interrupt Enable Register, allow interrupt 10 to occur
	TC2 = TCNT+50; // first interrupt right away
}

/**
 * Compares the actual set point to the target set point, and adjusts the
 * actual set point closer to the target set point.  The maximum amount
 * of change is MaxChangePerPeriod.
 *
 * @param actualSP The actual set point.
 * @param targetSP The target set point.
 * @return The new value of the actual set point.
 */
uint8_t Servo_calcNewSetPoint(uint8_t actualSP, uint8_t targetSP) {
	if(actualSP - MaxChangePerPeriod > targetSP) 
		// Target is lower than actual minus the max change per period
		return actualSP - MaxChangePerPeriod;
	else if(actualSP + MaxChangePerPeriod < targetSP) 
		// Target is higher than actual plus the max change per period
		return actualSP + MaxChangePerPeriod;
	else
		// The change is smaller than the max change per period.  Just jump to the target value.
		return targetSP;
}

/**
 * Each time an interrupt occurs, update the servo positions.
 * This should be executed at 50Hz.
 */
void Servo_updatePosition(void) {
	LED_BLUE1 ^= 1;
	
	ActualSetpoint1 = Servo_calcNewSetPoint(ActualSetpoint1, TargetSetpoint1);
	ActualSetpoint2 = Servo_calcNewSetPoint(ActualSetpoint2, TargetSetpoint2);
	ActualSetpoint3 = Servo_calcNewSetPoint(ActualSetpoint3, TargetSetpoint3);
	ActualSetpoint4 = Servo_calcNewSetPoint(ActualSetpoint4, TargetSetpoint4);
	
	_Servo1_set(ActualSetpoint1);
	_Servo2_set(ActualSetpoint2);   //128 straight
	_Servo3_set(ActualSetpoint3);
	_Servo4_set(ActualSetpoint4);
}

void Servo1_disable() {
  PWME &= ~0x02; // disable channel 1
  Servo1Enabled = 0; 
}

void Servo1_enable() {
  PWME |= 0x02; // enable channel 1
  Servo1Enabled = 1; 
}

void Servo2_disable() {
  PWME &= ~0x08; //disable channel 3
  Servo2Enabled = 0; 
}

void Servo2_enable() {
  PWME |= 0x08; //enable channel 3
  Servo2Enabled = 1; 
}

void Servo3_disable() {
  PWME &= ~0x20; //disable channel 5
  Servo3Enabled = 0; 
}

void Servo3_enable() {
  PWME |= 0x20; //enable channel 5
  Servo3Enabled = 1; 
}

void Servo4_disable() {
  PWME &= ~0x80; //disable channel 7
  Servo4Enabled = 0; 
}

void Servo4_enable() {
  PWME |= 0x80; //enable channel 7
  Servo4Enabled = 1; 
}

uint8_t Servo1_status() {
	return Servo1Enabled;
}

uint8_t Servo2_status() {
	return Servo2Enabled;
}

uint8_t Servo3_status() {
	return Servo3Enabled;
}

uint8_t Servo4_status() {
	return Servo4Enabled;
}

void _Servo1_set(uint8_t setpoint){   // Camera tilt (128 = straight)
	unsigned short duty16;
	duty16 = (setpoint * 75 / 16) + 900;
	PWMDTY01 = duty16;
}

void _Servo2_set(uint8_t setpoint){   // Camera pan (128 = straight)
	unsigned short duty16;
	duty16 = (setpoint * 75 / 16) + 900;
	PWMDTY23 = duty16;
}

void _Servo3_set(uint8_t setpoint){
	unsigned short duty16;
	duty16 = (setpoint * 75 / 16) + 900;
	PWMDTY45 = duty16;
}

void _Servo4_set(uint8_t setpoint){
	unsigned short duty16;
	duty16 = (setpoint * 75 / 16) + 900;
	PWMDTY67 = duty16;
}

void Servo_set1(uint8_t setpoint){
	TargetSetpoint1 = setpoint; 
}

void Servo_set2(uint8_t setpoint){
	TargetSetpoint2 = setpoint; 
}

void Servo_set3(uint8_t setpoint){
	TargetSetpoint3 = setpoint; 
}

void Servo_set4(uint8_t setpoint){
	TargetSetpoint4 = setpoint; 
}

uint8_t Servo_get1(void) {
	return TargetSetpoint1;
}

uint8_t Servo_get2(void) {
	return TargetSetpoint2;
}

uint8_t Servo_get3(void) {
	return TargetSetpoint3; 
}

uint8_t Servo_get4(void) {
	return TargetSetpoint4; 
}

/**
 * Converts the servo's target into a value with units 0.0001 radians.
 *
 * @param target The servo's target (between 0 and 255).
 * @return The angle of the servo in 0.0001 radians.
 */
int16_t getAngle(int32_t target) {
	int32_t center = 128 + SERVO_CENTER_CALIB;
	int16_t angle;
	if(target <= center) { //steering left
	
		// Suppose target = 20
		//   angle = -1 * 5200 * 20 / 128 + 5200 = 4387 (0.4387 radians)
		angle = (int16_t)(-1 * SERVO_STEERING_LEFT_CALIB * target / center + SERVO_STEERING_LEFT_CALIB);
		
	} else { //steering right
		
		// Suppose target = 235
		//    angle = -5000 * (235 - 128) / (255 - 128) = -4212 (-0.4212 radians)
		angle = (int16_t)(SERVO_STEERING_RIGHT_CALIB * (target - center) / (255 - center));
	}
	
	return angle;
}

/**
 * Returns the camera's current tilt angle.
 *
 * Input:  none
 * Output: The camera's current tilt angle, signed .0001 radian angle, +: up, -: down
 */
int16_t Servo_getCameraTiltAngle() {
	int32_t target = Servo_get1();
	return getAngle(target);
}

/**
 * Returns the camera's current pan angle.
 *
 * Input:  none
 * Output: The camera's current pan angle, signed .0001 radian angle, +: left, -: right
 */
int16_t Servo_getCameraPanAngle() {
	int32_t target = Servo_get2();
	return getAngle(target);
}

/**
 * This converts the input angle from units of .0001 radians into 
 * into a unit-less value between 20 and 235 that can be used to 
 * control the servo.
 *
 * Input:  The desired servo angle in .0001 radians
 * Output: none
 */
uint8_t getServoTarget(int16_t angle) {
	int32_t center = 128 + SERVO_CENTER_CALIB;
	int32_t target32;
	uint8_t target;
	
	if(angle >= 0) { // Turn left
		
		// Bound left turn angle to be <= SERVO_STEERING_LEFT_CALIB
		if(angle > SERVO_STEERING_LEFT_CALIB) 
			angle = SERVO_STEERING_LEFT_CALIB; 
		
		// The following code translates the angle into a value between 0 (full-lock-left) and 128 (center).
		//
		// For example, suppose angle = 15700 (pi/2).  Since this is greater than SERVO_STEERING_LEFT_CALIB,
		// it will be re-assigned a value of SERVO_STEERING_LEFT_CALIB, which is 5200.
		// Thus, the target will be:
		//    target = (5200 - 5200) * 128 / 5200 = 0;
		// In fact, any angle greater than 0.52 radians will result in a target of 0.
		//
		// Suppose angle = 3140 (pi/10 radians):
		//    target = (5200 - 3140) * 128 / 5200 = 50.71
		//
		// Suppose angle = 0 (0 radians):
		//    target = (5200 - 0) * 128 / 5200 = 128
		// This is exactly the center value.
		//
		// Suppose angle = 1000 (0.1 radians):
		//    target = (5200 - 1000) * 128 / 5200 = 103
		//
		// Suppose angle = 100 (0.01 radians):
		//    target = (5200 - 100) * 128 / 5200 = 125  
		// This is very close to the center value of 128
		//
		// Suppose angle = 0 (0 radians):
		//    target = (5200 - 0) * 128 / 5200 = 128
		target32 = ((SERVO_STEERING_LEFT_CALIB - angle) * center) / SERVO_STEERING_LEFT_CALIB;
		
	} else { // Turn right
		// Bound right turn angle to be >= SERVO_STEERING_RIGHT_CALIB
		if(angle < SERVO_STEERING_RIGHT_CALIB) 
			angle = SERVO_STEERING_RIGHT_CALIB;
		
		// The following code translates the angle into a value between 128 (center) and 255 (full-lock-right)
		// 
		// Suppose angle = -15700 (-pi/2 radians)
		//    target = (255 - 128) * -5000 / -5000 + 128 = 255
		//
		// Suppose angle = -3140 (-pi/10 radians)
		//    target = (255 - 128) * -3140 / -5000 + 128 = 207
		//
		// Suppose angle = -1000 (0.1 radians):
		//    target = (255 - 128) * -1000 / -5000 + 128 = 153
		//
		// Suppose angle = -100 (0.01 radians):
		//    target = (255 - 128) * -100 / -5000 + 128 = 130
		//
		target32 = (255 - center) * angle / SERVO_STEERING_RIGHT_CALIB + center;
	}
	
	target = (uint8_t) target32;
	
	// Bound the target to be between 20 and 235
	if(target < 20) target = 20; 
	if(target > 235) target = 235;
	
	/*{ // for debugging
		char message[100];
		if (sprintf(message, "Servo_setSteeringAngle: angle = %i, target32 = %li, target = %i", angle, target32, target) > 0)
			Command_sendMessagePacket(message);
	}*/
	return target;
}

/**
 * Sets the camera's tilt angle.
 *
 * Input:  Camera tilt angle in .0001 radians, +: up, -: down
 * Output: none
 */
void Servo_setCameraTiltAngle(int16_t angle) {
	uint8_t target = getServoTarget(angle);
	Servo_set1(target); // sets the target set point
}

/**
 * Sets the camera's pan angle.
 *
 * Input:  Camera pan angle in .0001 radians, +: left, -: right
 * Output: none
 */
void Servo_setCameraPanAngle(int16_t angle) {
	uint8_t target = getServoTarget(angle);
	Servo_set2(target); // sets the target set point
}

/**
 * This interrupt must occur every 20ms (50Hz).
 * It was measured to generate an interupt every 20.02ms.
 */
interrupt 10 void int10Handler(void) {;       
	TFLG1 = TFLG1_CH2_INT10_BIT;       // acknowledge ECT, Output Compare 2
	TaskHandler_postTask(&Servo_updatePosition);
	TC2 = TCNT + TCNT_20MS_INTERVAL;
}
