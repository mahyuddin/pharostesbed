/**
 * Speed measurement for the Proteus Robot Project
 * 
 * Originally written by Paine {n.a.paine@gmail.com}
 * Modified by Chien-Liang Fok <liangfok@mail.utexas.edu> on 11/28/2010.
 *  - Removed dependence on Scheduler
 *  - Added Enhanced Capture Timer, Channel 4, to generate 10Hz interrupts 
 *    for setting _checkTach to be TRUE.
 */

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

// The module assumes TCNT is enabled at 333 ns clock
// 5*65536*333.33ns = 109.226667 ms

#include <mc9s12dp512.h>     /* derivative information */
#include "Tach.h"
#include "TaskHandler.h"
#include "Types.h"
#include "LED.h"
#include "Command.h"
#include "MotorControl.h"
#include "SerialDriver.h"

#pragma LINK_INFO DERIVATIVE "mc9s12dp512"

// Private global variables: permanent allocation, local scope

/**
 * This is the time interval between calculating the speed in seconds * 1000.
 * The speed calculation is configured to be performed at 10Hz, or once every 0.1s.
 * 
 * 0.1 * 1000 = 100
 */
#define SPEED_CALC_TIME_INTERVAL 100

/**
 * This is the distance the robot travels per tachometer tick * 1000.
 *
 * From the evaluation of the tachometer here:
 * http://pharos.ece.utexas.edu/wiki/index.php/Evaluating_the_Traxxas_Renegade_Motor_Speed_%28PWM_Signal%29_-_11/29/2010
 *
 * There are 142 ticks per wheel rotation.  Since a wheel's circumference is 36.9cm,
 * the centimers per tick is 36.9/142 = 0.26.
 *
 * 0.26 * 1000 = 260
 *
 * Update: From Nick's measurements, the wheel circumference is 35cm.  Thus, the centimeters per tick is: 
 * 35/142 = 0.24647.   0.24647 * 1000 = 246
 */
//#define CENTIMETERS_PER_TICK 260
//#define CENTIMETERS_PER_TICK 246
// A value of 246 resulted in the robot moving at about half the speed it 
// should have been moving at.
#define CENTIMETERS_PER_TICK 140 

/**
 * Whether the number of tachometer ticks is being recorded.
 */
bool _tachTimerArmed = FALSE;

/**
 * Records the number of tach ticks for calculating speed of robot.
 * It is direction aware, meaning going in reverse results in a negative value.
 */
int16_t _numTachTicksSpeed = 0;

/**
 * Notifies the interrupt whether the foreground process is done
 * calculating the speed.
 */
bool _speedCalcDone = TRUE;

/**
 * Records the number of tach ticks since the last time Tach_getDistance
 * was called.  It is oblivious to direction and is always positive.
 */
uint16_t _numTachTicksDist = 0;

/**
 * The number of ticks that occured between two instances of Tach_doSpeedCalc.
 */
int16_t _speedCalcTicks = 0;

/**
 * The speed in cm/s.  A negative value means the robot is going in reverse.
 */
int16_t _speed = 0;

/**
 * A value of 1 means going in reverse.  Using during the speed calculation algorithm
 * to determine the sign of _speed.
 */
//int16_t _speedCalcDir;

/**
 * Keeps a running total of the number of TCNTs that have elapsed.
 * since the last interrupt.
 *
 * TODO: It assumes that TCNT does not overflow between interrupts!
 * TCNT overflows every 21.823ms!
 */
//unsigned long Tach_idt = 0;

//unsigned long Tach_dt = 0;
//unsigned short Tach_tcnt_prev = 0;

/**
 * Keeps track of the number of tachometer ticks.
 */
//uint32_t _numTachTicks = 0;

//unsigned char Tach_dir=0; //0=fwd, 1=bkwd
//unsigned long Tach_dt_filt; //filtered data
//unsigned long Tach_dir_filt; //filtered direction
//bool _tachNewData = FALSE;

//bool _tachSignalReceived = FALSE;

// ************Tach_Init**************************
// Activate Input capture 0
// PT1, PT0 are encoder inputs
// Input:  none
// Output: none
// Errors: assumes TCNT active at 3 MHz
void Tach_init() {
	/*
	 * Configure Enhanced Capture Timer (ECT) Channel 0, to be an input capture.
	 * TIOS = Timer Input Capture/Output Compare Select Register 
	 *        (0 = input capture, 1 = output compare)
	 */
	TIOS &= ~0x01;
	
	/*
	 * Configure ECT channel 0 to be an input.
	 * DDRT = Data Direction Register for Port T
	 *        (0 = in, 1 = out)
	 */
	DDRT &= ~0x01;  // this was 0x03 for some reason
	
	/*
	 * Configure ECT channel 0 to trigger interrupt on rising edge only
	 */
	TCTL4 &= ~0x02;       // EDG0B = 0
	TCTL4 |= 0x01;        // EDG0A = 1
	
	/*
	 * Enable the interrupt (ECT channel 0 results in interrupt 8, as is
	 * indicated by Table 5-1 of the 9S12 manual, see:
	 * http://pharos.ece.utexas.edu/wiki/images/6/6c/9s12dp512dgv1.pdf
	 */
	TIE |= 0x01;          // C0I = 1, arm PT0 input capture
	
	// Start the Tach_checkRobotStopped interrupt.
	// It interrupt 12.
	TIOS |= 0x10;  // activate TC4 as output compare
	TIE |= 0x10;   // Timer Interrupt Enable Register, allow interrupt 12 to occur
	TC4 = TCNT + 50; // first interrupt right away
}

