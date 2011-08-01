#ifndef _TYPES_H
#define _TYPES_H 1

typedef signed char int8_t;
typedef unsigned char uint8_t;
typedef signed short int16_t;
typedef unsigned short uint16_t;
typedef signed long int32_t;
typedef unsigned long uint32_t;
typedef uint8_t bool;

#define FALSE 0
#define TRUE 1

#define ON 1
#define OFF 0

// ECT status flags
#define TFLG1_CH0_INT8_BIT 0x01
#define TFLG1_CH1_INT9_BIT 0x02
#define TFLG1_CH2_INT10_BIT 0x04
#define TFLG1_CH3_INT11_BIT 0x08
#define TFLG1_CH4_INT12_BIT 0x10
#define TFLG1_CH5_INT13_BIT 0x20
#define TFLG1_CH6_INT14_BIT 0x40
#define TFLG1_CH7_INT15_BIT 0x80

// ECT interrupt enable flags
#define TIE_CH0_INT8_BIT TFLG1_CH0_INT8_BIT
#define TIE_CH1_INT9_BIT TFLG1_CH1_INT9_BIT
#define TIE_CH2_INT10_BIT TFLG1_CH2_INT10_BIT
#define TIE_CH3_INT11_BIT TFLG1_CH3_INT11_BIT
#define TIE_CH4_INT12_BIT TFLG1_CH4_INT12_BIT
#define TIE_CH5_INT13_BIT TFLG1_CH5_INT13_BIT
#define TIE_CH6_INT14_BIT TFLG1_CH6_INT14_BIT
#define TIE_CH7_INT15_BIT TFLG1_CH7_INT15_BIT

// ECT output compare flags
#define TIOS_CH0_INT8_BIT TFLG1_CH0_INT8_BIT
#define TIOS_CH1_INT9_BIT TFLG1_CH1_INT9_BIT
#define TIOS_CH2_INT10_BIT TFLG1_CH2_INT10_BIT
#define TIOS_CH3_INT11_BIT TFLG1_CH3_INT11_BIT
#define TIOS_CH4_INT12_BIT TFLG1_CH4_INT12_BIT
#define TIOS_CH5_INT13_BIT TFLG1_CH5_INT13_BIT
#define TIOS_CH6_INT14_BIT TFLG1_CH6_INT14_BIT
#define TIOS_CH7_INT15_BIT TFLG1_CH7_INT15_BIT

// The following assume TCNT increments every 333ns
#define TCNT_100US_INTERVAL 0x012C // 300 * 333ns = 100us
#define TCNT_10MS_INTERVAL 0x754E // 30030 * 333ns = 10ms
#define TCNT_20MS_INTERVAL 0xEA9C // 60060 * 333ns = 20ms

// This is used to distinguish the type of compass data
enum {
	I2C_DATA,
	PWM_DATA,
};

#endif
