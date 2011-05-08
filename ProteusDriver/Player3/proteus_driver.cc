/*
 *  Player - One Hell of a Robot Server
 *  Copyright (C) 2006 -
 *     Brian Gerkey
 *     Modified from roomba code by Nick Paine
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

/** @ingroup drivers */
/** @{ */
/** @defgroup driver_roomba roomba
 @brief iRobot Roomba

Newer versions of the iRobot Roomba vaccum robot can be controlled by an
external computer over a serial line.  This driver supports control of
these robots.

Note that the serial port on top of the Roomba operates at 5V, not the
RS232 standard of 12V.  This means that you cannot just plug a plain
old serial cable between the Roomba and your PC's serial port.  You need
to put a level-shifter in between them.  Or you if have a computer that
exposes serial lines at "logic level," (e.g., the Gumstix), you can use
them directly.  Check out <a href="http://www.irobot.com/hacker">iRobot's
hacker site</a> for more information, including the pinout on the Roomba's
serial port.  The <a href="http://roomba.pbwiki.com">Roomba Wiki</a>
has a howto on building an appropriate serial cable.

@par Compile-time dependencies

- none

@par Provides

The proteus driver provides the following device interfaces:

- @ref interface_position2d
  - This interface returns odometry data (PLAYER_POSITION2D_DATA_STATE),
    and accepts car commands (PLAYER_POSITION2D_CMD_CAR).

- @ref interface_ir
  - This interface returns ir ranges (PLAYER_IR_DATA_RANGES).

@par Supported configuration requests

- PLAYER_POSITION2D_REQ_GET_GEOM

@par Configuration file options

- port (string)
  - Default: "/dev/ttyS0"
  - Serial port used to communicate with the robot.
- safe (integer)
  - Default: 1
  - Nonzero to keep the robot in "safe" mode (the robot will stop if
    the wheeldrop or cliff sensors are triggered), zero for "full" mode

@par Example

@verbatim
driver
(
  name "proteus"
  provides ["position2d:0" "ir:0"]
  port "/dev/ttyS0"
)
@endverbatim


@todo
- Add support for IRs, vacuum motors, etc.
- Recover from a cliff/wheeldrop sensor being triggered in safe mode;
the robot goes into passive mode when this happens, which right now
requires Player to be restarted

@author Brian Gerkey
*/
/** @} */


#include <unistd.h>
#include <stddef.h>
#include <stdio.h>
#include <string.h>
#include <pthread.h>
#include <time.h>
#include <player-3.0/libplayercore/playercore.h>
#include <player-3.0/libplayercommon/error.h>
#include <math.h>

#include "proteus_comms.h"

#define CYCLE_TIME_NS 100000000 //10hz


#ifndef PI
#define PI 3.14159265
#endif

class Proteus : public ThreadedDriver {
	public:
		Proteus(ConfigFile* cf, int section);
		
		// Must implement the following methods.
		//int Setup();
		//int Shutdown();
		virtual int MainSetup();
		virtual void MainQuit();
		//void DummyMainQuit(void*);
		
		// This method will be invoked on each incoming message
		virtual int ProcessMessage(QueuePointer & resp_queue, player_msghdr * hdr, void * data);
		
	private:
		// Main function for device thread.
		virtual void Main();
		
		/**
		 * Attempts to open a serial connection to the MCU.
		 */
		//result_t openSerialConnection();
		
		/**
		 * Closes the serial connection to the MCU.
		 */
		//result_t closeSerialConnection();
		
		// Fetch the latest data from the micro-controller
		//int getNewData();
		
		void publishOpaqueMsg(uint8_t* msgBuffer);
		
		void updatePos2D();
		void updateCompass();
		void updateIR();
		//void updateSonar();
		//void updateOpaque();
		
		// The serial port where the micro-controller is attached
		const char* serial_port;
		
		/**
		 * Whether the robot is in safe mode.  In safe mode, if the proteus robot does not 
		 * receive a motor command for a second, it will automatically stop.
		 */
		bool safe;
		
		//bool ir_as_sonar; //ir data to sonar interface
		
