//Command.h
//Parameter declaration for Proteus SCI protocol
//Written by Paine {n.a.paine@gmail.com}

/* Commands come over the serial port with the following structure
  {begin char} {opcode} {data 1} {data 2} ... {data N} {end char}
  
  SCI_Proteus.h takes care of low level protocol handling, using the {begin char} and {end char} to capture command packets
  These packets are turned over to Command.c and look like {opcode} {data 1} ... {data N}
  Command.c processes these commands
  */

//command map

#ifndef _PROTEUS_COMMAND_H
#define _PROTEUS_COMMAND_H 1

#include "Types.h"

#define PROTEUS_BEGIN 0x24 //'$' to start transmissions
#define PROTEUS_END 0x0A //LF terminated transmissions
#define PROTEUS_ESCAPE 0xFF // escape special characters

/* command opcodes */
#define PROTEUS_OPCODE_HEARTBEAT        0x61
#define PROTEUS_OPCODE_BAUD             0x62
#define PROTEUS_OPCODE_CONTROL          0x63
#define PROTEUS_OPCODE_SAFE             0x64
#define PROTEUS_OPCODE_FULL             0x65
#define PROTEUS_OPCODE_STOP             0x66
#define PROTEUS_OPCODE_DRIVE            0x67
#define PROTEUS_OPCODE_LEDS             0x68
#define PROTEUS_OPCODE_SENSORS          0x69
#define PROTEUS_OPCODE_SONAR_EN         0x6A
#define PROTEUS_OPCODE_SONAR_DE         0x6B

enum{
  PROTEUS_MODE_OFF,
  PROTEUS_MODE_PASSIVE,
  PROTEUS_MODE_SAFE,
  PROTEUS_MODE_FULL
};

enum {
	ZERO,
	PROTEUS_ODOMETRY_PACKET,
	PROTEUS_IR_PACKET,
	PROTEUS_SONAR_PACKET,
	PROTEUS_COMPASS_PACKET,
	PROTEUS_OPAQUE_PACKET,
	PROTEUS_TACHOMETER_PACKET,
	PROTEUS_STATUS_PACKET,
	PROTEUS_MOTOR_SAFETY_PACKET,
	PROTEUS_TEXT_MESSAGE_PACKET,
	PROTEUS_ACCELEROMETER_PACKET
};

/**
 * This is sent from the x86 to the micro-controller.
 * It tells the micro-controller to move at a certain speed,
 * and turn at a certain rate.
 */
typedef struct ProteusDrivePacket {
	uint8_t opcode;
	int16_t speed;
	int16_t angle;
} proteusDrivePkt;

typedef struct ProteusCompassPacket {
	uint8_t pktType;
	uint16_t heading;
} proteusCompassPkt;


#define PROTEUS_ODOMETRY_PACKET_SIZE      5
#define PROTEUS_IR_PACKET_SIZE           12
#define PROTEUS_SONAR_PACKET_SIZE        12
#define PROTEUS_OPAQUE_PACKET_SIZE        1

#define PROTEUS_PACKET_OVERHEAD           2
#define MAX_CMD_LEN 20
#define MAX_PACKET_LEN MAX_CMD_LEN + PROTEUS_PACKET_OVERHEAD
#define PROTEUS_MAX_TEXT_MESSAGE_LENGTH 100
#define SCI_X86 0

/**
 * Initialize the command component.
 */
void Command_init(void);

/**
 * Processes a command that is received from the x86.
 *
 * @param cmd A pointer to the command.
 * @param size The size of the command in number of bytes.
 */
void Command_processCmd(uint8_t* cmd, uint16_t size);

/**
 * Sends a compass packet to the x86.
 */
void Command_sendCompassPacket(uint8_t sensorType, uint16_t compassHeading);
void Command_sendAccelerometerPacket(uint8_t tickNumber, uint16_t x, uint16_t y, uint16_t gyro);

void Command_startSendingData(void);
void Command_stopSendingData(void);
void Command_sendStatus(void);
void Command_sendMotorSafetyMsg(int16_t previousMotorPower, int16_t currentSpeed);
void Command_sendMessagePacket(char* message);

#endif /* _PROTEUS_COMMAND_H */