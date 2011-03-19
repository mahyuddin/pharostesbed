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

unsigned short tick = 0;
signed short XDisp=0;
signed short YDisp=0;
signed short GyroDisp=0;
signed short GyroDispr=0;

signed long YSpeed=0;
signed int YDispr=0;

#if USE_XAXIS
signed long XSpeed=0;
signed int XDispr=0;
#endif

            
AddFifo(Xaxis, 40, FIFO_Item, 1, 0);
AddFifo(Yaxis, 40, FIFO_Item, 1, 0);
AddFifo(Gyro, 40, FIFO_Item, 1, 0);

void INS_Init() {
    ADC0_Init();
    TIOS |= 0x40;    // activate TC6 as output compare
    TIE  |= 0x40;    // arm OC6
    TC6   = TCNT+75; // First interrupt right away.
}

#define Acc2DSpd 1000/INS_SAMPLEFREQ
// TODO: Create real tables that don't suck
ADC_Accel TableX1[] = {{0,-19620*Accc2DSpd},{1025,19620*Accc2DSpd}};
ADC_Accel TableX2[] = {{0,-19620*Accc2DSpd},{1025,19620*Accc2DSpd}};
ADC_Accel TableY1[] = {{0,-19620*Accc2DSpd},{373,-9810*Accc2DSpd},{530,0*Accc2DSpd},{674,9810*Accc2DSpd},{1025,19620*Accc2DSpd}};
ADC_Accel TableY2[] = {{0,-19.620*Accc2DSpd},{383,-9810*Accc2DSpd},{534,0*Accc2DSpd},{687,9810*Accc2DSpd},{1025,19620*Accc2DSpd}};
// 1 in this = .00001 m/s^2 (before the /INS_SAMPLEFREQ)
// 1 in this = .00001 m/s change from last cycle.

// 1 = .01 deg/sec
ADC_Accel Gyro_Table[] = {{0,-50000},{200,-100},{300,0},{400,100},{676,50000}}; //This table's pretty bad
ADC_Accel *ADC_Table[] = {&TableX1, &TableX2, &TableY1, &TableY2, &Gyro_Table};


// Foreground thread:
// Does ADC translation, pipes the translated info to the 
void INSPeriodicFG(){
    // Pull values from FIFO
    FIFO_Item output;        
    unsigned short save, save2;
	signed short Xacc;
	signed short Yacc;
	signed short GyroRate;
	unsigned short tickNum;
	if (YaxisFifo_Get(&output) == 1) {
		LED_RED1 = 1;
	    tickNum = output.tick;
		Yacc = INS_Translate(output.value, TableY1);
		save = output.value;
        
		if (YaxisFifo_Peak(&output) == 1){
			if (output.tick ==tickNum){
				save2 = output.value;
				YaxisFifo_Remove();
				Yacc += INS_Translate(output.value, TableY2);
				Yacc = Yacc >> 1;
			}
		}
        YSpeed += Yacc;
        YDisp += (YSpeed + YDispr) /  (100 * INS_SAMPLE_FREQ);
        YDispr = (YSpeed + YDispr) %  (100 * INS_SAMPLE_FREQ);
        
        #if USE_XAXIS
		if (XaxisFifo_Get(&output) == 1) {
			Xacc = INS_Translate(output.value, TableX1);
			if (XaxisFifo_Peak(&output) == 1){
				if (output.tick ==tickNum){
					XaxisFifo_Remove();
					Xacc += INS_Translate(output.value, TableX2);
					Xacc = Xacc >> 1;
				}
			}
		}
        XSpeed += Xacc;
        YDisp += (YSpeed + YDispr)/INS_SAMPLE_FREQ;
        YDispr = (YSpeed + YDispr) % INS_SAMPLE_FREQ;
        #endif
		
		if (GyroFifo_Get(&output) == 1 ){
			// Translate and output value to the x86.
			GyroRate = INS_Translate(output.value, Gyro_Table);
            GyroDisp  += (GyroRate + GyroDispr)/INS_SAMPLE_FREQ;
            GyroDispr += (GyroRate + GyroDispr)%INS_SAMPLE_FREQ;
            
		}
        if (tickNum >= INS_SAMPLE_FREQ/10 ){
            // This should be in .1 cm or .001 m displacements.
            #if USE_XAXIS
            Command_sendAccelerometerPacket(XDisp, YDisp, GyroDisp);
            #else
            Command_sendAccelerometerPacket(YDisp, GyroDisp);
            #endif
            tick = 0;
            XDisp = 0;
            YDisp = 0;
        }
		LED_RED1 = 0;
	}
}

char t = 0;
interrupt 14 void INSPeriodicBG(void){
    TFLG1 = 0x40;         // acknowledge OC6
    TC6 = TC6 + 3000300 / INS_SAMPLE_FREQ;    // 3 000 300 = 10 000 000 000 / 3 333
    if(1){//t++ > 4){
    // Read the relevant ADC inputs, shove onto a FIFO.
    // unsigned short input = ADC0_In(0);
    char i = 0;
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
        // Actual translation of values is done in main();
        GyroFifo_Put(putMe);
    }
	tick++;
    //TaskHandler_postTask(&INSPeriodicFG);
    LED_GREEN1 = 0;
    t = 1;
    }
	
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