		/**
		 * This is the address to each device provided by the Proteus robot.
		 *
		 * See: http://playerstage.sourceforge.net/doc/Player-2.1.0/player/structplayer__devaddr.html
		 */
		player_devaddr_t position_addr;
		// player_devaddr_t power_addr;
		player_devaddr_t ir_addr;
		//player_devaddr_t sonar_addr;
		player_devaddr_t opaque_addr;
		player_devaddr_t compass_addr;
		
		
		// The underlying proteus object
		proteus_comm_t* proteus_dev;
		
		// Used when sending a motor command to the micro-controller
		player_position2d_cmd_car_t position_car_cmd;
		
		player_position2d_cmd_vel_t position_vel_cmd;
		player_position2d_geom_t pos_geom;
		player_ir_pose ir_array;
		player_sonar_geom sonar_array;
		player_opaque_data_t opaque_data;
		
		// Whether the interface is being used
		uint16_t interfacesEnabled;
		
		int packet_type;
		
		timeval _currTime;
};

/**
 * A factory creation function
 */
Driver* Proteus_Init(ConfigFile* cf, int section) {
	return (Driver*)(new Proteus(cf, section));
}

/**
 * A driver registration function
 */
void proteus_Register(DriverTable* table) {
	table->AddDriver("proteus", Proteus_Init);
}

/**
 * The constructor.  The Proteus class extends the Driver class whose API can be found here:
 * http://playerstage.sourceforge.net/doc/Player-2.1.0/player/classDriver.html
 *
 * Since this is a multi-interface driver, each interface is added using AddInterface(...).
 */
Proteus::Proteus(ConfigFile* cf, int section)
: ThreadedDriver(cf, section, true /* new commands overwrite old commands*/, PLAYER_MSGQUEUE_DEFAULT_MAXLEN) {
	
	gettimeofday(&_currTime, NULL);
	printf("%ld.%.6ld proteus_driver: constructor called\n", _currTime.tv_sec, _currTime.tv_usec);
	
	// Clear the device addresses...
	memset(&this->position_addr,0,sizeof(player_devaddr_t));
	//memset(&this->power_addr,0,sizeof(player_devaddr_t));
	memset(&this->ir_addr,0,sizeof(player_devaddr_t));
	//memset(&this->sonar_addr,0,sizeof(player_devaddr_t));
	memset(&this->opaque_addr,0,sizeof(player_devaddr_t));
	memset(&this->compass_addr, 0, sizeof(player_devaddr_t));
	
	// Reset the interfaces enabled...
	interfacesEnabled = 0;
	
	/*
	 *  Do we create a position 2D interface?
	 *
	 *  For documentation of ReadDeviceAddr, see:
	 *  http://playerstage.sourceforge.net/doc/Player-2.1.0/player/classConfigFile.html#b3f2b1557d97d5460a3d0ce83def74c8
	 */
	if(cf->ReadDeviceAddr(&(this->position_addr), section, "provides" /*field name*/, PLAYER_POSITION2D_CODE /*interface type*/, 
		-1 /*match all tuple indices*/, NULL /*match all device key values*/) == 0) 
	{
		if(this->AddInterface(this->position_addr) != 0) {
			this->SetError(-1);
			return;
		}
		interfacesEnabled |= POSITION2D_INTERFACE;
	}
	
	// Do we create a compass position interface?
	if(cf->ReadDeviceAddr(&(this->compass_addr), section, "provides", PLAYER_POSITION2D_CODE, -1, "compass") == 0) {
		if(this->AddInterface(this->compass_addr) != 0) {
			this->SetError(-1);
			return;
		}
		interfacesEnabled |= COMPASS_INTERFACE;
	}
	
	
	// Do we create a power interface?
	/*if(cf->ReadDeviceAddr(&(this->power_addr), section, "provides", PLAYER_POWER_CODE, -1, NULL) == 0) {
		if(this->AddInterface(this->power_addr) != 0) {
			this->SetError(-1);
			return;
		}
	}*/
	
	// Do we create an IR interface?
	if(cf->ReadDeviceAddr(&(this->ir_addr), section, "provides", PLAYER_IR_CODE, -1, NULL) == 0) {
		if(this->AddInterface(this->ir_addr) != 0) {
			this->SetError(-1);
			return;
		}
		interfacesEnabled |= IR_INTERFACE;
	}
	
	// Do we create a sonar interface?
	/*if(cf->ReadDeviceAddr(&(this->sonar_addr), section, "provides", PLAYER_SONAR_CODE, -1, NULL) == 0) {
		if(this->AddInterface(this->sonar_addr) != 0) {
			this->SetError(-1);
			return;
		}
		b_sonar_interface = true;
	}*/
	
	// Do we create a Opaque interface?
	if(cf->ReadDeviceAddr(&(this->opaque_addr), section, "provides", PLAYER_OPAQUE_CODE, -1, NULL) == 0) {
		if(this->AddInterface(this->opaque_addr) != 0) {
			this->SetError(-1);
			return;
		}
		interfacesEnabled |= OPAQUE_INTERFACE;
		printf("proteus_driver: opaque interface enabled.\n");
	}
	
	/* 
	 * For documentation, see: 
	 * http://playerstage.sourceforge.net/doc/Player-2.1.0/player/classConfigFile.html#5b8c497335c1036b9a865d4c784625c9
	 */
	this->serial_port = cf->ReadString(section, "port", "/dev/ttyS0" /*default value*/);
	this->safe = cf->ReadInt(section, "safe", 1);
	//this->ir_as_sonar = cf->ReadInt(section, "ir_as_sonar", 0);
	this->proteus_dev = NULL;
} // end constructor

