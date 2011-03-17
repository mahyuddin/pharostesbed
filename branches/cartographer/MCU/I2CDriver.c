/**
 * A hardware-interrupt-based driver for accessing slave device data
 * using the I2C bus. 
 *
 * Originally written by Chien-Liang Fok on 12/01/2010.
 */

#include <mc9s12dp512.h>
#include "I2CDriver.h"
#include "Types.h"
#include "LED.h"
#include "I2CDriver.h"
#include "Compass.h"
#include "TaskHandler.h"
#include "Command.h"

uint8_t _I2C_driver_state = I2C_STATE_IDLE;

uint16_t _i2cBusyCount;

/**
 * Initializes the I2C bus.  Enables the bus, sets the correct clock frequency,
 * and clears the interrupt.
 */
void I2CDriver_init(void) {
	
	_i2cBusyCount = 0;
	
	/*
	 * The IBCR (I2C Bus Control Register) configures the I2C bus.
	 * 
	 * See the I2C Block Description document available here:
	 * http://pharos.ece.utexas.edu/wiki/images/4/4a/9S12I2C_Block_Diagram.pdf
	 *
	 * Bit 7 (IBEN): bus enable/disable
	 * Bit 6 (IBIE): interrupt enable/disable
	 * Bit 5 (MS/SL): master/slave mode select bit
	 * Bit 4 (Tx/Rx): transmit/receive 
	 * Bit 3 (TXAK): transmit ack enable
	 * Bit 2 (RSTA): repeat start
	 * Bit 1: reserved, should always be zero
	 * Bit 0 (IBSWAI): stop in wait mode
	 */
	IBCR = I2C_IBCR_BUS_ENABLE_BIT 
		| I2C_IBCR_INTERUPT_ENABLE_BIT; // enable, with interrupts

	/*
	 * The IBFD (I2C Bus Frequency Divider) register determines the ratio of the I2C bus
	 * relative to the micro-controller frequency.
	 * 
	 * See the 9S12DP512 documementation available here:
	 * http://www.freescale.com/webapp/sps/site/prod_summary.jsp?code=S12D&fpsp=1&tab=Documentation_Tab
	 *
	 * The value of IBFD is described in the I2C Block Description document (S12IICV2.pdf):
	 *   For IBFD = 0x1f (0001 1111b)
	 *      - bits 2-0 = 111:
	 *        - SCL Tap: 15 clocks
	 *        - SDA Tap: 4 clocks
	 *      - bits 5-3 = 011:
	 *        - scl2start = 6 clocks
	 *        - scl2stop = 9 clocks
	 *        - scl2tap = 6 clocks
	 *        - tap2tap = 8 clocks
	 *      - bits 7-6 = 00:
	 *        - MUL = 1 (multiplier factor)
	 *
	 * From the I2C Block Description document (S12IICV2.pdf):
	 *   - SCL Divider = MUL * (2 * (scl2tap + ((SCL_Tap - 1) * tap2tap) + 2))
	 *             = 1 * (2 * (6 + ((15 - 1) * 8) + 2))
	 *             = 240
	 *     - This is correct since the MCU bus frequency is 24MHz, the I2C bus frequency is 100KHz, and
	 *       SCL Divider = MCU Bus freq / I2C Bus freq = 24MHz / 100KHz= 240
	 * 
	 *   - SDA Hold = MUL * (scl2tap + ((SDA_Tap - 1) * tap2tap) + 3)
	 *             = 1 * (6 + ((4 - 1) * 8) + 3)
	 *             = 33 clocks
	 *      - This matches the I2C Block Description document (S12IICV2.pdf), Table 1-5.
	 *      - Since the MCU bus is operating at 24MHz, SDA Hold = 33 clocks * (1/24MHz) = 1.375us,
	 *        which falls within the maximum of 3.45us.
	 *
	 *    - SCL Hold (Start) = 118
	 *    - SCL Hold (Stop) = 121
	 */
	IBFD = 0x1F; // 100KHz assuming 24 MHz bus clock
	
	/*
	 * The IBSR (I2C Bus Status Register) is read-only except for bits 1 (IBIF) and 4 (IBAL)
	 * 
	 * See the I2C Block Description document available here:
	 * http://pharos.ece.utexas.edu/wiki/images/4/4a/9S12I2C_Block_Diagram.pdf
	 *
	 * Bit 7 (TCF): 0 = data transfer in progress, 1 = data transfer done
	 * Bit 6 (IAAS): 1 = addressed as slave
	 * Bit 5 (IBB): 1 = bus is busy
	 * Bit 4 (IBAL): Hardware sets this bit when bus arbitration is lost.  
	 *        Software resets it by writing a 1 to it
	 * Bit 3: Reserved for future use
	 * Bit 2 (SRW): 0 = slave receive, 1 = slave transmit
	 * Bit 1 (IBIF): Set by hardware when bus arbitration is lost, byte transfer is complete, 
	 *        or device is addressed as slave.  Software resets this by writing a 1 to it.
	 * Bit 0 (RXAK): 1 = No Ack received
	 */
	IBSR = I2C_IBSR_BUS_INTERRUPT_BIT; // clear the I2C interrupt bit
}

