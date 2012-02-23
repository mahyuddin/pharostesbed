/**
 * Interrupting I/O routines to 9S12DP512 serial port
 * Jonathan W. Valvano 8/1/08
 * Composed of files written by Jonathan W. Valvano
 *
 *  This example accompanies the books
 *   "Embedded Microcomputer Systems: Real Time Interfacing",
 *        Thompson, copyright (c) 2006,
 *   "Introduction to Embedded Microcomputer Systems:
 *    Motorola 6811 and 6812 Simulation", Brooks-Cole, copyright (c) 2002
 *
 * Copyright 2008 by Jonathan W. Valvano, valvano@mail.utexas.edu 
 *    You may use, edit, run or distribute this file
 *    as long as the above copyright notice remains
 *
 * Modified by EE345L students Charlie Gough && Matt Hawk
 * Modified by EE345M students Agustinus Darmawan + Mingjie Qiu
 * Multiport functionality added by Paine on 04/10/2009
 * Modified by Chien-Liang Fok on 11/25/2010:
 *   - Fixed a bug in method "interrupt 20 void SCI0Handler(void)" 
 *     when a new packet arrives before the previous one
 *     is processed.  Now it drops the packet rather than potentially
 *     corrupt the previously-received one.
 *   - Created a buffer called "_serialRxBuffer" for temporarily storing 
 *     incoming bytes so that they can be processed by a forground process
 *     rather than within an interrupt handler.  This significantly 
 *     shortens the interrupt handler.
 *   - Created a buffer called "_serialTxBuffer" for buffering data to
 *     be sent to the x86.
 *   - Removed unused funcationality
 */
#include <mc9s12dp512.h>
#include "LED.h"
#include "Types.h"
#include "Command.h"
#include "SerialDriver.h"

#define SERIAL_RX_BUFFER_SIZE 200
#define SERIAL_TX_BUFFER_SIZE 200

/**
 * The following variables are used to tokenize the bytes
 * received from the x86 into commands.
 */
uint8_t inFromSerial[MAX_CMD_LEN]; //This is for storing commands received from the x86.
uint8_t _cmdState = INACTIVE; // Keeps track of whether we're processing a command
uint16_t _cmd_buf_idx = 0; // The index of where to store the next command's bytes

/**
 * The following variables are used to buffer incoming serial data.
 * The buffer is empty when _rxBuffStartIndx = _rxBuffEndIndx.
 */
uint8_t _serialRxBuffer[SERIAL_RX_BUFFER_SIZE];
uint16_t _rxBuffStartIndx = 0; // points to the next Rx byte to process
uint16_t _rxBuffEndIndx = 0; // points to the location where the next Rx byte should be stored

/**
 * Calculates the next index into the serial rx buffer.
 */
uint16_t nextRxBuffIndx(uint16_t indx) {
	return (indx + 1) % SERIAL_RX_BUFFER_SIZE;
}

/**
 * Adds a new byte to the incoming serial data buffer.
 * This is called within an interrupt context.
 */
void addToRxSerialBuffer(uint8_t data) {
	uint16_t nxtEndIndx = nextRxBuffIndx(_rxBuffEndIndx);
	if (nxtEndIndx != _rxBuffStartIndx) {
		_serialRxBuffer[_rxBuffEndIndx] = data; // save the data
		_rxBuffEndIndx = nxtEndIndx; // advance the Rx buffer's end index
	} else {
		// The serial buffer overflowed!
		LED_RED2 ^= 1;
	}
}


uint8_t _serialTxBuffer[SERIAL_TX_BUFFER_SIZE];
uint16_t _txBuffStartIndx = 0; // points to the next Rx byte to process
uint16_t _txBuffEndIndx = 0; // points to the location where the next Rx byte should be stored
/**
 * Calculates the next index into the serial rx buffer.
 */
uint16_t nextTxBuffIndx(uint16_t indx) {
	return (indx + 1) % SERIAL_TX_BUFFER_SIZE;
}

/**
 * Adds a new byte to the serial data buffer.
 * This is called by a foreground process.  Since it 
 * does not execute within an interrupt process, it 
 * returns TRUE if it was successful, FALSE otherwise.
 */
bool addToTxSerialBuffer(uint8_t data) {
	uint16_t nxtEndIndx = nextTxBuffIndx(_txBuffEndIndx);
	if (nxtEndIndx != _txBuffStartIndx) {
		_serialTxBuffer[_txBuffEndIndx] = data; // save the data
		_txBuffEndIndx = nxtEndIndx; // advance the TX buffer's end index
		return TRUE;
	} else {
		// The serial buffer overflowed!
		LED_RED3 ^= 1;
		return FALSE;
	}
}

/**
 * Removes a byte of data from the Tx serial buffer.
 *
 * @param data A pointer to where the data should be stored.
 * @return TRUE if data was obtained.
 */
bool removeFromTxSerialBuffer(uint8_t* data) {
	if (_txBuffStartIndx != _txBuffEndIndx) { // If there is data in the buffer ...
		(*data) = _serialTxBuffer[_txBuffStartIndx];
		_txBuffStartIndx = nextTxBuffIndx(_txBuffStartIndx);
		return TRUE;
	} else
		return FALSE;
}

bool _inEscapeMode = FALSE;

/**
 * This is continuously called by the foreground process.
 * It pops bytes off the serial buffer, assembles it into
 * commands, and submits them to Command.c for processing.
 */
