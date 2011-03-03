/*
 *  Player - One Hell of a Robot Server
 *  Copyright (C) 2006 -
 *     Brian Gerkey
 *                      
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
//Proteus comm driver modified from Roomba comm driver by Nicholas Paine

#ifdef __cplusplus
extern "C" {
#endif

#ifndef PROTEUS_COMMS_H
#define PROTEUS_COMMS_H

#include <limits.h>
#include <stdint.h>

#define PROTEUS_BEGIN 0x24 //'$' to start transmissions
#define PROTEUS_END 0x0A // LF terminated transmissions
#define PROTEUS_ESCAPE 0xFF // Used to escape the above special characters

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

#define PROTEUS_DELAY_MODECHANGE_MS       5
#define INS_SAMPLE_FREQ 800

enum{
  PROTEUS_MODE_OFF,                  
  PROTEUS_MODE_PASSIVE,              
  PROTEUS_MODE_SAFE,                 
  PROTEUS_MODE_FULL
};

// Message type definitions
enum{
	ZERO,
	PROTEUS_ODOMETRY_PACKET,
	PROTEUS_IR_PACKET,
	PROTEUS_SONAR_PACKET,
	PROTEUS_COMPASS_PACKET,
	PROTEUS_OPAQUE,
	PROTEUS_TACHOMETER_PACKET,
	PROTEUS_STATUS_PACKET,
	PROTEUS_MOTOR_SAFETY_PACKET,
	PROTEUS_TEXT_MESSAGE_PACKET,
	PROTEUS_ACCELEROMETER_PACKET
};

#define PROTEUS_ODOMETRY_PACKET_SIZE 5
#define PROTEUS_IR_PACKET_SIZE 12
#define PROTEUS_SONAR_PACKET_SIZE 12
#define PROTEUS_COMPASS_PACKET_SIZE 3
#define PROTEUS_OPAQUE_PACKET_SIZE 1
#define PROTEUS_TACH_PACKET_SIZE 4
#define PROTEUS_STATUS_PACKET_SIZE 8
#define PROTEUS_MOTOR_SAFETY_PACKET_SIZE 4
#define PROTEUS_MAX_TEXT_MESSAGE_LENGTH 100

#define PROTEUS_PACKET_OVERHEAD           3 // one byte each for PROTEUS_BEGIN, MESSAGE_TYPE, and PROTEUS_END

#define MAX_CMD_LEN                      12 
#define MAX_PACKET_LEN MAX_CMD_LEN+PROTEUS_PACKET_OVERHEAD

// This is used to distinguish the type of compass data
enum {
	I2C_DATA,
	PWM_DATA,
};

const int ProteusPacketInfo[6] = {
	0,
	PROTEUS_ODOMETRY_PACKET_SIZE,
	PROTEUS_IR_PACKET_SIZE,
	PROTEUS_SONAR_PACKET_SIZE,
	PROTEUS_COMPASS_PACKET_SIZE,
	PROTEUS_OPAQUE_PACKET_SIZE
};

#define PROTEUS_FRONT_TO_REAR_AXLE     .258 //meters

//#define PROTEUS_AXLE_LENGTH              .3
#define PROTEUS_LENGTH 			 .4
#define PROTEUS_WIDTH                    .3

#ifndef MIN
  #define MIN(a,b) ((a < b) ? (a) : (b))
#endif
#ifndef MAX
  #define MAX(a,b) ((a > b) ? (a) : (b))
#endif
#ifndef NORMALIZE
  #define NORMALIZE(z) atan2(sin(z), cos(z))
#endif

#ifndef PI
#define PI 3.14159265
#endif

//#define TRUE 1
//#define FALSE 0

#define SUCCESS 1
#define FAIL 0
typedef uint8_t result_t;


#define SERIAL_RX_BUFFER_SIZE 500

typedef struct {
	/* Serial port to which the robot is connected */
	char serial_port[PATH_MAX];
	
	/* File descriptor associated with serial connection (-1 if no valid
	 * connection) */
	int fd;

	/* Current operation mode; one of PROTEUS_MODE_* */
	unsigned char mode;
	
	/* Integrated odometric position [m m rad] */
	double ox, oy, oa;

	/*
	float ir_fl;			//front left SHARP Infrared rangefinder distance, read
	float ir_fc;			//front center SHARP Infrared rangefinder distance, read
	float ir_fr;			//front right SHARP Infrared rangefinder distance, read
	float ir_rl;			//rear left SHARP Infrared rangefinder distance, read
	float ir_rc;			//rear center SHARP Infrared rangefinder distance, read
	float ir_rr;			//rear right SHARP Infrared rangefinder distance, read
	*/
	
	/*
	float accelerometer_x;  	//accelerometer x-axis force, read
	float accelerometer_y;	//accelerometer y-axis force, read
	*/
	
	/*
	float gyro_z;			//gyro z-axis rotation, read
	*/
	
	uint8_t newCompassData;
	float compass_heading;
	//float compass;
	
	uint8_t newMessage;
	uint8_t messageBuffer[PROTEUS_MAX_TEXT_MESSAGE_LENGTH];
	
	uint8_t newOdometryData;
	unsigned char motor_stall;
	float distance;
	float steering_angle;
	
	/*
	float srf_fl;			//front left SRF08 ultrasonic rangefinder distance, read
	float srf_fc;			//front center SRF08 ultrasonic rangefinder distance, read
	float srf_fr;			//front right SRF08 ultrasonic rangefinder distance, read
	float srf_rl;			//rear left SRF08 ultrasonic rangefinder distance, read
	float srf_rc;			//rear center SRF08 ultrasonic rangefinder distance, read
	float srf_rr;			//rear right SRF08 ultrasonic rangefinder distance, read
	float gps_lat;		//GPS latitude, read 16-bit
	float gps_long;		//GPS longitude, read 16-bit
	*/
	unsigned char line_detect;    //if line detecting, 1 for line, 0 for no line (not supported on most robots)
	
	
	uint8_t newStatusData;
	int16_t statusTachSpeed;
	int16_t statusTargetSpeed;
	int16_t statusMotorPower;
	int16_t statusSteeringAngle;
    
	uint8_t newINSData;
	
    /* INS Status stuff go! */
    double statusINSSpeedX;
    float statusINSAccelerationX;
    unsigned char statusINSTickX;
    
    double statusINSSpeedY;
    float statusINSAccelerationY;
    unsigned char statusINSTickY;
    
    float statusINSGyroSpeed;
    float statusINSOrientation;
    unsigned char statusINSTickGyro;
    
    float statusINSDisplaceX;
    float statusINSDisplaceY;
	
	/**
	 * The following variables are used to buffer incoming serial data.
	 * The buffer is empty when _rxBuffStartIndx = _rxBuffEndIndx.
	 */
	uint8_t serialRxBuffer[SERIAL_RX_BUFFER_SIZE];
	uint16_t rxBuffStartIndx; // points to the next Rx byte to process
	uint16_t rxBuffEndIndx; // points to the location where the next Rx byte should be stored
    
} proteus_comm_t;

proteus_comm_t* proteus_create(const char* serial_port);

void proteus_destroy(proteus_comm_t* r);

result_t proteus_open(proteus_comm_t* r);

result_t proteus_sendHeartBeat(proteus_comm_t* r);

//int proteus_init(proteus_comm_t* r, bool fullcontrol); // replaced by heartbeat

result_t proteus_close(proteus_comm_t* r);

result_t proteus_set_speeds(proteus_comm_t* r, double velocity, double angle);

result_t proteusProcessRxData(proteus_comm_t* r);

result_t proteusReceiveSerialData(proteus_comm_t* r);

result_t processTextMessagePacket(proteus_comm_t* r);

result_t processAccelerometerPacket(proteus_comm_t* r);

//int proteus_parse_sensor_packet(proteus_comm_t* r, unsigned char* buf, size_t buflen, uint8_t packcet);
//int proteus_get_sensors(proteus_comm_t* r, int timeout, uint8_t packet);
//int proteus_enable_sonar(proteus_comm_t* r);
//int proteus_disable_sonar(proteus_comm_t* r);
#ifdef __cplusplus
}
#endif

#endif