/**
 * Waits for the IBIF flag to be set indicating that the I2C byte
 * transfer is complete.
 *
 * Note that this flag may also indicate other events like bus arbitration lost
 * or that the device was addressed as a slave.  For now, assume that these
 * events do not occur. 
 *
 * TODO: Handle exceptional cases like bus arbitration lost.
 */
//static bool waitIBIF() {
//	uint16_t MAX_LOOP_CNT = 20000; // prevent the loop from cycling forever
//	uint16_t loopCnt = 0;
//	
//	/*
//	 * Using an oscilloscope, it was determined that this wait loop
//	 * lasts about 100us, and that it loops about 20 times.  To be 
//	 * safe, we will allow the loop to cycle up to 20000 times.
//	 * This is because the loop was analyzed by instrumenting it with
//	 * LED-toggling code, whose latency compared to the loopCnt computation is
//	 * unknown.
//	 */
//	while((IBSR & 0x02) == 0 && loopCnt < MAX_LOOP_CNT) { 
//		loopCnt++;
//	}
//	
//	IBSR = 0x02; // reset the IBSR register
//	return TRUE;
//}


/**
 * Initiates the process of reading the compass.
 *
 * TODO: generalize this driver by using call-back function pointers.
 * @return TRUE if initiation successful, FALSE otherwise
 */
bool I2CDriver_readCompass() {
	if  (_I2C_driver_state == I2C_STATE_IDLE) {
		LED_BLUE2 ^= 1; // to indicate compass read activity
		_i2cBusyCount = 0;
		
		// Update the state of this driver
		_I2C_driver_state = I2C_STATE_ADDRESSING_SLAVE;
		
		// Enable I2C interrupts, and set MCU to be 9S12 MCU to be bus master and in transmit mode
		IBCR |= I2C_IBCR_MASTER_MODE_BIT | I2C_IBCR_TX_MODE_BIT;
		
		/*
		 * IBDR (I2C Bus Data Register)
		 *  - in master Tx mode, data is transmitted when it is written to the IBDR, MSB first
		 *  - in master Rx mode, reading this register initiates a data receive operation
		 */
		IBDR = COMPASS_ADDRESS; // Send the slave address on the I2C bus
		return TRUE;
	} else {
		_i2cBusyCount++;
		if (_i2cBusyCount == 3) {
			LED_BLUE2 ^= 1; // to indicate i2c reset condidion
			LED_RED1 ^= 1;
			
			Command_sendMessagePacket("ERROR: I2CDriver: compass read failed!");
			
			// abort the previous operation.
			IBCR &= ~(/*I2C_IBCR_INTERUPT_ENABLE_BIT |*/ I2C_IBCR_MASTER_MODE_BIT | I2C_IBCR_TX_MODE_BIT | I2C_IBCR_TXACK_BIT);
			_I2C_driver_state = I2C_STATE_IDLE;
			_i2cBusyCount = 0;
		}
		return FALSE;
	}
}

uint8_t _i2c_compass_b1, _i2c_compass_b2;

/**
 * Processes the compass data and sends it to the compass component.
 */
