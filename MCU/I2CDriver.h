#ifndef _I2C_DRIVER_H
#define _I2C_DRIVER_H 1


#define I2C_IBCR_BUS_ENABLE_BIT 0x80
#define I2C_IBCR_INTERUPT_ENABLE_BIT 0x40
#define I2C_IBCR_MASTER_MODE_BIT 0X20
#define I2C_IBCR_TX_MODE_BIT 0x10
#define I2C_IBCR_TXACK_BIT 0x08
#define I2C_IBCR_REPEAT_START_BIT 0x04
#define I2C_IBCR_WAIT_MODE_BIT 0x01

// I2C status register bits
#define I2C_IBSR_DATA_TX_BIT 0x80
#define I2C_IBSR_ADDRESSED_AS_SLAVE_BIT 0x40  // the MCU should never be addressed as a slave
#define I2C_IBSR_BUS_BUSY_BIT 0x20
#define I2C_IBSR_ARBITRATION_LOST_BIT 0x10
#define I2C_IBSR_SLAVE_READ_WRITE_BIT 0x04
#define I2C_IBSR_BUS_INTERRUPT_BIT 0x02
#define I2C_IBSR_ACK_RECIEVED_BIT 0x01

#include "Types.h"

enum {
	I2C_STATE_IDLE,
	I2C_STATE_ADDRESSING_SLAVE,
	I2C_STATE_SENDING_REGISTER_ADDRESS,
	I2C_STATE_SENDING_READ_BIT,
	I2C_STATE_RECEIVING_FIRST_BYTE,
	I2C_STATE_RECEIVING_SECOND_BYTE,
	I2C_STATE_FINISH_READ_COMPASS
};

/**
 * Initializes the I2C bus.  Enables the bus, sets the correct clock frequency,
 * and clears the interrupt.
 */
void I2CDriver_init(void);

/**
 * Initiates the process of reading the compass.
 *
 * TODO: generalize this driver by using call-back function pointers.
 */
bool I2CDriver_readCompass(void);

#endif /* _I2C_DRIVER_H */