/*result_t Proteus::openSerialConnection() {
	return proteus_open(this->proteus_dev);
}

result_t Proteus::closeSerialConnection() {
	return proteus_close(this->proteus_dev);
}*/

/**
 * Initialize the driver.  This function is called when the first client subscribes.
 * See: http://playerstage.sourceforge.net/doc/Player-2.1.0/player/classDriver.html#032a5853e3099f7613de6074941527d9
 */
int Proteus::MainSetup() {
	this->proteus_dev = proteus_create(this->serial_port);
	
	// Even if this failed, the main method will retry later.
	gettimeofday(&_currTime, NULL);
	printf("%ld.%.6ld proteus_driver: setup: opening serial connection to MCU on %s...\n", 
		_currTime.tv_sec, _currTime.tv_usec, this->serial_port);
	
	if (proteus_open(this->proteus_dev) == FAIL) {
		gettimeofday(&_currTime, NULL);
		printf("%ld.%.6ld proteus_driver: setup: ERROR: failed to open %s to MCU, will try again later...\n", 
			_currTime.tv_sec, _currTime.tv_usec, this->serial_port);
	}
	
	//if(b_sonar_interface && !this->ir_as_sonar){  //start sampling sonar sensors
	//	proteus_enable_sonar(this->proteus_dev);
	//}
	
	// Each driver has its own thread, which is started by the function below.
	// The thread starts executing in the Proteus::Main(void) method, which is defined below.
	//StartThread();
	return 0;
}

/**
 * Terminates the driver.  This method is called when the last client disconnects.
 * See: http://playerstage.sourceforge.net/doc/Player-2.1.0/player/classDriver.html#925c5c4806495d699eb9719ebdd1a03a
 */
void Proteus::MainQuit() {
	gettimeofday(&_currTime, NULL);
	printf("%ld.%.6ld proteus_driver: shutdown: shutting down...\n", _currTime.tv_sec, _currTime.tv_usec);
	
	// Wait for driver thread to terminate, see:
	// http://playerstage.sourceforge.net/doc/Player-2.1.0/player/classDriver.html#cc21fd92e8bbe2c006497c9a9313f626
	StopThread();
	printf("proteus_driver: Done calling StopThread...\n");
	gettimeofday(&_currTime, NULL);
	printf("%ld.%.6ld proteus_driver: shutdown: calling proteus_close...\n", _currTime.tv_sec, _currTime.tv_usec);
	
	if(proteus_close(this->proteus_dev)== FAIL) {
		gettimeofday(&_currTime, NULL);
		printf("%ld.%.6ld proteus_driver: shutdown: failed to close proteus connection.\n", _currTime.tv_sec, _currTime.tv_usec);
	}
	
	gettimeofday(&_currTime, NULL);
	printf("%ld.%.6ld proteus_driver: shutdown: calling proteus_destroy...\n", _currTime.tv_sec, _currTime.tv_usec);
	proteus_destroy(this->proteus_dev);
	this->proteus_dev = NULL;
	
	//return 0;
}

/**
 * Updates and publishes the latest position 2d data.  This is the odometry
 * data provided by the robot.
 */