/**
 * Returns velocity in +-cm/s.
 */
int16_t Tach_getVelocity(void) {
	return _speed;
}

/**
 * Returns the distance traveled since this last time this
 * method was called.  The value returned is in units of number of
 * rising pulse edges.
 *
 * This is called by Command.c when creating an odometry packet.
 */
int16_t Tach_getDistance(void) {
	int16_t result = (int16_t)_numTachTicksDist;
	_numTachTicksDist = 0;
	return result;
}

/**
 * Saves the two bytes in data into the specified buffer.
 *
 * @param buff The buffer into which the data is saved.
 * @param indx The index in the buffer at which to start saving data
 * @param data The source data to save.
 * @return The new index pointing to the position immediately after the last byte saved
 */
uint16_t Tach_saveTwoBytes(uint8_t* buff, uint16_t indx, int16_t data) {
	buff[indx++] = (char)(data >> 8);  // most significant byte in lowest address (Big Endian)
	buff[indx++] = (char)(data & 0x00FF);
	return indx;
}

/**
 * This is used for debugging.  It sends the current speed estimated
 * by the tachometer to the x86.
 */
void sendTachPacket() {
	uint8_t outToSerial[MAX_PACKET_LEN];
	uint16_t indx = 0; // an index into the outToSerial array
	uint16_t i;
	outToSerial[indx++] = PROTEUS_BEGIN;
	outToSerial[indx++] = PROTEUS_TACHOMETER_PACKET;
	indx = Tach_saveTwoBytes(outToSerial, indx, _speed); // the most recent speed calculation in cm/sec
	indx = Tach_saveTwoBytes(outToSerial, indx, MotorControl_getTargetSpeed());
	outToSerial[indx++] = PROTEUS_END;
	
	for(i = 0; i < indx; i++){
		SerialDriver_sendByte(outToSerial[i]);
	}
}

/**
 * Calculates the number of cm/s that the robot traveled.
 * It relies on variable _speedCalcTicks, which is the number of times the tachometer ticked
 * over a span of 100ms.  It saves the speed in variable _speed.
 */
void Tach_doSpeedCalc(void) {
  /*
   * NOTE: possible loss of precision since _speed is an int16_t and 
   * _speedCalcTicks is a _uint16_t
   *
   * In the evaluation here:
   * http://pharos.ece.utexas.edu/wiki/index.php/Evaluating_the_Traxxas_Renegade_Motor_Speed_%28PWM_Signal%29_-_11/29/2010
   *
   * The minimum period of receiving tachometer ticks is 245µs.  Over a span of
   * 100ms, we expect to receive 100ms/245µs = 100ms/0.245ms = 408 ticks
   * This is well within the range of a signed 16-bit value, whose maximum value is 0x7FFF = 32767.
   */
	_speed = (int16_t)(_speedCalcTicks * CENTIMETERS_PER_TICK / SPEED_CALC_TIME_INTERVAL);
	
	_speedCalcDone = TRUE;
	
	//sendTachPacket(); // for debugging
}

/**
 * This interrupt occurs on the rising edge of PT0, the encoder channel A.
 * Its rate of invocation is proportional to the speed of the robot.
 */
interrupt 8 void IC0Handler(void) {
	TFLG1 = TFLG1_CH0_INT8_BIT;  // Reset the interrupt, TFLG1 is the "Main Timer Interrupt Flag 1"
	
	/*
	 * The encoder we have is a quadrature encoder, meaning we can tell both
	 * velocity and direction.  To do this you need two signals.  We're using
	 * channel A for velocity and then channel B tells us direction.
	 * 
	 * See: http://zone.ni.com/devzone/cda/tut/p/id/4763
	 */
	if (PTT_PTT1)
		_numTachTicksSpeed--; // The robot is going backwards
	else
		_numTachTicksSpeed++;
	
	_numTachTicksDist++; // this is always positive
}

/**
 * Keeps track of the number of times interrupt 12 has occured.
 */
uint16_t _int12Count = 0;

/**
 * This interrupt is used to estimate the speed of the robot
 * based on the number of tachometer ticks received between the
 * times this interrupt occurs.  Each time this interrupt occurs,
 * it saves the number of tachometer ticks that occured since the
 * the previous interrupt and schedules a task to calculate the
 * distance. 
 *
 * This interrupt is scheduled to occur at 10Hz.
 */
interrupt 12 void int12Handler(void) {
	TFLG1 = TFLG1_CH4_INT12_BIT; // acknowledge ECT, channel 4
	
	_int12Count++;
	if (_int12Count == 5) { // count 5 interrupts since 20ms * 5 = 100ms (10Hz)
		if (_tachTimerArmed) {
			if (_speedCalcDone) {
				_speedCalcTicks = _numTachTicksSpeed;
				TaskHandler_postTask(&Tach_doSpeedCalc);
				_speedCalcDone = FALSE;
			}
		} else {
			_tachTimerArmed = TRUE;
		}
		_numTachTicksSpeed = 0;
		_int12Count = 0;
	}
	
	TC4 = TCNT + TCNT_20MS_INTERVAL;
}
