// filename ******** Lynx_Accel.h **************
// A Lynx accel driver for the Proteus Robot


/*
 ** Pin connections **

    PAD10 -> X AXIS
    PAD11 -> Y AXIS



 ** Feature Usage **

   2 pins on ADC0
*/

#ifndef Lynx_Accel_H
#define Lynx_Accel_H  1

#define ADC_X_AXIS 0x8A
#define ADC_Y_AXIS 0x8B
#define ADC_Z_AXIS 0x89

unsigned short x_axis;
unsigned short y_axis;
unsigned short z_axis;


//all return units of mm
unsigned short X_AXIS_get(void); //X
unsigned short Y_AXIS_get(void); //Y
unsigned short Z_AXIS_get(void); //Z

#endif /* _Lynx_Accel_H*/