void Proteus::updatePos2D() {
	////////////////////////////
	// Update position2d data
	// Update odometry info first
	// First-order odometric integration for a car
	this->proteus_dev->oa += this->proteus_dev->distance *
		atan(this->proteus_dev->steering_angle) / PROTEUS_FRONT_TO_REAR_AXLE;
	
	//clip rotation to +/-PI
	if(this->proteus_dev->oa > PI)  this->proteus_dev->oa -= 2*PI;
	if(this->proteus_dev->oa < -PI)  this->proteus_dev->oa += 2*PI;
	
	float prev_x_pos = this->proteus_dev->ox;
	this->proteus_dev->ox += this->proteus_dev->distance * cos(this->proteus_dev->oa);
	this->proteus_dev->oy += this->proteus_dev->distance * sin(this->proteus_dev->oa);
	
	player_position2d_data_t posdata;
	memset(&posdata,0,sizeof(posdata));
	
	posdata.pos.px = this->proteus_dev->ox;
	posdata.pos.py = this->proteus_dev->oy;
	posdata.pos.pa = this->proteus_dev->oa;
	posdata.vel.px = this->proteus_dev->distance / ((float)CYCLE_TIME_NS/1000000000);
	posdata.stall = this->proteus_dev->motor_stall;
	
	//printf("Publishing the following position2D data: px=%f, py=%f, pa=%f, vel=%f, stall=%i\n",
	//	posdata.pos.px, posdata.pos.py, posdata.pos.pa, posdata.vel.px, posdata.stall);
	
	// publish the new position2d data
	this->Publish(this->position_addr,
		PLAYER_MSGTYPE_DATA, PLAYER_POSITION2D_DATA_STATE,
		(void*)&posdata);
}

/**
 * Take a new compass heading reading and publish it.
 */
void Proteus::updateCompass() {
	player_position2d_data_t compassdata;
	memset(&compassdata,0,sizeof(compassdata)); // fill entire structure with zeros
	
	compassdata.pos.pa = this->proteus_dev->compass_heading;
	
	//gettimeofday(&_currTime, NULL);
	//printf("%ld.%.6ld proteus_driver: updateCompass: Publishing new compass data: %f (%f)\n",
	//	_currTime.tv_sec, _currTime.tv_usec, compassdata.pos.pa, this->proteus_dev->compass_heading);
	
	// publish the new compass data
	this->Publish(this->compass_addr,
		PLAYER_MSGTYPE_DATA, PLAYER_POSITION2D_DATA_STATE,
		(void*)&compassdata);
}

/**
 * Take the new IR data and publish it.
 */
void Proteus::updateIR() {
	player_ir_data_t irdata;
	memset(&irdata,0,sizeof(irdata)); // clear the irdata struct
	
	irdata.ranges_count = 6;
	irdata.ranges = new float [irdata.ranges_count];
	irdata.ranges[0] = this->proteus_dev->ir_fl;
	irdata.ranges[1] = this->proteus_dev->ir_fc;
	irdata.ranges[2] = this->proteus_dev->ir_fr;
	irdata.ranges[3] = this->proteus_dev->ir_rl;
	irdata.ranges[4] = this->proteus_dev->ir_rc;
	irdata.ranges[5] = this->proteus_dev->ir_rr;
	
	this->Publish(this->ir_addr, PLAYER_MSGTYPE_DATA, PLAYER_IR_DATA_RANGES, (void*)&irdata);
	delete [] irdata.ranges;
}

/*void Proteus::updateSonar() {
	////////////////////////////
	// Update sonar data
	player_sonar_data_t sonardata;
	memset(&sonardata,0,sizeof(sonardata)); // clear the sonar data struct
	
	sonardata.ranges_count = 6;
	sonardata.ranges = new float [sonardata.ranges_count];
	if(this->ir_as_sonar){
		sonardata.ranges[0] = this->proteus_dev->ir_fl;
		sonardata.ranges[1] = this->proteus_dev->ir_fc;
		sonardata.ranges[2] = this->proteus_dev->ir_fr;
		sonardata.ranges[3] = this->proteus_dev->ir_rl;
		sonardata.ranges[4] = this->proteus_dev->ir_rc;
		sonardata.ranges[5] = this->proteus_dev->ir_rr;
	} else {
		sonardata.ranges[0] = this->proteus_dev->srf_fl;
		sonardata.ranges[1] = this->proteus_dev->srf_fc;
		sonardata.ranges[2] = this->proteus_dev->srf_fr;
		sonardata.ranges[3] = this->proteus_dev->srf_rl;
		sonardata.ranges[4] = this->proteus_dev->srf_rc;
		sonardata.ranges[5] = this->proteus_dev->srf_rr;
	}
	
	this->Publish(this->sonar_addr,
		PLAYER_MSGTYPE_DATA, PLAYER_SONAR_DATA_RANGES,
		(void*)&sonardata);
	delete [] sonardata.ranges;
}*/

