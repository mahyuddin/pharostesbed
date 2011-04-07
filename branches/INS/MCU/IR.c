// Original code by Francis Israel
// Modified by Jasmine Liu

#include "IR.h"
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

    // 9 separate IR sensors fifo's

    AddFifo(IR1_, 40, unsigned short, 1, 0); 
    AddFifo(IR2_, 40, unsigned short, 1, 0); 
    AddFifo(IR3_, 40, unsigned short, 1, 0);
	AddFifo(IR4_, 40, unsigned short, 1, 0); 
    AddFifo(IR5_, 40, unsigned short, 1, 0);
    AddFifo(IR6_, 40, unsigned short, 1, 0);
    AddFifo(IR7_, 40, unsigned short, 1, 0); 
	AddFifo(IR8_, 40, unsigned short, 1, 0);
	AddFifo(IR9_, 40, unsigned short, 1, 0);
   
    
   IR_Init() {
   	  ADC0_Init(); // need to integrate initisalization with INS
      ADC1_Init();
      TIOS  |= 0x01; // TC5 is output compare
      TIE   |= 0x01;
      TC1   = TCNT + 100; // interrupt immediately
    }
    
   interrupt 8 void IRPeriodic(void){
    unsigned short input;
    char i = 0;
    TFLG1 = 0x01;         // acknowledge OC5
    TC1 = TC1 + 3000300 / IR_SAMPLE_FREQ;    // 3 000 300 = 10 000 000 000 / 3 333

    // Read ADC inputs, put into appropriate FIFO.
    // unsigned short input = ADC1_In(0);
    
    /*Used AD0 pin 07 and AD1 pins 08-15 */
    //LED_GREEN1 = 1;
    
	input = ADC1_In(ADC_IR_1); // IR0
    IR1_Fifo_Put(input);

    input = ADC1_In(ADC_IR_2); // IR1,4
    IR2_Fifo_Put(input);
    
    input = ADC1_In(ADC_IR_3); // IR2,6
    IR3_Fifo_Put(input);
    
    input = ADC1_In(ADC_IR_4); // IR3
    IR4_Fifo_Put(input);
    
    input = ADC1_In(ADC_IR_5); // IR5
    IR5_Fifo_Put(input);

	input = ADC1_In(ADC_IR_6); // IR5
    IR6_Fifo_Put(input);
    
	input = ADC1_In(ADC_IR_7); // IR7
    IR7_Fifo_Put(input);
	
	input = ADC1_In(ADC_IR_8); // IR8
    IR8_Fifo_Put(input);
	
	input = ADC0_In(ADC_IR_9); // IR8
    IR9_Fifo_Put(input);	
  }
  
  // Foreground thread:
  // todo translate values into positions around the vehicle
  // separate out combined sensor readings
  // readings will return as -1 if fifo is empty
  short* IRTranslate(){
    const short divider = 128; //division between sensor readings in combined IR
    short ir[9] = {0};
    unsigned long x1,x2,x3,x5,x6,x7,x8,x9; // no x4 
    unsigned long y1,y2,y3,y4,y5,y6,y7; // no y8-9 because of positioning
    
    
    if(IR1_Fifo_Get(&ir[1]) == 1){
		
    }else{ ir[1] = -1;}
    if(IR2_Fifo_Get(&ir[2]) == 1){

    }else{ ir[2] = -1;}
    if(IR3_Fifo_Get(&ir[3]) == 1){

    }else{ ir[3] = -1;}
    if(IR4_Fifo_Get(&ir[4]) == 1){

    }else{ ir[4] = -1;}
    if(IR5_Fifo_Get(&ir[5]) == 1){
      
    }else{ ir[5] = -1;}
	if(IR6_Fifo_Get(&ir[6]) == 1){
      
    }else{ ir[6] = -1;}
    if(IR7_Fifo_Get(&ir[7]) == 1){
      
    }else{ ir[7] = -1;}
	if(IR8_Fifo_Get(&ir[8]) == 1){
      
    }else{ ir[8] = -1;}
	if(IR9_Fifo_Get(&ir[9]) == 1){
      
    }else{ ir[9] = -1;}
   
   return ir;    
  }