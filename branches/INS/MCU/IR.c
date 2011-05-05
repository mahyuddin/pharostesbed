// Original code by Francis Israel
// Modified by Jasmine Liu

#include "IR.h"
#include "LED.h"
#include "command.h"

static short ir[10] = {0};

unsigned short IR_default_tab[256] = {	 // these values need to be modified depending on long range or short range sensors
10651	,  //  0
10507	,  //  1
10363	,  //  2
10219	,  //  3
10075	,  //  4
9931	,  //  5
9779	,  //  6
9627	,  //  7
9474	,  //  8
9322	,  //  9
9169	,  //  10
9014	,  //  11
8860	,  //  12
8705	,  //  13
8550	,  //  14
8395	,  //  15
8230	,  //  16
8065	,  //  17
7899	,  //  18
7734	,  //  19
7569	,  //  20
7346	,  //  21
7122	,  //  22
6899	,  //  23
6675	,  //  24
6452	,  //  25
6270	,  //  26
6088	,  //  27
5906	,  //  28
5723	,  //  29
5541	,  //  30
5359	,  //  31
5226	,  //  32
5093	,  //  33
4959	,  //  34
4826	,  //  35
4676	,  //  36
4526	,  //  37
4376	,  //  38
4227	,  //  39
4077	,  //  40
3992	,  //  41
3907	,  //  42
3823	,  //  43
3738	,  //  44
3653	,  //  45
3569	,  //  46
3483	,  //  47
3397	,  //  48
3312	,  //  49
3226	,  //  50
3160	,  //  51
3094	,  //  52
3028	,  //  53
2962	,  //  54
2896	,  //  55
2838	,  //  56
2781	,  //  57
2724	,  //  58
2667	,  //  59
2610	,  //  60
2553	,  //  61
2515	,  //  62
2477	,  //  63
2438	,  //  64
2400	,  //  65
2362	,  //  66
2318	,  //  67
2273	,  //  68
2229	,  //  69
2184	,  //  70
2159	,  //  71
2134	,  //  72
2108	,  //  73
2083	,  //  74
2057	,  //  75
2032	,  //  76
2004	,  //  77
1976	,  //  78
1948	,  //  79
1920	,  //  80
1892	,  //  81
1870	,  //  82
1848	,  //  83
1826	,  //  84
1803	,  //  85
1782	,  //  86
1760	,  //  87
1739	,  //  88
1717	,  //  89
1695	,  //  90
1673	,  //  91
1651	,  //  92
1629	,  //  93
1607	,  //  94
1590	,  //  95
1573	,  //  96
1556	,  //  97
1539	,  //  98
1522	,  //  99
1505	,  //  100
1490	,  //  101
1474	,  //  102
1459	,  //  103
1444	,  //  104
1429	,  //  105
1417	,  //  106
1406	,  //  107
1394	,  //  108
1383	,  //  109
1372	,  //  110
1358	,  //  111
1344	,  //  112
1330	,  //  113
1316	,  //  114
1302	,  //  115
1290	,  //  116
1279	,  //  117
1267	,  //  118
1256	,  //  119
1245	,  //  120
1229	,  //  121
1214	,  //  122
1199	,  //  123
1184	,  //  124
1168	,  //  125
1159	,  //  126
1149	,  //  127
1140	,  //  128
1130	,  //  129
1121	,  //  130
1111	,  //  131
1106	,  //  132
1102	,  //  133
1097	,  //  134
1092	,  //  135
1081	,  //  136
1070	,  //  137
1058	,  //  138
1047	,  //  139
1036	,  //  140
1024	,  //  141
1013	,  //  142
1002	,  //  143
991	,  //  144
983	,  //  145
975	,  //  146
968	,  //  147
960	,  //  148
953	,  //  149
929	,  //  150
905	,  //  151
881	,  //  152
857	,  //  153
843	,  //  154
830	,  //  155
816	,  //  156
802	,  //  157
788	,  //  158
775	,  //  159
759	,  //  160
744	,  //  161
729	,  //  162
714	,  //  163
699	,  //  164
682	,  //  165
666	,  //  166
650	,  //  167
633	,  //  168
617	,  //  169
601	,  //  170
584	,  //  171
579	,  //  172
574	,  //  173
569	,  //  174
564	,  //  175
559	,  //  176
548	,  //  177
537	,  //  178
525	,  //  179
514	,  //  180
509	,  //  181
504	,  //  182
498	,  //  183
493	,  //  184
488	,  //  185
483	,  //  186
476	,  //  187
470	,  //  188
464	,  //  189
457	,  //  190
451	,  //  191
445	,  //  192
438	,  //  193
432	,  //  194
425	,  //  195
422	,  //  196
418	,  //  197
414	,  //  198
410	,  //  199
406	,  //  200
401	,  //  201
396	,  //  202
391	,  //  203
386	,  //  204
381	,  //  205
377	,  //  206
373	,  //  207
368	,  //  208
364	,  //  209
360	,  //  210
356	,  //  211
352	,  //  212
349	,  //  213
346	,  //  214
343	,  //  215
340	,  //  216
338	,  //  217
335	,  //  218
333	,  //  219
330	,  //  220
325	,  //  221
320	,  //  222
315	,  //  223
310	,  //  224
305	,  //  225
302	,  //  226
300	,  //  227
297	,  //  228
295	,  //  229
292	,  //  230
290	,  //  231
287	,  //  232
284	,  //  233
282	,  //  234
279	,  //  235
277	,  //  236
274	,  //  237
272	,  //  238
269	,  //  239
267	,  //  240
264	,  //  241
262	,  //  242
259	,  //  243
257	,  //  244
254	,  //  245
251	,  //  246
249	,  //  247
246	,  //  248
244	,  //  249
241	,  //  250
236	,  //  251
231	,  //  252
226	,  //  253
221	,  //  254
216	,  //  255
 };

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
    
   interrupt 1 void IRPeriodic(void){
    unsigned short input;
    char i = 0;
    TFLG1 = 0x01;         // acknowledge OC5
    TC1 = TC1 + 3000300 / IR_SAMPLE_FREQ;    // 3 000 300 = 10 000 000 000 / 3 333

    // Read ADC inputs, put into appropriate FIFO.
    // unsigned short input = ADC1_In(0);
    
    /*Used AD0 pin 07 and AD1 pins 08-15 */
    //LED_GREEN1 = 1;
    
	input = ir[1] = ADC1_In(ADC_IR_1); // IR0
    IR1_Fifo_Put(input);

    input = ir[2] = ADC1_In(ADC_IR_2); // IR1,4
    IR2_Fifo_Put(input);
    
    input = ir[3] = ADC1_In(ADC_IR_3); // IR2,6
    IR3_Fifo_Put(input);
    
    input = ir[4] = ADC1_In(ADC_IR_4); // IR3
    IR4_Fifo_Put(input);
    
    input = ir[5] = ADC1_In(ADC_IR_5); // IR5
    IR5_Fifo_Put(input);

	input = ir[6] = ADC1_In(ADC_IR_6); // IR5
    IR6_Fifo_Put(input);
    
	input = ir[7] = ADC1_In(ADC_IR_7); // IR7
    IR7_Fifo_Put(input);
	
	input = ir[8] = ADC1_In(ADC_IR_8); // IR8
    IR8_Fifo_Put(input);
	
	input = ir[9] = ADC0_In(ADC_IR_9); // IR8
    IR9_Fifo_Put(input);
    
    Command_sendIRPacket();	
  }
  
  
  // Foreground thread:
  // todo translate values into positions around the vehicle
  // separate out combined sensor readings
  // readings will return as -1 if fifo is empty
  short* IRTranslate(){
    const short divider = 128; //division between sensor readings in combined IR
    
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
 
  unsigned short IR_get1(void){
    return IR_default_tab[ir[1]];
  }
  unsigned short IR_get2(void){
    return IR_default_tab[ir[2]];
  }
  unsigned short IR_get3(void){
    return IR_default_tab[ir[3]];
  }
  unsigned short IR_get4(void){
    return IR_default_tab[ir[4]];
  }
  unsigned short IR_get5(void){
    return IR_default_tab[ir[5]];
  }
  unsigned short IR_get6(void){
    return IR_default_tab[ir[6]];
  }
  unsigned short IR_get7(void){
    return IR_default_tab[ir[7]];
  }
  unsigned short IR_get8(void){
    return IR_default_tab[ir[8]];
  }
  unsigned short IR_get9(void){
    return IR_default_tab[ir[9]];
  }