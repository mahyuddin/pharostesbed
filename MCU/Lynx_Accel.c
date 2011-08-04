// filename ******** Lynx_Accel.c **************
// A Lynx Accelerometer driver for the Proteus Robot
// For use with
//Lynxmotion Buffered ±2g Accelerometer

/*
 ** Pin connections **

 PAD10 -> X AXIS
 PAD11 -> Y AXIS


 ** Feature Usage **

   2 pins on ADC0
*/

#include <mc9s12dp512.h>     /* derivative information */
#include "adc.h"
#include "Lynx_Accel.h"




unsigned short X_AXIS_get(void){


  return ADC0_In(ADC_X_AXIS);


}

unsigned short Y_AXIS_get(void){


  return ADC0_In(ADC_Y_AXIS);



}unsigned short Z_AXIS_get(void){


  return ADC0_In(ADC_Z_AXIS);



} 