void Proteus::publishOpaqueMsg(uint8_t* msgBuffer) {
	// See: http://playerstage.sourceforge.net/doc/Player-2.1.0/player/structplayer__opaque__data.html
	player_opaque_data_t opaqueData;
	opaqueData.data_count = strlen((char*)msgBuffer);
	opaqueData.data = msgBuffer;
	this->Publish(this->opaque_addr,
		PLAYER_MSGTYPE_DATA,PLAYER_OPAQUE_DATA_STATE,
		(void*)&opaqueData,
		opaqueData.data_count);
}

/**
 * Takes a message from the MCU and publishes it inside an opaque message.
 */
//void Proteus::updateOpaque() {
	//this->publishOpaqueMsg(this->proteus_dev->messageBuffer);
	
	/*player_opaque_data_t opaqueData; // See: http://playerstage.sourceforge.net/doc/Player-2.1.0/player/structplayer__opaque__data.html
	
	opaqueData.data_count = strlen((char*)this->proteus_dev->messageBuffer);
	opaqueData.data = this->proteus_dev->messageBuffer;
	printf("proteus_driver: updateOpaque: count=%i\n", opaqueData.data_count);
	this->Publish(this->opaque_addr,
		PLAYER_MSGTYPE_DATA,PLAYER_OPAQUE_DATA_STATE,
		(void*)&opaqueData,
		opaqueData.data_count);*/
	/*char message[150];
	if (sprintf(message, "Hello World This is a wonderful world.  Nice weather today too!") > 0) {
		opaqueData.data_count = strlen(message);
		opaqueData.data = (uint8_t*)message;
		printf("proteus_driver: updateOpaque: count=%i\n", opaqueData.data_count);
		this->Publish(this->opaque_addr,
			PLAYER_MSGTYPE_DATA,PLAYER_OPAQUE_DATA_STATE,
			(void*)&opaqueData,
			opaqueData.data_count);
	}*/
//}

/**
 * The main thread of the proteus driver.
 * It sits in a loop doing the following:
 *   1. Process incomming messages from the Player Server.
 *   2. Grab the latest data from the sensors, by communicating with the micro-controller.
 *   3. Process and publish the new data from the micro-controller.
 */
