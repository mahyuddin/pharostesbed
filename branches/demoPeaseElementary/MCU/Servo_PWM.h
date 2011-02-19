#ifndef _Servo_PWM_H
#define _Servo_PWM_H 1

#include "Types.h"

// filename ******** PWM.h **************
// A servo driver for the Proteus Robot

/* TEMPORARY NOTICE:
   This driver is also currently responsible for the motor control signal
   which is attached at servo1 position.  
*/

/*
 ** Pin connections **
    
    PWM1 -> Servo1
    PWM3 -> Servo2
    PWM5 -> Servo3
    PWM7 -> Servo4

 ** Feature Usage **
   
   All PWM channels (4 16-bit) : signals to servos
   Output Compare 7 : handles graceful servo movement
*/

//#define MOTOR_STOP 0

//#define MOTOR_MAX_POWER_SOFT 1800
//#define MOTOR_MIN_POWER_SOFT 256

#define SERVO_UPDATE_HZ 50 //servo positions update

/* if you change the servo period, you must recalculate the Servo_set functions (see below)*/
#define SERVO_PULSE_PERIOD 20000 //20ms period


//*****ODOMETRY TUNING CONSTANTS*****
#define SERVO_STEERING_LEFT_CALIB 5200  //affects the raw->steering_angle converstion when steering to the left
                                        //increasing causes robot to report higher steering angles for servo positions when turning left
                                        //as a result, higher values cause robot to turn less when seeking an odometry target

#define SERVO_STEERING_RIGHT_CALIB -5000 //affects the raw->steering_angle converstion when steering to the right
                                        //increasing causes robot to report higher steering angles for servo positions when turning right
                                        //as a result, higher values cause robot to turn less when seeking an odometry target

#define SERVO_CENTER_CALIB 0            //straight_servo_value = 128 + SERVO_CENTER_CALIB
                                        //if your car goes straight with the steering servo set to 126, SERVO_CENTER_CALIB should be -2
                                        //if your car goes straight with the steering servo set to 130, SERVO_CENTER_CALIB should be 2


//*************initialize periodic interrupt OC7************
// used to update three servos in a smooth transition
// MaxChangePerPeriod defines how fast the servos change
// assumes timer is initialized so TCNT runs at 3MHz, PLL on
//void OC7_Init(void);

//Servo initialization routine
//Run this first
void Servo_init(void);

//runs in foreground and updates servo positions periodically
//void ServoPeriodicFG(void);


//Enable/Disable functions save state
void Servo1_disable(void);
void Servo1_enable(void);
void Servo2_disable(void);
void Servo2_enable(void);
void Servo3_disable(void);
void Servo3_enable(void);
void Servo4_disable(void);
void Servo4_enable(void);

// returns status of servos
unsigned char Servo1_status(void);
unsigned char Servo2_status(void);
unsigned char Servo3_status(void);
unsigned char Servo4_status(void);

// Internal functions (immediate motion)
void _Servo1_set(uint8_t setpoint);
void _Servo2_set(uint8_t setpoint);
void _Servo3_set(uint8_t setpoint);
void _Servo4_set(uint8_t setpoint);

// API functions (smooth motion)
void Servo_set1(uint8_t setpoint);
void Servo_set2(uint8_t setpoint);
void Servo_set3(uint8_t setpoint);
void Servo_set4(uint8_t setpoint);
uint8_t Servo_get1(void);
uint8_t Servo_get2(void);
uint8_t Servo_get3(void);
uint8_t Servo_get4(void);

/**
 * Returns the camera's current tilt angle.
 *
 * Input:  none
 * Output: The camera's current tilt angle, signed .0001 radian angle, +: up, -: down
 */
int16_t Servo_getCameraTiltAngle();

/**
 * Returns the camera's current pan angle.
 *
 * Input:  none
 * Output: The camera's current pan angle, signed .0001 radian angle, +: left, -: right
 */
int16_t Servo_getCameraPanAngle();

/**
 * Sets the camera's tilt angle.
 *
 * Input:  Camera tilt angle in .0001 radians, +: up, -: down
 * Output: none
 */
void Servo_setCameraTiltAngle(int16_t angle);

/**
 * Sets the camera's pan angle.
 *
 * Input:  Camera pan angle in .0001 radians, +: left, -: right
 * Output: none
 */
void Servo_setCameraPanAngle(int16_t angle);


#endif /* _Servo_PWM_H */