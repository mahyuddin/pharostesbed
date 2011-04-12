// filename ******** IR.h **************
// A Sharp IR driver for the Proteus Robot
// For use with Proteus IR Switch V3+ circuit board
// code provided by Cartographer group
// modified by Jasmine Liu

/*
 ** Pin connections **
    
	PAD07 -> Short Range Right Sensor (9)
	PAD08 -> Short Range Front Left Left (1)
	PAD09 -> Long Range Front Left (2)
	PAD10 -> Short Range Front Left Right (3)
	PAD11 -> Long Range Front Center (4)
	PAD12 -> Short Range Front Right Right (7)
	PAD13 -> Long Range Front Right	(6)
	PAD14 -> Short Range Left Sensor (8)
	PAD15 -> Short Range Front Right Left (5) 

 ** Feature Usage **
   
   1 pin on ADC0
   8 pins on ADC1
*/

#ifndef _Sharp_IR_H
#define _Sharp_IR_H 1

#include "Command.h"
#include "adc.h"
#include <mc9s12dp512.h>

#define ADC_IR_1 0x88 
#define ADC_IR_2 0x89
#define ADC_IR_3 0x8A
#define ADC_IR_4 0x8B
#define ADC_IR_5 0x8F
#define ADC_IR_6 0x8D
#define ADC_IR_7 0x8C
#define ADC_IR_8 0x8E
#define ADC_IR_9 0x87

#define IR_SAMPLE_FREQ 1000 

//all return units of mm
unsigned short IR_get1(void);
unsigned short IR_get2(void);
unsigned short IR_get3(void);
unsigned short IR_get4(void);
unsigned short IR_get5(void);
unsigned short IR_get6(void);
unsigned short IR_get7(void);
unsigned short IR_get8(void);
unsigned short IR_get9(void);

#endif /* _Sharp_IR_H */

// A table that converts raw IR data to mm
// For most accurate results, generate this table
// in the environment you are using
// generated in ENS526W (florecent lighting)