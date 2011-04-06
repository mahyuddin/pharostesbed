/**
 * Command.c
 * Serial interface processing block for the Proteus Robot
 * Written by Paine {n.a.paine@gmail.com}
 * Last modified 1/15/10
 *
 * Commands come over the serial port with the following structure
 * {begin char} {opcode} {data 1} {data 2} ... {data N} {end char}
 *
 * The SerialDriver takes care of low level protocol handling, using the
 * {begin char} and {end char} to capture command tokens.
 * These tokens are turned over to Command.c and look like 
 * {opcode} {data 1} ... {data N}, where {opcode} is the actual command.
 * Command.c processes these commands
 *
 * Modified by Chien-Liang Fok on 11/25/2010
 *  - Changed the naming convention of variables
 *  - Removed magic numbers
 *  - Removed the unused "Active" variable
 *  - Divide up long method "InterfaceFG(...)" into numerous shorter methods.
 *  - Removed the InterfaceFG, commands are now processed synchronously as they are
 *    assembled by the SerialDriver.
 *  - Added interrupt 9 (ECT ch 1) for periodically delivering sensor data to x86.
 */

#include <mc9s12dp512.h>     /* derivative information */
#include "Types.h"
#include "LED.h"
#include "Command.h"
#include "Servo_PWM.h"
#include "Tach.h"
#include "MotorControl.h"
#include "TaskHandler.h"
//#include "Compass.h"
#include "INS.h"
#include "SerialDriver.h"
#include "String.h"
#include <stdio.h>
//#include "Sharp_IR.h"


/**
 * Keeps track of the number of times interrupt 9 has occured.
 */
uint16_t _int9Count = 0;

bool _heartbeatReceived = FALSE;
bool _heartBeatTimerArmed = FALSE;

/**
 * Initializes the command component.
 * This sets ECT channel 1 to be an output compare.
 */
void Command_init() {
	// Configure the safe mode interupt (ECT Channel 1, interupt 9)
	//TIOS |= TIOS_CH1_INT9_BIT;  // Set Enhanced Capture Timer Channel 9 to be output compare.  This is interrupt 9.
	
	//Command_startSendingData();
}

/**
 * Reads the wheel speed (2 bytes) and steering angle (2 bytes), and sends
 * them to the appropriate components that execute the movements.
 *
 * @param pkt The command received from the x86.
 * @param numBytes The number of bytes within the command
 */
void processDriveCmd(uint8_t* pkt, uint16_t numBytes) {
	
	// Verify that the number of bytes received is the amount expected.
	if (numBytes == sizeof(proteusDrivePkt)) {
		struct ProteusDrivePacket* drivePkt = (struct ProteusDrivePacket*)pkt;
		MotorControl_setTargetSpeed(drivePkt->speed);
		Servo_setSteeringAngle(drivePkt->angle);
	} else {
		char message[50];
		if (sprintf(message, "ERROR: Cmd drive size %i != %i [%x %x %x %x %x]", numBytes, sizeof(proteusDrivePkt),
			pkt[0], pkt[1], pkt[2], pkt[3], pkt[4]) > 0)
		  Command_sendMessagePacket(message);
	}
}

/**
 * Saves the two bytes in data into the specified buffer.
 *
 * @param buff The buffer into which the data is saved.
 * @param indx The index in the buffer at which to start saving data
 * @param data The source data to save.
 * @return The new index pointing to the position immediately after the last byte saved
 */
uint16_t saveTwoBytes(uint8_t* buff, uint16_t indx, int16_t data) {
	buff[indx++] = (char)(data >> 8);  // most significant byte in lowest address (Big Endian)
	buff[indx++] = (char)(data & 0x00FF);
	return indx;
}

/**
 * Sends an odometry packet to the x86 computer.
 */
void Command_sendOdometryPacket() {
	uint8_t outToSerial[MAX_PACKET_LEN];
	uint16_t indx = 0; // an index into the _outToSerial array
	uint16_t i;
	outToSerial[indx++] = PROTEUS_BEGIN;
	outToSerial[indx++] = PROTEUS_ODOMETRY_PACKET;
	indx = saveTwoBytes(outToSerial, indx, Tach_getDistance()); // distance in units of 1.2833 mm 
	indx = saveTwoBytes(outToSerial, indx, Servo_getSteeringAngle()); // angle in units of .0001 radians 
	outToSerial[indx++] = MotorControl_getMotorThrottled();
	outToSerial[indx++] = PROTEUS_END;
	
	for(i = 0; i < indx; i++){
		SerialDriver_sendByte(outToSerial[i]);  
	}
}

/**
 * Sends a message containing the previous motor power and current speed.
 * This is called whenever the motor controller reports a safety event.
 */
void Command_sendMotorSafetyMsg(int16_t previousMotorPower, int16_t currentSpeed) {
	uint8_t outToSerial[MAX_PACKET_LEN];
	uint16_t indx = 0; // an index into the _outToSerial array
	uint16_t i;
	outToSerial[indx++] = PROTEUS_BEGIN;
	outToSerial[indx++] = PROTEUS_MOTOR_SAFETY_PACKET;
	indx = saveTwoBytes(outToSerial, indx, previousMotorPower); // unit-less
	indx = saveTwoBytes(outToSerial, indx, currentSpeed); // in cm/s
	outToSerial[indx++] = PROTEUS_END;
	
	for(i = 0; i < indx; i++){
		SerialDriver_sendByte(outToSerial[i]);  
	}
}

