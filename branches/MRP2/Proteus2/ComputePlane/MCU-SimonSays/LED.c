#include <mc9s12dp512.h>     /* derivative information */
#include "LED.h"

/**
 * Initialize the LEDs.
 */
void LED_Init(void) {
	DDRT |= 0xF0;    //PT4-7 output to LEDs
	DDRH |= 0xFC;    //PH2-7 output to LEDs
	PTT &= ~0xF0;
	PTH &= ~0xFC;
	
	LED_BLUE1 = 0;
	LED_BLUE2 = 0;
	
	LED_GREEN1 = 0;
	LED_GREEN2 = 0;
	
	LED_YELLOW1 = 0;
	LED_YELLOW2 = 0;
	
	LED_ORANGE1 = 0;
	LED_ORANGE2 = 0;
	
	LED_RED1 = 0;
	LED_RED2 = 0;
	LED_RED3 = 0;
}