void Proteus::Main() {
	
	uint32_t loopCount = 0;
	
	//pthread_setcancelstate(PTHREAD_CANCEL_ENABLE, NULL);
	
	while(1) {
		result_t commOK = SUCCESS;
		
		// See: http://www.kernel.org/doc/man-pages/online/pages/man3/pthread_testcancel.3.html
		pthread_testcancel();
		
		/*
		 * Send a heartbeat message every second.
		 */
		if (loopCount % 10 == 0) {
			// send a heart beat at 1Hz
			commOK = proteus_sendHeartBeat(this->proteus_dev, interfacesEnabled);
		}
		loopCount++;
		
		/*
		 * Method "ProcessMessages" is defined in the Device super-class. See the following website for documentation:
		 * http://playerstage.sourceforge.net/doc/Player-2.1.0/player/classDriver.html#24f15e0e5b8931805f734862f5bc8a7d
		 *
		 * In this case, it processes all messages that are currently buffered and no more.
		 * For each message, Proteus::ProcessMessage(...) is called, which is defined below.
		 */
		printf("proteus_driver: main: processing messages from player server...\n");
		this->ProcessMessages(); // should we only process one message at a time by passing a '1' in as a parameter?
		
		printf("proteus_driver: main: receiving serial data...\n");
		if (commOK == SUCCESS) { commOK = proteusReceiveSerialData(this->proteus_dev); }
		
		printf("proteus_driver: main: processing serial data...\n");
		if (commOK == SUCCESS) { 
			while (proteusProcessRxData(this->proteus_dev) == SUCCESS) {
				//printf("proteus_driver: main: checking for new data to publish...\n");
				
				if (this->proteus_dev->newOdometryData) {
					//printf("proteus_driver: main: Publishing new odometry data!\n");
					this->updatePos2D();
					this->proteus_dev->newOdometryData = false;
				} 
				
				if (this->proteus_dev->newCompassData) {
					this->updateCompass();
					this->proteus_dev->newCompassData = false;
				}
				
				if (this->proteus_dev->newIRdata) {
					this->updateIR();
					this->proteus_dev->newIRdata = false;
				}
				
				if (proteus_dev->newStatusData) {
					uint8_t textMessage[500];
					sprintf((char*)textMessage, "MCU_STATUS: tach speed = %i, target speed = %i, motor power = %i, steering angle = %f", 
						proteus_dev->statusTachSpeed, proteus_dev->statusTargetSpeed, proteus_dev->statusMotorPower, 
						proteus_dev->statusSteeringAngle/1000.0);
					publishOpaqueMsg(textMessage);
					proteus_dev->newStatusData = false;
				}
				
				//this->updateSonar();
				
				if (proteus_dev->newMessage) {
					publishOpaqueMsg(proteus_dev->messageBuffer);
					//this->updateOpaque();
					proteus_dev->newMessage = false;
				}
			}
		} else {
			gettimeofday(&_currTime, NULL);
			printf("%ld.%.6ld proteus_driver: main: COMM FAILURE: closing and then re-opening the serial link\n", 
				_currTime.tv_sec, _currTime.tv_usec);
			proteus_close(this->proteus_dev);
			proteus_open(this->proteus_dev);
		}
		
		//printf("sensor data updated..waiting\n");
		struct timespec ts;
		ts.tv_sec = 0;
		ts.tv_nsec = CYCLE_TIME_NS;
		nanosleep(&ts, NULL);
	}
	
	gettimeofday(&_currTime, NULL);
	printf("%ld.%.6ld proteus_driver: main: driver thread exits...\n", _currTime.tv_sec, _currTime.tv_usec);
}

// overrride parent method?
/*closing TCP connection to client 0 on port 6665
proteus_driver: shutdown: shutting down...

Program received signal SIGSEGV, Segmentation fault.
[Switching to Thread 0xb6fecb70 (LWP 1229)]
0x0017205c in Driver::DummyMainQuit () from /usr/local/lib/libplayercore.so.2
(gdb) backtrace
#0  0x0017205c in Driver::DummyMainQuit () from /usr/local/lib/libplayercore.so.2
#1  0x00172b11 in ~__pthread_cleanup_class (devicep=0x8054980) at /usr/include/pthread.h:535
#2  Driver::DummyMain (devicep=0x8054980) at driver.cc:353
#3  0x001e996e in start_thread () from /lib/tls/i686/cmov/libpthread.so.0
#4  0x00453a4e in clone () from /lib/tls/i686/cmov/libc.so.6
*/
//void Proteus::DummyMainQuit(void *devicep) {
//}
//void Proteus::MainQuit() {
//}

/**
 * This is called by the Player middleware, passing it commands from the Player Client.
 * See: http://playerstage.sourceforge.net/doc/Player-2.1.0/player/classDriver.html#b05d0e8502a494a83d0442a48095a35c
 *
 * The most important section in this method is the one that sends a motor command to the micro-controller.
 * This is done through the proteus_set_speeds(...) method defined in proteus_comms.c.
 */
