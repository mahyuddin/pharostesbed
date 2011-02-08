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
 */

#include <mc9s12dp512.h>     /* derivative information */
#include "Servo_PWM.h"
#include "LED.h"
#include "Types.h"
#include "TaskHandler.h"

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
	DDRT |= 0x04;     //set directional bit PT2--output
	PWME = 0x00;      // disable channels 0-7
	PWMPOL |= 0xFF;   // PP0-7 high then low
	PWMCLK |= 0xFC;   // use clock SA,SB on channels 7-2
	PWMCLK &= ~0x02;  // use clock A on channel1 (motor)
	PWMPRCLK = 0x00;  // A,B = ECLK = 24MHz
	PWMCAE = 0x00;    // all left aligned
	PWMCTL = 0xF0;    // 4 16-bit channels
	PWMSCLA = 12;     // 1MHz SA clk
	PWMSCLB = 12;     // 1MHz SB clk
	
	//  PWMPER1 = 255;    // Channel1 is 255*41.667ns=10.6us, 24MHz/255=94.1kHz
	PWMPER01 = 2047;    // Channel1 is 2047*41.667ns=85.3us, 24MHz/2047=11.7kHz
	PWMPER23 = SERVO_PULSE_PERIOD;  
	PWMPER45 = SERVO_PULSE_PERIOD;  
	PWMPER67 = SERVO_PULSE_PERIOD;
	
	Motor_setSpeed(MOTOR_STOP);
	_Servo2_set(128+SERVO_CENTER_CALIB);
	_Servo3_set(128);
	_Servo4_set(128); 
	
	PWME = 0xFF;       // enable channels 0-7
	//Servo1Enabled = 1; // ??Nick??? is this a bug?? should be 0??
	Servo2Enabled = 1;
	Servo3Enabled = 1;
	Servo4Enabled = 1;
	
	// Start the 50Hz periodic interrupts
	TIOS |= 0x04;  // activate TC3 as output compare
	TIE |= 0x04;   // imer Interrupt Enable Register, allow interrupt 10 to occur
	TC2 = TCNT+50; // first interrupt right away
}

/**
 * Each time an interrupt occurs, update the servo positions.
 * This should be executed at 50Hz.
 */
void Servo_updatePosition(void) {
	short as2, ts2, as3, ts3, as4, ts4, mcpp;
	
	LED_BLUE1 ^= 1;
	
	as2 = (short) ActualSetpoint2;
	ts2 = (short) TargetSetpoint2;
	as3 = (short) ActualSetpoint3;
	ts3 = (short) TargetSetpoint3;
	as4 = (short) ActualSetpoint4;
	ts4 = (short) TargetSetpoint4;
	mcpp = (short) MaxChangePerPeriod;
	
	if(as2 - mcpp > ts2) ActualSetpoint2 = ActualSetpoint2 - MaxChangePerPeriod;
	else if(as2 + mcpp < ts2) ActualSetpoint2 = ActualSetpoint2 + MaxChangePerPeriod;
	else ActualSetpoint2 = TargetSetpoint2;
	
	if(as3 - mcpp > ts3) ActualSetpoint3 = ActualSetpoint3 - MaxChangePerPeriod;
	else if(as3 + mcpp < ts3) ActualSetpoint3 = ActualSetpoint3 + MaxChangePerPeriod;
	else ActualSetpoint3 = TargetSetpoint3;
	
	if(as4 - mcpp > ts4) ActualSetpoint4 = ActualSetpoint4 - MaxChangePerPeriod;
	else if(as4 + mcpp < ts4) ActualSetpoint4 = ActualSetpoint4 + MaxChangePerPeriod;
	else ActualSetpoint4 = TargetSetpoint4;
	
	_Servo2_set(ActualSetpoint2);   //128 straight
	_Servo3_set(ActualSetpoint3);
	_Servo4_set(ActualSetpoint4);
}

void Motor_disable(){
  PWME &= ~0x02; // disable channel 1
  Servo1Enabled = 0; 
}

void Motor_enable(){
  PWME |= 0x02; //enable channel 1
  Servo1Enabled = 1; 
}

void Servo2_disable(){
  PWME &= ~0x08; //disable channel 3
  Servo2Enabled = 0; 
}

void Servo2_enable(){
  PWME |= 0x08; //enable channel 3
  Servo2Enabled = 1; 
}

void Servo3_disable(){
  PWME &= ~0x20; //disable channel 5
  Servo3Enabled = 0; 
}

void Servo3_enable(){
  PWME |= 0x20; //enable channel 5
  Servo3Enabled = 1; 
}

void Servo4_disable(){
  PWME &= ~0x80; //disable channel 7
  Servo4Enabled = 0; 
}

void Servo4_enable(){
  PWME |= 0x80; //enable channel 7
  Servo4Enabled = 1; 
}

