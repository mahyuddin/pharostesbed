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

#define NELEMS(x)  (sizeof(x) / sizeof(x[0]))
                   
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

#define Acc2DSpd 1000/INS_SAMPLE_FREQ
// TODO: Create real tables that don't suck
ADC_Accel TableX1[] = {{0,-19620*Acc2DSpd},{1025,19620*Acc2DSpd}};
ADC_Accel TableX2[] = {{0,-19620*Acc2DSpd},{1025,19620*Acc2DSpd}};
//ADC_Accel TableY1[] = {{0,-19620*Acc2DSpd},{373,-9810*Acc2DSpd},{530,0*Acc2DSpd},{674,9810*Acc2DSpd},{1025,19620*Acc2DSpd}};
ADC_Accel TableY1[] = {{0,-19620*Acc2DSpd}, {384, -9806*Acc2DSpd}, {449,-4903*Acc2DSpd}, {474,-3354*Acc2DSpd}, {486,-2538*Acc2DSpd}, {498,-1703*Acc2DSpd}, {515,-855*Acc2DSpd}, {522,-513*Acc2DSpd}, {536,0*Acc2DSpd}, {546,513*Acc2DSpd}, {550,855*Acc2DSpd}, {550,1703*Acc2DSpd}, {566,2538*Acc2DSpd}, {586,3354*Acc2DSpd}, {608,4903*Acc2DSpd}, {1025,19620*Acc2DSpd}};
long int TableY1_0 = 536;
//ADC_Accel TableY2[] = {{0,-19620*Acc2DSpd},{383,-9810*Acc2DSpd},{534,0*Acc2DSpd},{687,9810*Acc2DSpd},{1025,19620*Acc2DSpd}};
ADC_Accel TableY2[] = {{0,-19620*Acc2DSpd},                        {454,-4903*Acc2DSpd}, {477,-3354*Acc2DSpd}, {491,-2538*Acc2DSpd}, {503,-1703*Acc2DSpd}, {522,-855*Acc2DSpd}, {520,-513*Acc2DSpd}, {529,0*Acc2DSpd}, {548,513*Acc2DSpd}, {553,855*Acc2DSpd}, {568,1703*Acc2DSpd}, {577,2538*Acc2DSpd}, {590,3354*Acc2DSpd}, {615,4903*Acc2DSpd}, {688,9806*Acc2DSpd}};
long int TableY2_0 = 529;
// 1 in this = .00001 m/s^2 (before the /INS_SAMPLEFREQ)
// 1 in this = .00001 m/s change from last cycle.

// 1 = .01 deg/sec
ADC_Accel Gyro_Table[] = {{0,-50000},{200,-100},{300,0},{400,100},{676,50000}}; //This table's pretty bad
long int Gyro_Table_0 = 300;
ADC_Accel *ADC_Table[] = {&TableX1, &TableX2, &TableY1, &TableY2, &Gyro_Table};

void INS_Init() {
    long int Y1_0, Y2_0, Rot_0;
    int idx;
    
    // Shift the lookup tables to match the current 'zero'
        // Warning: Assumes that the machine is perfectly still when starting
    ADC0_Init();
    Y1_0 = ADC0_In(2);
    for(idx = 0; idx <  NELEMS(TableY1); idx++)
        TableY1[idx].ADC += Y1_0 - TableY1_0;
        
    Y2_0 = ADC0_In(3);
    for(idx = 0; idx <  NELEMS(TableY2); idx++)
        TableY2[idx].ADC += Y2_0 - TableY2_0;
    
    Rot_0 = ADC0_In(4);
    for(idx = 0; idx <  NELEMS(Gyro_Table); idx++)
        Gyro_Table[idx].ADC += Rot_0 - Gyro_Table_0;
        
    YDisp = 0;
    XDisp = 0;
    GyroDisp = 0;
    YSpeed = 0;

    // Start the interrupt!
    TIOS |= 0x40;    // activate TC6 as output compare
    TIE  |= 0x40;    // arm OC6
    TC6   = TCNT+75; // First interrupt right away.
}
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
            GyroDispr = GyroDispr % (36000);
		}
        if (tickNum >= INS_SAMPLE_FREQ/10 ){
            LED_GREEN2 = 1;
            // This should be in .1 cm or .001 m displacements.
            #if USE_XAXIS
            Command_sendAccelerometerPacket(XDisp, YDisp, GyroDisp);
            #else
            Command_sendAccelerometerPacket(YDisp, GyroDisp);
            //Command_sendAccelerometerPacket(save2, save2);
            #endif
            tick = 0;
            XDisp = 0;
            YDisp = 0;
            LED_GREEN2 = 0;
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
    for (i = 0; i <= 1; i++){
        FIFO_Item putMe;
        putMe.label = i;
        putMe.value = ADC0_In(i);
        putMe.tick  = tick;
        // Actual translation of values is done in main();
        XaxisFifo_Put(putMe);
    }
    /*Used ATD pins in Y */
    for (i = 2;i <= 3; i++){
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