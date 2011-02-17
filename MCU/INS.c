// Code by Francis Rei Lao Israel
// Tries to model same style as Paine {n.a.paine@gmail.com}

#include "INS.h"

#define AddFifo(NAME,SIZE,TYPE, SUCCESS,FAIL) \
			unsigned long volatile PutI ## NAME;  \
			unsigned long volatile GetI ## NAME;  \
			TYPE static Fifo ## NAME [SIZE];      \
			void NAME ## Fifo_Init(void){         \
			    PutI ## NAME= GetI ## NAME = 0;   \
			}                                     \
			int NAME ## Fifo_Put (TYPE data){     \
			    if(( PutI ## NAME - GetI ## NAME ) & ~(SIZE-1)){  \
			        return(FAIL);                                 \
			    }                                                 \
			    Fifo ## NAME[ PutI ## NAME &(SIZE-1)] = data;     \
			    PutI ## NAME ## ++;                               \
			    return(SUCCESS);                                  \
			}                                                     \
			int NAME ## Fifo_Get (TYPE *datapt){                 \
			    if( PutI ## NAME == GetI ## NAME ){              \
			        return(FAIL);                                \
			    }                                                \
			    *datapt = Fifo ## NAME[ GetI ## NAME &(SIZE-1)]; \
			    GetI ## NAME ## ++;                              \
			    return(SUCCESS);                                 \
			}
AddFifo(Xaxis, 40, FIFO_Item, 1, 0);
AddFifo(Yaxis, 40, FIFO_Item, 1, 0);

void INS_Init() {
    ADC1_Init();
    TIOS |= 0x02;    // activate TC1 as output compare
    TIE  |= 0x02;    // arm OC1
    TC1   = TCNT+75; // First interrupt right away.
}


// TODO: Create real tables that don't suck
ADC_Accel ADC0_Table[] = {{0,-2000},{1025,2000}};
ADC_Accel ADC1_Table[] = {{0,-2000},{1025,2000}};
ADC_Accel *ADC_Table[&ADC0_Table, &ADC1_Table]

// Foreground thread:
// Does ADC translation, pipes the translated info to the 
void INSPeriodicFG(){
    // Pull values from FIFO
    FIFO_Item output;
    double sum = 0;
    int accelAverage = 0;
    int itemCount =0;
    while (XaxisFifo_Get(&output) == 1 ){
        // Translate and output value to the x86.
        int value = INS_Translate(output.value, ADC_Table[output.label]);
        itemCount ++;
        sum += value;
    }
    accelAverage = sum / itemCount;
	sendAccelerometerPacket( (uint8_t)output.tick, 0, (uint16_t) accelAverage);
    
    sum = 0;
    accelAverage = 0;
    itemCount =0;
    while (YaxisFifo_Get(&output) == 1 ){
        // Translate and output value to the x86.
        int value = INS_Translate(output.value, ADC_Table[output.label]);
        itemCount ++;
        sum += value;
    }
    accelAverage = sum / itemCount;
	sendAccelerometerPacket( (uint8_t) output.tick, 1, (uint16_t) accelAverage);
}


interrupt 9 void INSPeriodicBG(void){
    // Read the relevant ADC inputs, shove onto a FIFO.
    // unsigned short input = ADC1_In(0);
    // TODO: Figure out what ATD pins are available.
    TFLG1 = 0x02;         // acknowledge OC1
	TC1 = TCNT + 10000000000 / (SAMPLEFREQ * 3333);
    static unsigned short tick = 0;
    int i = 0;
    for (;0/*Used ATD pins in X */; i++){
        FIFO_Item putMe;
        putMe.label = i;
        putMe.value = ADC1_In(i);
        putMe.tick  = tick;
        tick++;
        // Actual translation of values is done in main();
        XaxisFifo_Put();
    }
    for (;0/*Used ATD pins in Y */; i++){
        FIFO_Item putMe;
        putMe.label = i;
        putMe.value = ADC1_In(i);
        putMe.tick  = tick;
        tick++;
        // Actual translation of values is done in main();
        YaxisFifo_Put();
    }
    TaskHandler_postTask(&INSPeriodicFG);
	
}




// input: The ADC output which we are trying to convert
// TableStart: Should be a pointer to the beginning of a translation array.
//      Array should be in increasing order.
// !!!WARNING!!!
//      Make sure this ADC array covers all posible values of input (0-2^10 for the 9S12 ADC)
//      Otherwise, you run the risk of an infinite loop as the function spins off into infinity.
int INS_Translate(unsigned short translate, ADC_Accel *TableStart){
    int result;
    int a_1, a_2, v_1, v2;
    ADC_Accel TTE1, TTE2;
    TTE1 = TableStart;
    TTE2 = TableStart + 1;
    // Find the two TTE (Translation Table Entries) which the input lies between
    while !((TTE1-> ADC <= translate && translate <  TTE2->ADC)){
        TTE1++;
        TTE2++;
    }
    // Linear interpolation between the two TTEs
        // Multiply first, or else you'll get bitten by truncation
    result = ( (TTE2->Accel - TTE1->Accel) * (translate - TTE1->ADC)    )   /   (TTE2->ADC - TTE1->ADC);
    return result + TTE1->Accel;
}