unsigned char Motor_status(){
 return Servo1Enabled; 
}

unsigned char Servo2_status(){
 return Servo2Enabled; 
}

unsigned char Servo3_status(){
 return Servo3Enabled; 
}

unsigned char Servo4_status(){
 return Servo4Enabled; 
}

/**
 * This is called by MotorControl.c
 *
 * 0 stopped
 * 2047 full speed
 * Input is -2048 -> +2048
 */
void Motor_setSpeed(int16_t setpoint) {
	
	// Output the direction signal, which is attached to PTT_PTT2
	if(setpoint < 0) { 
		PTT_PTT2 = 0;
		setpoint *= -1;
	} else
		PTT_PTT2 = 1;
		
	// set the PWM signal
	PWMDTY01 = setpoint;
}

// 128 straight
void _Servo2_set(unsigned char setpoint){   // steering
	unsigned short duty16;
	duty16 = (setpoint * 75 / 16) + 900;
	PWMDTY23 = duty16;
}

void _Servo3_set(unsigned char setpoint){
	unsigned short duty16;
	duty16 = (setpoint * 75 / 16) + 900;
	PWMDTY45 = duty16;
}

void _Servo4_set(unsigned char setpoint){
	unsigned short duty16;
	duty16 = (setpoint * 75 / 16) + 900;
	PWMDTY67 = duty16;
}

/*
void Servo_set1(unsigned char setpoint){
	TargetSetpoint1 = setpoint; 
}*/

void Servo_set2(unsigned char setpoint){
	TargetSetpoint2 = setpoint; 
}

void Servo_set3(unsigned char setpoint){
	TargetSetpoint3 = setpoint; 
}

void Servo_set4(unsigned char setpoint){
	TargetSetpoint4 = setpoint; 
}

unsigned short Motor_get(void){
	return PWMDTY01; 
}

unsigned char Servo_get2(void){
	return TargetSetpoint2; 
}

unsigned char Servo_get3(void){
	return TargetSetpoint3; 
}

unsigned char Servo_get4(void) {
	return TargetSetpoint4; 
}

/**
 * ************Servo_getSteeringAngle**************************
 * Gives steering angle (.0001 radians) for current servo position
 * Piecewise linear conversion (left and right)
 * Input:  none
 * Output: signed .0001 radian steering angle for current servo position
 *        +: left, -: right
 */
int16_t Servo_getSteeringAngle() {
	uint16_t x0,x2;
	int16_t y0,y1,y3;
	int16_t temp;
	x0 = Servo_get2();
	x2 = 128 + SERVO_CENTER_CALIB;
	
	if(x0 <= x2) { //steering left
		y1 = SERVO_STEERING_LEFT_CALIB;
		
		//y0 = -y1*x0/x2 + y1
		asm  ldd   y1
		asm  coma
		asm  comb
		asm  addd  #1
		asm  ldy   x0
		asm  emuls   
		asm  ldx   x2
		asm  edivs
		asm  tfr   y,d
		asm  addd  y1
		asm  std   y0 
	} else { //steering right
		y3 = SERVO_STEERING_RIGHT_CALIB;
		temp = 255 - x2;
		
		//y0 = y3*(x0-x2)/(255-x2)
		asm  ldd   x0
		asm  subd  x2
		asm  ldy   y3
		asm  emuls
		asm  ldx   temp
		asm  edivs
		asm  sty   y0 
	}
	
	return y0;
}

/**
 * Sets steering angle (.0001 radians) 
 * Piecewise linear conversion (left and right)
 * Input:  Steering angle (.0001 radians)
 * Output: none
 *        +: left, -: right
 */
void Servo_setSteeringAngle(short angle) {
	unsigned short x0,x2;
	short y0,y1,y3;
	y0 = angle;
	x2 = 128 + SERVO_CENTER_CALIB;
	
	if(y0 >= 0) { //left
		y1 = SERVO_STEERING_LEFT_CALIB;
		if(y0 > y1) y0 = y1;
		
		//x0 = (y1-y0)*x2/y1
		asm  ldd   y1
		asm  subd  y0
		asm  ldy   x2
		asm  emuls
		asm  ldx   y1
		asm  edivs
		asm  sty   x0
	} else { //right
		y3 = SERVO_STEERING_RIGHT_CALIB;
		if(y0 < y3) y0 = y3;
		//x0 = (255-x2)*y0/y3 + x2
		asm  ldd   #255
		asm  subd  x2
		asm  ldy   y0
		asm  emuls
		asm  ldx   y3
		asm  edivs
		asm  tfr   y,d
		asm  addd  x2
		asm  std   x0
	}
	
	if(x0 < 20) x0 = 20; 
	if(x0 > 235) x0 = 235;
	Servo_set2((unsigned char)x0);
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
