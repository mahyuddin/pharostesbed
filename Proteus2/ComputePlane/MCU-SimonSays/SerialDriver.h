#ifndef _SERIAL_DRIVER_H
#define _SERIAL_DRIVER_H 1
// filename **************SCIA_FIFO.H  *****************************
// Interrupting I/O routines to 9S12DP512 serial port 
// Jonathan W. Valvano 8/1/08

//  This example accompanies the books
//   "Embedded Microcomputer Systems: Real Time Interfacing",
//        Thompson, copyright (c) 2006,
//   "Introduction to Embedded Microcomputer Systems: 
//    Motorola 6811 and 6812 Simulation", Brooks-Cole, copyright (c) 2002

// Copyright 2008 by Jonathan W. Valvano, valvano@mail.utexas.edu 
//    You may use, edit, run or distribute this file 
//    as long as the above copyright notice remains 
// Modified by EE345L students Charlie Gough && Matt Hawk
// Modified by EE345M students Agustinus Darmawan + Mingjie Qiu

#include "Types.h"

// standard ASCII symbols 
#define CR   0x0D
#define LF   0x0A
#define BS   0x08
#define ESC  0x1B
#define SP   0x20
#define DEL  0x7F
#define TAB  0x09

// Interrupting I/O routines to 9S12DP512 serial port 
#define RDRF 0x20   // Receive Data Register Full Bit
#define TDRE 0x80   // Transmit Data Register Empty Bit

// Possible values of cmdState
#define ACTIVE 1
#define INACTIVE 0
 
//-------------------------SCI_Init------------------------
// Initialize Serial port SCI
// Input: baudRate is the baud rate in bits/sec
// Output: none
// SCIBDL=1500000/baudRate, for example
// baudRate =    9600 bits/sec  SCIBDL=156
// baudRate =   19200 bits/sec  SCIBDL=78
// baudRate =   38400 bits/sec  SCIBDL=39
// baudRate =  115200 bits/sec  SCIBDL=13
// assumes a module clock frequency of 24 MHz
// baud rate must be faster than 5900 bits/sec
void SerialDriver_init(unsigned long baudRate);

/**
 * This is continuously called by the foreground process.
 * It pops bytes off the serial buffer, assembles it into
 * commands, and submits them to Command.c for processing.
 */
void SerialDriver_processRxBytes(void);

/**
 * Wait for buffer to be empty, output 8-bit to serial port
 * interrupt synchronization.
 *
 * TODO: Get rid of this busy wait.
 *
 * Input: SCI port, 8-bit data to be transferred
 * Output: none
 */
void SerialDriver_sendByte(uint8_t data);

#endif /* _SERIAL_DRIVER_H */