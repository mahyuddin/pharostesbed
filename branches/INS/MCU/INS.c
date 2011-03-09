// Code by Francis Rei Lao Israel
// Tries to model same style as Paine {n.a.paine@gmail.com}

#include "INS.h"
#include "LED.h"


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
			}													 \
			int NAME ## Fifo_Peak (TYPE *datapt){                \
			    if( PutI ## NAME == GetI ## NAME ){              \
			        return(FAIL);                                \
			    }                                                \
			    *datapt = Fifo ## NAME[ GetI ## NAME &(SIZE-1)]; \
			    return(SUCCESS);                                 \
			}													 \
			int NAME ## Fifo_Remove (){                          \
			    if( PutI ## NAME == GetI ## NAME ){              \
			        return(FAIL);                                \
			    }                                                \
			    GetI ## NAME ## ++;                              \
			    return(SUCCESS);                                 \
			}
AddFifo(Xaxis, 40, FIFO_Item, 1, 0);
AddFifo(Yaxis, 40, FIFO_Item, 1, 0);
AddFifo(Gyro, 40, FIFO_Item, 1, 0);

void INS_Init() {
    ADC0_Init();
    TIOS |= 0x40;    // activate TC6 as output compare
    TIE  |= 0x40;    // arm OC6
    TC6   = TCNT+75; // First interrupt right away.
}


// TODO: Create real tables that don't suck
ADC_Accel TableX1[] = {{0,-19620},{1025,19620}};
ADC_Accel TableX2[] = {{0,-19620},{1025,19620}};
ADC_Accel TableY1[] = {{0,-19620},{1025,19620}};
ADC_Accel TableY2[] = {{0,-19620},{1025,19620}};

ADC_Accel Gyro_Table[] = {{0,-50000},{676,50000}};
ADC_Accel *ADC_Table[] = {&TableX1, &TableX2, &TableY1, &TableY2, &Gyro_Table};

// Foreground thread:
// Does ADC translation, pipes the translated info to the 
void INSPeriodicFG(){
    // Pull values from FIFO
    FIFO_Item output;
	int Xacc;
	int Yacc;
	int GyroRate;
	unsigned short tick;
	if (XaxisFifo_Get(&output) == 1) {
		LED_RED1 = 1;
	    tick = output.tick;
		Xacc = INS_Translate(output.value, ADC_Table[output.label]);
		if (XaxisFifo_Peak(&output) == 1){
			if (output.tick ==tick){
				XaxisFifo_Remove();
				Xacc += INS_Translate(output.value, ADC_Table[output.label]);
				Xacc = Xacc >> 1;
			}
		}
		if (YaxisFifo_Get(&output) == 1) {
			Yacc = INS_Translate(output.value, ADC_Table[output.label]);
			if (YaxisFifo_Peak(&output) == 1){
				if (output.tick ==tick){
					YaxisFifo_Remove();
					Yacc += INS_Translate(output.value, ADC_Table[output.label]);
					Yacc = Yacc >> 1;
				}
			}
		}
		
		if (GyroFifo_Get(&output) == 1 ){
			// Translate and output value to the x86.
			GyroRate = INS_Translate(output.value, ADC_Table[output.label]);
		}
		
		Command_sendAccelerometerPacket(tick, Xacc, Yacc, GyroRate);
		LED_RED1 = 0;
	}
}


interrupt 14 void INSPeriodicBG(void){
    // Read the relevant ADC inputs, shove onto a FIFO.
    // unsigned short input = ADC0_In(0);
    int i = 0;
    static unsigned short tick = 0;
    TFLG1 = 0x40;         // acknowledge OC6
    TC6 = TC6 + 3000300 / INS_SAMPLE_FREQ;    // 3 000 300 = 10 000 000 000 / 3 333
    /*Used ATD pins in X */
    LED_GREEN1 = 1;
    for (i = 0; i < 2; i++){
        FIFO_Item putMe;
        putMe.label = i;
        putMe.value = ADC0_In(i);
        putMe.tick  = tick;
        // Actual translation of values is done in main();
        XaxisFifo_Put(putMe);
    }
    /*Used ATD pins in Y */
    for (i = 2;i < 4; i++){
        FIFO_Item putMe;
        putMe.label = i;
        putMe.value = ADC0_In(i);
        putMe.tick  = tick;
        // Actual translation of values is done in main();
        YaxisFifo_Put(putMe);
    }
    /*ATD pins for gyroscope*/
    for(i = 4;i == 4;i++){
        FIFO_Item putMe;
        putMe.label = i;
        putMe.value = ADC0_In(i);
        putMe.tick  = tick;
        tick++;
        // Actual translation of values is done in main();
        GyroFifo_Put(putMe);
    }
	tick++;
    //TaskHandler_postTask(&INSPeriodicFG);
    LED_GREEN1 = 0;
	
}




// input: The ADC output which we are trying to convert
// TableStart: Should be a pointer to the beginning of a translation array.
//      Array should be in increasing order.
// !!!WARNING!!!
//      Make sure this ADC array covers all posible values of input (0-2^10 for the 9S12 ADC)
//      Otherwise, you run the risk of an infinite loop as the function spins off into infinity.
int INS_Translate(unsigned short translate, ADC_Accel *TableStart){
    int result;
    ADC_Accel* TTE1;
    ADC_Accel *TTE2;
    TTE1 = TableStart;
    TTE2 = TableStart + 1;
    // Find the two TTE (Translation Table Entries) which the input lies between
    while ( ! (TTE1-> ADC <= translate && translate <  TTE2->ADC)){
        TTE1++;
        TTE2++;
    }
    // Linear interpolation between the two TTEs
        // Multiply first, or else you'll get bitten by truncation
    result = ( (TTE2->X - TTE1->X) * (translate - TTE1->ADC)    )   /   (TTE2->ADC - TTE1->ADC);
    return result + TTE1->X;
}