void SerialDriver_processRxBytes() {
	if (_rxBuffStartIndx != _rxBuffEndIndx) { // If there is data in the buffer ...
		
		// Grab the next byte received from the serial port.
		uint8_t data = _serialRxBuffer[_rxBuffStartIndx];
		
		// Since we've already grabbed the data off the buffer, 
		// it's safe to increment the start pointer now
		_rxBuffStartIndx = nextRxBuffIndx(_rxBuffStartIndx); 
		
		if (_cmdState == INACTIVE) {
			if(data == PROTEUS_BEGIN) {
				_cmdState = ACTIVE; // Begin a new command
				_cmd_buf_idx = 0;
			} // else discard the data, it must be the middle of a packet
		} else {
			if (_inEscapeMode) {
				// In escape mode, just add the next byte to the command
				_inEscapeMode = FALSE;
				if(_cmd_buf_idx >= MAX_CMD_LEN) {  // Ensure the command is not too long
					LED_RED2 ^= 1; // error, packet too long, discard it
					_cmdState = INACTIVE;
					_cmd_buf_idx = 0;
				} else
					inFromSerial[_cmd_buf_idx++] = data; // fill the command buffer
			} else {
				if (data == PROTEUS_ESCAPE)
					_inEscapeMode = TRUE; // the next character is being escaped
				else {
					if(data == PROTEUS_END) { // If end of command
						Command_processCmd(inFromSerial, _cmd_buf_idx);
						_cmdState = INACTIVE;
						_cmd_buf_idx = 0;
					} else {
						if(_cmd_buf_idx >= MAX_CMD_LEN) {  // Ensure the command is not too long
							LED_RED2 ^= 1; // error, packet too long, discard it
							_cmdState = INACTIVE;
							_cmd_buf_idx = 0;
						} else
							inFromSerial[_cmd_buf_idx++] = data; // fill the command buffer
					}
				}
			}
		}
	}
}
  
/**
 * Initialize Serial port.
 *
 * Input: baudRate is the baud rate in bits/sec
 * Output: none
 * 
 * SCI0BDL=1500000/baudRate, for example
 * baudRate =    9600 bits/sec  SCI0BDL=156
 * baudRate =   19200 bits/sec  SCI0BDL=78
 * baudRate =   38400 bits/sec  SCI0BDL=39
 * baudRate =  115200 bits/sec  SCI0BDL=13
 *
 * Assumes a module clock frequency of 24 MHz.
 * Baud rate must be faster than 5900 bits/sec.
 */
void SerialDriver_init(unsigned long baudRate) {
	//SCI0BDH = 0;   // br=MCLK/(16*BaudRate)
	
	//SCI0BDL = 500000L/baudRate;   //ECLK = 8mhz
	SCI0BD = 1500000L/baudRate;	 //ECLK = 24mhz
	//SCI0BDL = 2000000L/baudRate;	 //ECLK = 32mhz
	
	SCI0CR1 = 0;
	/* 
	bit value meaning
	7   0    LOOPS, no looping, normal
	6   0    WOMS, normal high/low outputs
	5   0    RSRC, not appliable with LOOPS=0
	4   0    M, 1 start, 8 data, 1 stop
	3   0    WAKE, wake by idle (not applicable)
	2   0    ILT, short idle time (not applicable)
	1   0    PE, no parity
	0   0    PT, parity type (not applicable with PE=0)
	*/
	SCI0CR2 = 0x2C;
	/*
	bit value meaning
	7   0    TIE, no transmit interrupts on TDRE
	6   0    TCIE, no transmit interrupts on TC
	5   1    RIE, no receive interrupts on RDRF
	4   0    ILIE, no interrupts on idle
	3   1    TE, enable transmitter
	2   1    RE, enable receiver
	1   0    RWU, no receiver wakeup
	0   0    SBK, no send break 
	*/
	asm cli   /* enable interrupts */
}

/**
 * Wait for buffer to be empty, output 8-bit to serial port
 * interrupt synchronization.
 *
 * TODO: Get rid of this busy wait.
 *
 * Input: SCI port, 8-bit data to be transferred
 * Output: none
 */
void SerialDriver_sendByte(uint8_t data) {
	while (!addToTxSerialBuffer(data)) { }; // spin if TxFifo is full
	SCI0CR2 = 0xAC; /* arm TDRE */
}



/**
 * This interrupt executes whenever a serial event associated with
 * serial port 0 occurs.
 *
 * Relevant register bits:
 *  - RDRF (Receive Data Register Full) but is set when new data is received
 *  - TDRE (Transmit Data Register Empty) bit is set when transmit data register becomes empty.
 */
interrupt 20 void SCI0Handler(void) {
	uint8_t data; // a buffer for holding the byte received
	
	if(SCI0SR1 & RDRF) { // if data is received...
		//RxFifo_Put(1,SCI1DRL); // clears RDRF
		data = SCI0DRL; // get the next byte received
		addToRxSerialBuffer(data);
	}
	
	// If there is data to send, send it!
	if((SCI0CR2 & 0x80) && (SCI0SR1 & TDRE)){
		if(removeFromTxSerialBuffer(&data)) {
			SCI0DRL = data;   // clears TDRE
		} else {
			SCI0CR2 = 0x2c;   // disarm TDRE
		}
	}
}

/**
 * This interrupt executes whenever a serial event associated with
 * serial port 0 occurs.
 *
 * Relevant register bits:
 *  - RDRF (Receive Data Register Full) but is set when new data is received
 *  - TDRE (Transmit Data Register Empty) bit is set when transmit data register becomes empty.
 */
/*interrupt 21 void SCI1Handler(void){
	char data; 
	if(SCI1SR1 & RDRF) { 
		RxFifo_Put(1,SCI1DRL); // clears RDRF
	}
	
	if((SCI1CR2 & 0x80) && (SCI1SR1 & TDRE)) {
		if(TxFifo_Get(1, &data)) {
			SCI1DRL = data;   // clears TDRE
		} else {
			SCI1CR2 = 0x2c;   // disarm TDRE
		}
	}
}*/
