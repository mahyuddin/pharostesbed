// filename ******** Sharp_IR.h **************
// A Sharp IR driver for the Proteus Robot
// For use with Proteus IR Switch V3 circuit board

/*
 ** Pin connections **
    
    PAD00 -> Front Left Sensor
    PAD01 -> Front Center Sensor
    PAD02 -> Front Right Sensor
    PAD03 -> Rear Left Sensor
    PAD04 -> Rear Center Sensor
    PAD05 -> Rear Right Sensor
    

 ** Feature Usage **
   
   6 pins on ADC0
*/

#include <mc9s12dp512.h>     /* derivative information */
#include "adc.h"
#include "Sharp_IR.h"


// A table that converts raw IR data to mm
// For most accurate results, generate this table
// in the environment you are using
// generated in ENS526W (florecent lighting)
/*unsigned short IR_default_tab[256] = {
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
 };*/
 
/**
 * Converts the raw ADC reading into an actual distance
 * in mm.  Note that this is specific to the short range
 * IR sensor, and that it is only valid for distances
 * between 30cm and 85cm.
 */ 
/*unsigned short calibrateShortRangeIR(unsigned char rawADC) {
  if (rawADC < 41)
    return 0xFFFF;
  else if (rawADC < 117)
    return 35469 / rawADC;
  else
    return 0xFFFF;
}*/

unsigned char IR_getFL(void){
  unsigned char raw;
  raw = (unsigned char)ADC0_In(ADC_IR_FL);
  //return calibrateShortRangeIR(raw);
  return raw;
  //return IR_default_tab[raw]; //front left
}

unsigned char IR_getFC(void){
  unsigned char raw;
  raw = (unsigned char)ADC0_In(ADC_IR_FC);
  //return calibrateShortRangeIR(raw);
  return raw;
  //return IR_default_tab[raw]; //front center
}

unsigned char IR_getFR(void){
  unsigned char raw;
  raw = (unsigned char)ADC0_In(ADC_IR_FR);
  //return calibrateShortRangeIR(raw);
  return raw;
  //return IR_default_tab[raw]; //front right
}

unsigned char IR_getRR(void){
  unsigned char raw;
  raw = (unsigned char)ADC0_In(ADC_IR_RL);
  //return calibrateShortRangeIR(raw);
  return raw;
  //return IR_default_tab[raw]; //rear right
}

unsigned char IR_getRC(void){
  unsigned char raw;
  raw = (unsigned char)ADC0_In(ADC_IR_RC);
  //return calibrateShortRangeIR(raw);
  return raw;
  //return IR_default_tab[raw]; //rear center
}

unsigned char IR_getRL(void){
  unsigned char raw;
  raw = (unsigned char)ADC0_In(ADC_IR_RR);
  //return calibrateShortRangeIR(raw);
  return raw;
  //return IR_default_tab[raw]; //rear left
}


   


