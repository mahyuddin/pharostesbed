// Code modified from Francis Israel

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

    // 9 separate IR but 01, 46, 23, 78 are pairs

    AddFifo(IR01_, 40, unsigned short, 1, 0); // long and short
    AddFifo(IR46_, 40, unsigned short, 1, 0); // two short range
    AddFifo(IR23_, 40, unsigned short, 1, 0); // long and short
    AddFifo(IR5_, 40, unsigned short, 1, 0); // front IR sensor long distance  
    AddFifo(IR78_, 40, unsigned short, 1, 0); // side IR sensors left and right
   
    
   IR_Init() {
      ADC1_Init();
      TIOS  |= 0x20; // TC5 is output compare
      TIE   |= 0x20;
      TC5   = TCNT + 100; // interrupt immediately
    }
    
   interrupt 13 void IRPeriodic(void){
    unsigned short input;
    TFLG1 = 0x20;         // acknowledge OC5
    TC5 = TC5 + 3000300 / IR_SAMPLE_FREQ;    // 3 000 300 = 10 000 000 000 / 3 333

    // Read ADC inputs, put into appropriate FIFO.
    // unsigned short input = ADC1_In(0);
    char i = 0;
    /*Used ATD pins 0-4 */
    //LED_GREEN1 = 1;
    
    input = ADC0_In(0); // IR01
    IR01_Fifo_Put(input);
    
    input = ADC0_In(1); // IR46
    IR46_Fifo_Put(input);
    
    input = ADC0_In(2); // IR23
    IR23_Fifo_Put(input);
    
    input = ADC0_In(3); // IR5
    IR5_Fifo_Put(input);
    
    input = ADC0_In(4); // IR78
    IR78_Fifo_Put(input);
	
  }
  
  // Foreground thread:
  // todo translate values into positions around the vehicle
  // separate out combined sensor readings
  
  void IRTranslate(){
    const short divider = 128; //division between sensor readings in combined IR
    short ir01, ir23, ir46 ir5, ir78;
    unsigned long x1,x2,x3,x4,x6,x7,x8; // no x5 
    unsigned long y1,y2,y3,y4,y5,y6; // no y7,y8 because of positioning
    
    
    if(IR01_Fifo_Get(&ir01) == 1){
    }
    if(IR23_Fifo_Get(&ir01) == 1){
    }
    if(IR46_Fifo_Get(&ir01) == 1){
    }
    if(IR5_Fifo_Get(&ir5) == 1){
    }
    if(IR78_Fifo_Get(&ir78) == 1){
      
    }
    
       
  }