/**
 * Sends a compass packet to the x86 computer.
 * This is called by Compass.c after it gets the compass reading.
 */
/*void Command_sendCompassPacket(uint8_t sensorType, uint16_t compassHeading) {
	uint8_t outToSerial[MAX_PACKET_LEN];
	uint16_t indx = 0; // an index into the _outToSerial array
	uint16_t i;
	
	outToSerial[indx++] = PROTEUS_BEGIN;
	outToSerial[indx++] = PROTEUS_COMPASS_PACKET;
	outToSerial[indx++] = sensorType;
	
	indx = saveTwoBytes(outToSerial, indx, compassHeading);
	
	outToSerial[indx++] = PROTEUS_END;
	
	for(i = 0; i < indx; i++){
		SerialDriver_sendByte(outToSerial[i]);  
	}
}
*/
/**
 * Sends an accelerometer packet to the x86 computer.
 * This is called by Compass.c after it gets the compass reading.
 */
#if USE_XAXIS
void Command_sendAccelerometerPacket(int16_t x, int16_t y, int16_t gyro) {
#else
void Command_sendAccelerometerPacket(int16_t y, int16_t gyro) {
#endif
	uint8_t outToSerial[MAX_PACKET_LEN];
	uint16_t indx = 0; // an index into the _outToSerial array
	uint16_t i;
	
	outToSerial[indx++] = PROTEUS_BEGIN;
	outToSerial[indx++] = PROTEUS_ACCELEROMETER_PACKET;
    #if USE_XAXIS
	indx = saveTwoBytes(outToSerial, indx, x);
    #endif
	indx = saveTwoBytes(outToSerial, indx, y);
	indx = saveTwoBytes(outToSerial, indx, gyro);
	
	outToSerial[indx++] = PROTEUS_END;
	
	for(i = 0; i < indx; i++){
		SerialDriver_sendByte(outToSerial[i]);  
	}
}


/**
 * Sends a text message to the x86 computer.
 * This is used for debugging/logging.
 */
void Command_sendMessagePacket(char* message) {
	uint8_t len = (uint8_t)strlen(message); // this should never exceed 0xff (255)
	uint16_t i;
	
	SerialDriver_sendByte(PROTEUS_BEGIN);
	SerialDriver_sendByte(PROTEUS_TEXT_MESSAGE_PACKET);
	SerialDriver_sendByte(len);
	
	for (i=0; i < len && i < PROTEUS_MAX_TEXT_MESSAGE_LENGTH; i++) {
		SerialDriver_sendByte(message[i]);  
	}
}

/**
 * This is called by interrupt 9 at 5Hz (200ms period).
 */
void Command_sendData() {
	if (_heartbeatReceived) {
		//LED_GREEN2 ^= 1;
		//Command_sendOdometryPacket();
		//Compass_getHeading();
	} else {
		//LED_GREEN2 = LED_OFF;
		//LED_BLUE2 = LED_OFF;
	}
}

void Command_sendStatus() {
	uint8_t outToSerial[MAX_PACKET_LEN];
	uint16_t indx = 0; // an index into the _outToSerial array
	uint16_t i;
	outToSerial[indx++] = PROTEUS_BEGIN;
	outToSerial[indx++] = PROTEUS_STATUS_PACKET;
	indx = saveTwoBytes(outToSerial, indx, Tach_getVelocity()); // velocity in cm/s
	indx = saveTwoBytes(outToSerial, indx, MotorControl_getTargetSpeed()); // target velocity in cm/s
	indx = saveTwoBytes(outToSerial, indx, MotorControl_getMotorPower()); // motor power being sent to the motor controller
	indx = saveTwoBytes(outToSerial, indx, Servo_getSteeringAngle()); // steering angle in units of .0001 radians 
	outToSerial[indx++] = PROTEUS_END;
	
	for(i = 0; i < indx; i++){
		SerialDriver_sendByte(outToSerial[i]);  
	}
}

/**
 * Enable interrupt 9.  This results in safe mode operation.
 */
void Command_startSendingData() {
	asm sei
	TIE |= TIE_CH1_INT9_BIT; // Timer Interrupt Enable Register, allow interrupt 13 (output compare 5) to occur
	TC1 = TCNT + 50; // first interrupt right away
	asm cli
}

/**
 * Disable interrupt 9.  This results in full mode operation (robot continues to run even
 * if motor command was not recently received).
 */
void Command_stopSendingData() {
	asm sei
	TIE &= ~TIE_CH1_INT9_BIT; // Timer Interrupt Enable Register, disallow interrupt 13 (output compare 5) to occur
	asm cli
}

/**
 * Processes a command that is received from the x86.
 *
 * @param cmd A pointer to the command.
 * @param size The size of the command in number of bytes.
 */
void Command_processCmd(uint8_t* cmd, uint16_t size) {
	switch(cmd[0]) { // Look at opcode (loosely based on roomba command set)
		case PROTEUS_OPCODE_HEARTBEAT:
			_heartbeatReceived = TRUE; // indicates that a heartbeat was received from the x86.
			_heartBeatTimerArmed = FALSE; // disarm the heartbeat timer
			break;
		case PROTEUS_OPCODE_BAUD:
			// TODO: change SCI baud rate
			break;
		case PROTEUS_OPCODE_CONTROL:
			// TODO: what does this command do?
			break;
		case PROTEUS_OPCODE_SAFE: 
			MotorControl_enableSafeMode(); // Sets the mode of operation to be "safe"
			break;
		case PROTEUS_OPCODE_FULL:
			MotorControl_disableSafeMode(); // Sets the mode of operation to be "full"
			break;
		case PROTEUS_OPCODE_STOP:
			Command_stopSendingData();
			MotorControl_setTargetSpeed(0); // stop the robot
			Servo_setSteeringAngle(0); // straighten the front wheels
			//LED_GREEN2 = OFF; // turn off LED5
			break;
		case PROTEUS_OPCODE_DRIVE:
			processDriveCmd(cmd, size);
			break;
		/*case PROTEUS_OPCODE_LEDS:
			// TODO...
			break;
		case PROTEUS_OPCODE_SONAR_EN : //begin periodic sonar samples  
			processOpcodeSonarEn();
			break;
		case PROTEUS_OPCODE_SONAR_DE : //stop periodic sonar samples
			processOpcodeSonarDe();
			break;
		case PROTEUS_OPCODE_SENSORS:
			processOpcodeSensors();
			break;*/
		default: 
			//LED_ORANGE2 ^= 1; // Command not recognized, toggle LED_RED3
			break; 
	} // end switch
}

/**
 * This interrupt periodically posts the Command_sendData task.
 * It occurs every 20ms (50Hz).
 *
 * However, it posts Command_sendData at 5Hz
 */
interrupt 9 void sendDataInterrupt(void) {
	TFLG1 = TFLG1_CH1_INT9_BIT; // acknowledge ECT, Channel 1

	_int9Count++;
	if (_int9Count % 10 == 0) { // 20ms * 10 = 200ms (5Hz)
		TaskHandler_postTask(&Command_sendData);
	}
	
	/*
	 * If a heart beat message is not received inbetween two executions
	 * of the code below, assume that the x86 has disconnected and set
	 * _heartBeatReceived = FALSE.
	 * 
	 * The heartbeat is expected to arrive every 1s.  This code runs
	 * every 1.4s, meaning we should always receive a heartbeat so long
	 * as the x86 is connected and alive.
	 */
	if (_int9Count == 70) { // 20ms * 70 = 1400ms (0.71Hz)
		if (_heartBeatTimerArmed) {
			_heartbeatReceived = FALSE;
		}
		_heartBeatTimerArmed = TRUE;
		_int9Count = 0;
		TaskHandler_postTask(&Command_sendStatus);
	}
	
	TC1 = TCNT + TCNT_20MS_INTERVAL;
}


/** Code provided by Cartographer group
 * Function: Command_sendIRPacket()
 * Inputs: None
 * Outputs: None
 * Desc: Sends an IR packet to the x86 computer.
 * DATA PACKETS COME LIKE THIS:
 * BEGIN Packet
 * PROTEUS_IR_PACKET
 * FL Data (2Bytes)
 * FC Data (2Bytes)
 * FR Data (2Bytes)
 * RL Data (2Bytes)
 * RC Data (2Bytes)
 * RR Data (2Bytes)
 * END Packet
 */
void Command_sendIRPacket(void) {
  uint8_t outToSerial[MAX_PACKET_LEN];
	uint16_t indx = 0; // an index into the _outToSerial array
  uint16_t i;
  
  outToSerial[indx++] = PROTEUS_BEGIN;  // Package BEGIN packet
	outToSerial[indx++] = PROTEUS_IR_PACKET;  // Identify data as IR packet
	indx = saveTwoBytes(outToSerial, indx, IR_get1()); //  
	indx = saveTwoBytes(outToSerial, indx, IR_get2()); //  
	indx = saveTwoBytes(outToSerial, indx, IR_get3()); //  
	indx = saveTwoBytes(outToSerial, indx, IR_get4()); //  
	indx = saveTwoBytes(outToSerial, indx, IR_get5()); //  		
	indx = saveTwoBytes(outToSerial, indx, IR_get6()); //  
	indx = saveTwoBytes(outToSerial, indx, IR_get7()); //   
	indx = saveTwoBytes(outToSerial, indx, IR_getL()); // Data information to the left of the robot  
	indx = saveTwoBytes(outToSerial, indx, IR_getR()); // Data information to the right of the robot  		
	outToSerial[indx++] = PROTEUS_END;  // Package END packet 

	//i should equal PROTEUS_IR_PACKET_SIZE 
  // Send all of the IR Data through the Serial Port
	for(i=0; i<indx; i++){
  	SerialDriver_sendByte(outToSerial[i]);
	}
}

