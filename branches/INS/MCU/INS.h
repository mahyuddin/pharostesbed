#include "Command.h"
#include "adc.h"
#include <mc9s12dp512.h>


#define INS_SAMPLE_FREQ 500

#define USE_XAXIS 0

// Making a table from 10-bit ADC to M/S^2
// Accel is in signed, fixed point, where 1 = 0.001 M/S^2
typedef struct ADC_Accel {
    short ADC;
    long   X;
} ADC_Accel;

typedef struct FIFO_Item {
    char label;
    int  value;
    unsigned short tick;   
} FIFO_Item;    




void INS_Init(void);

void INSPeriodicFG();
void INSPeriodicBG();

// input: The ADC output which we are trying to convert
// TableStart: Should be a pointer to the beginning of a translation array.
//      Array should be in increasing order.
int INS_Translate(unsigned short translate, ADC_Accel *TableStart);