void I2CDriver_processCompassData() {
	uint16_t result = ((_i2c_compass_b1<<8) + _i2c_compass_b2);
	Compass_headingReady(result);
	
	// return the state of this driver to be idle
	_I2C_driver_state = I2C_STATE_IDLE;
}

/**
 * This is called from within an interrupt context.
 */
void I2C_updateState() {
	switch(_I2C_driver_state) {
		case I2C_STATE_ADDRESSING_SLAVE:
			// Update the state of this driver
			_I2C_driver_state = I2C_STATE_SENDING_REGISTER_ADDRESS;
			
			// Send the address of the register from which to read.
			IBDR = COMPASS_REGISTER_ADDRESS; 
			break;
		
		case I2C_STATE_SENDING_REGISTER_ADDRESS:
			// By now assume the device was successfully addressed and that
			// the address of the register from which to read was successful.
			_I2C_driver_state = I2C_STATE_SENDING_READ_BIT;
			
			IBCR |= I2C_IBCR_REPEAT_START_BIT; // Send repeated start signal
			IBDR = COMPASS_ADDRESS | 0x01; // Send read bit
			break;
		
		case I2C_STATE_SENDING_READ_BIT:
			// Update the state of this driver
			_I2C_driver_state = I2C_STATE_RECEIVING_FIRST_BYTE;
			
			IBCR &= ~(I2C_IBCR_TX_MODE_BIT | I2C_IBCR_TXACK_BIT); // Set receive mode, disable Tx Ack
			_i2c_compass_b1 = IBDR; // initiates a data receive operation?
			break;
		case I2C_STATE_RECEIVING_FIRST_BYTE:
			_I2C_driver_state = I2C_STATE_RECEIVING_SECOND_BYTE;
			
			IBCR |= I2C_IBCR_TXACK_BIT; // Enable Tx Ack
			_i2c_compass_b1 = IBDR; // Read in the first byte sent from the slave device.
			break;
		case I2C_STATE_RECEIVING_SECOND_BYTE:
			_I2C_driver_state = I2C_STATE_FINISH_READ_COMPASS;
			
			IBCR |= I2C_IBCR_TXACK_BIT; // Enable Tx Ack
			
			// Send the stop bit (disable i2c interrupts, relinquish bus master mode, disable Tx Ack, set to Rx mode)
			IBCR &= ~(/*I2C_IBCR_INTERUPT_ENABLE_BIT |*/ I2C_IBCR_MASTER_MODE_BIT | I2C_IBCR_TX_MODE_BIT | I2C_IBCR_TXACK_BIT); 
			
			_i2c_compass_b2 = IBDR; // Read in the second byte sent from the slave device.
			
			TaskHandler_postTask(&I2CDriver_processCompassData);
			break;
	}
}

interrupt 31 void I2CInterruptHandler(void) {
	uint8_t status = IBSR;
	asm sei  // disable interrupts
	
	IBSR_IBIF = 1; // clear interrupt flag
	
	/*
	 * Check if we lost arbitration of the I2C bus.
	 * If so, resort back to idle state.
	 */
	if (IBSR & I2C_IBSR_ARBITRATION_LOST_BIT) {
		IBSR = I2C_IBSR_ARBITRATION_LOST_BIT; // clear the arbitration lost bit
		
		// abort the operation
		IBCR &= ~(I2C_IBCR_MASTER_MODE_BIT | I2C_IBCR_TX_MODE_BIT | I2C_IBCR_TXACK_BIT); 
		_I2C_driver_state = I2C_STATE_IDLE;
		
		// Report the failure to the compass component
		Compass_I2C_Read_Failed();
	} else {
		if ((status & I2C_IBSR_BUS_INTERRUPT_BIT) && _I2C_driver_state != I2C_STATE_IDLE
			&& _I2C_driver_state != I2C_STATE_FINISH_READ_COMPASS) 
		{
			I2C_updateState();
		} else {
			// received an I2C interrupt when we did not expect it.
			LED_RED1 ^= 1;
		}
	}

	//IBSR = I2C_IBSR_BUS_INTERRUPT_BIT; // reset the interrupt
	
	asm cli // enable interrupts
}