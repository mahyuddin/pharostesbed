// filename ******** Sharp_IR.h **************
// IR driver for the Proteus Robot
// Raw 8-bit ADC data are read and returned
// Value: 0-255
// Mapping: 0-5 Volts
/*
 ** Pin connections **
    
    PAD00 -> Left Sensor
    PAD01 -> Front Sensor
    PAD02 -> Right Sensor
    

 ** Feature Usage **

    3 pins on ADC0
*/

#include <mc9s12dp512.h>     /* derivative information */
#include "adc.h"
#include "Sharp_IR.h"

/**
 *Table used for IR sensor validation
  unsigned short temp_lookup_table[10] = {
  2.44, //20cm
  2.14, //25cm
  1.89, //30cm
  1.65, //35cm
  1.47, //40cm
  1.30, //45cm
  1.19, //50cm
  1.05; //55cm
  0.98; //60cm
  0.83; //65cm
}
*/

unsigned short IR_getLeft(void){
  unsigned short raw;
  raw = ADC_In(LEFT);
  return raw; //Left sensor (mounted right, facing left)
}

unsigned short IR_getFront(void){
  unsigned short raw;
  raw = ADC_In(FRONT);
  return raw; //Front sensor
}

unsigned short IR_getRight(void){
  unsigned short raw;
  raw = ADC_In(RIGHT);
  return raw; //Right sensor (mounted left, facing right)
}


   