int Proteus::ProcessMessage(QueuePointer & resp_queue, player_msghdr * hdr, void* data) {
	//printf("proteus_driver.cc: ProcessMessage: Processing message with type=%i, subtype=%i\n", hdr->type, hdr->subtype);
	
	// Send a motor command to the micro-controller
	if(Message::MatchMessage(hdr, PLAYER_MSGTYPE_CMD, PLAYER_POSITION2D_CMD_CAR, this->position_addr)) {
		// get and send the latest motor command
		if (!data) {
			PLAYER_ERROR("NULL position command");
			return -1;
		}
		
		memcpy(&position_car_cmd, data, sizeof position_car_cmd); // Save the message
		
		printf("proteus_driver: ProcessMessage: Sending car command velocity=%f, angle=%f.\n", 
			position_car_cmd.velocity, position_car_cmd.angle);
		
		if(proteus_set_speeds(this->proteus_dev, position_car_cmd.velocity, position_car_cmd.angle) < 0) {
			PLAYER_ERROR("proteus_driver.cc: Failed to set speeds to proteus");
		}
		return 0;
	}
	
	// Send a velocity command to the micro-controller
	// Note: velocity control is supported for crude mobility but is NOT accurate
	else if(Message::MatchMessage(hdr, PLAYER_MSGTYPE_CMD, PLAYER_POSITION2D_CMD_VEL, this->position_addr)) {
		// get and send the latest motor command
		if (!data) {
			PLAYER_ERROR("NULL position command");
			return -1;
		}
		
		memcpy(&position_vel_cmd, data, sizeof position_vel_cmd); // save the message
		
		//printf("proteus_driver.cc: ProcessMessage: Sending car commands velocity=%f, angle=%f.", position_vel_cmd.vel.px, position_vel_cmd.vel.pa);
		
		if(proteus_set_speeds(this->proteus_dev, position_vel_cmd.vel.px, position_vel_cmd.vel.pa) < 0) {
			PLAYER_ERROR("failed to set speeds to proteus");
		}
		
		return 0;
	}
	
	// On the Proteus robot, the motor cannot be powered on/off by software, just reply with a dummy Ack
	else if(Message::MatchMessage(hdr, PLAYER_MSGTYPE_REQ, PLAYER_POSITION2D_REQ_MOTOR_POWER, this->position_addr)) {
		//printf("proteus_driver.cc: ProcessMessage: The message requests the motor power\n");
		
		// This does not seem to be correct.  See the API for Publish:
		// http://playerstage.sourceforge.net/doc/Player-2.1.0/player/classDriver.html#d26174a11b4ed4837d7cd584b69cbd58
		// However, I don't think we're using this anyway so ignore for now.
		this->Publish(this->position_addr, PLAYER_MSGTYPE_RESP_ACK, PLAYER_POSITION2D_REQ_MOTOR_POWER);
		return 0;
	}
	
	// Reply with the robot's geometry, which is its length and width.
	else if(Message::MatchMessage(hdr,PLAYER_MSGTYPE_REQ, PLAYER_POSITION2D_REQ_GET_GEOM, this->position_addr)) {
		//printf("proteus_driver.cc: ProcessMessage: The message requests the robot geometry\n");
		
		/* Return the robot geometry. */
		memset(&pos_geom, 0, sizeof pos_geom);
		
		pos_geom.size.sl = PROTEUS_LENGTH;
		pos_geom.size.sw = PROTEUS_WIDTH;
	
		this->Publish(this->position_addr, resp_queue, PLAYER_MSGTYPE_RESP_ACK, 
			PLAYER_POSITION2D_REQ_GET_GEOM, (void*)&pos_geom);
		return 0;
	}
	
	// Reset the odometry data...
	else if (Message::MatchMessage(hdr,PLAYER_MSGTYPE_REQ, PLAYER_POSITION2D_REQ_RESET_ODOM, this->position_addr)) {
		printf("proteus_driver.cc: ProcessMessage: The message requests to reset odometry...\n");
		proteus_dev->oa = 0;
		proteus_dev->ox = 0;
		proteus_dev->oy = 0;
		proteus_dev->newOdometryData = 0;
		return 0;
	}
	
	// Reply with the IR sensor's orientation
	/*else if(Message::MatchMessage(hdr,PLAYER_MSGTYPE_REQ, PLAYER_IR_REQ_POSE, this->ir_addr)) {
		printf("proteus_driver.cc: ProcessMessage: The message requests the IR pose\n");
		
		// Returns the ir sensor spacial configuration 
		memset(&ir_array,0,sizeof(ir_array));
		
		ir_array.poses_count = 6;
		//ir_array.poses = new player_pose_t [ir_array.poses_count];
		
		//FIXME: translationals are at 0, need to go back and fill these in for accurate measurements
		
		//front left
		ir_array.poses[0].px = 0;
		ir_array.poses[0].py = 0;
		ir_array.poses[0].pyaw = PI/4;
		
		//front center
		ir_array.poses[1].px = 0;
		ir_array.poses[1].py = 0;
		ir_array.poses[1].pyaw = 0;
		
		//front right
		ir_array.poses[2].px = 0;
		ir_array.poses[2].py = 0;
		ir_array.poses[2].pyaw = -PI/4;
		
		//rear left
		ir_array.poses[3].px = 0;
		ir_array.poses[3].py = 0;
		ir_array.poses[3].pyaw = (3*PI)/4;
		
		//rear center
		ir_array.poses[4].px = 0;
		ir_array.poses[4].py = 0;
		ir_array.poses[4].pyaw = PI;
		
		//rear right
		ir_array.poses[5].px = 0;
		ir_array.poses[5].py = 0;
		ir_array.poses[5].pyaw = -(3*PI)/4;
		
		this->Publish(this->ir_addr, PLAYER_MSGTYPE_RESP_ACK, PLAYER_IR_REQ_POSE, (void*)&ir_array);
	}*/
	
	// Reply with the sonar sensor's orientation
	/*else if(Message::MatchMessage(hdr,PLAYER_MSGTYPE_REQ, PLAYER_SONAR_REQ_GET_GEOM, this->sonar_addr)) {
		printf("proteus_driver.cc: ProcessMessage: The message requests the sonar geom\n");
		
		// Returns the sonar sensor spacial configuration
		memset(&sonar_array,0,sizeof(sonar_array));
		
		sonar_array.poses_count = 6;
		//ir_array.poses = new player_pose_t [ir_array.poses_count];
		
		//FIXME: translationals are at 0, need to go back and fill these in for accurate measurements
		
		//front left
		sonar_array.poses[0].px = 0;
		sonar_array.poses[0].py = 0;
		sonar_array.poses[0].pyaw = PI/4;
		
		//front center
		sonar_array.poses[1].px = 0;
		sonar_array.poses[1].py = 0;
		sonar_array.poses[1].pyaw = 0;
		
		//front right
		sonar_array.poses[2].px = 0;
		sonar_array.poses[2].py = 0;
		sonar_array.poses[2].pyaw = -PI/4;
		
		//rear left
		sonar_array.poses[3].px = 0;
		sonar_array.poses[3].py = 0;
		sonar_array.poses[3].pyaw = (3*PI)/4;
		
		//rear center
		sonar_array.poses[4].px = 0;
		sonar_array.poses[4].py = 0;
		sonar_array.poses[4].pyaw = PI;
		
		//rear right
		sonar_array.poses[5].px = 0;
		sonar_array.poses[5].py = 0;
		sonar_array.poses[5].pyaw = -(3*PI)/4;
		
		this->Publish(this->sonar_addr, PLAYER_MSGTYPE_RESP_ACK, PLAYER_SONAR_REQ_GET_GEOM, (void*)&sonar_array);
	}*/
	else if(Message::MatchMessage(hdr,PLAYER_MSGTYPE_REQ, PLAYER_OPAQUE_REQ, this->opaque_addr)) {
		if (!data) {
			PLAYER_ERROR("NULL opaque data");
			return -1;
		}
		memcpy(&opaque_data, data, sizeof opaque_data);
		// Play Command
		//if (opaque_data.data[0] == 0 ) {
		//	uint8_t song_index;
		//	song_index = opaque_data.data[1];
		//	roomba_play_song(this->roomba_dev, song_index);
		//}
		return 0;
	}
	else {
		gettimeofday(&_currTime, NULL);
		printf("%ld.%.6ld proteus_driver.cc: ProcessMessage: Unkown message! type=%i, subtype=%i\n",
			_currTime.tv_sec, _currTime.tv_usec, hdr->type, hdr->subtype);
		return -1;
	}
}

////////////////////////////////////////////////////////////////////////////////
// Extra stuff for building a shared object.

/* need the extern to avoid C++ name-mangling  */
extern "C" {
	int player_driver_init(DriverTable* table) {
		timeval currTime;
		gettimeofday(&currTime, NULL);
		printf("%ld.%.6ld Proteus driver: Initializing version 3.1\n", currTime.tv_sec, currTime.tv_usec);
		
		proteus_Register(table);
		
		gettimeofday(&currTime, NULL);
		printf("%ld.%.6ld Proteus driver: Done initializing\n", currTime.tv_sec, currTime.tv_usec);
		return 0;
	}
}
