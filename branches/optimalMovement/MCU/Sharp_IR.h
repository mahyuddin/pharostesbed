// filename ******** Sharp_IR.h **************
// IR driver for the Proteus Robot
/*
 ** Pin connections **
    
    PAD00 -> Left Sensor
    PAD01 -> Front Sensor
    PAD02 -> Right Sensor
    

 ** Feature Usage **
   
   3 pins on ADC0
*/

#ifndef _Sharp_IR_H
#define _Sharp_IR_H 1

#define LEFT 0x83 
#define FRONT 0x84
#define RIGHT 0x85

//all return units of mm
unsigned short IR_getLeft(void); //front left
unsigned short IR_getCenter(void); //front center
unsigned short IR_getRight(void); //front right

#endif /* _Sharp_IR_H */