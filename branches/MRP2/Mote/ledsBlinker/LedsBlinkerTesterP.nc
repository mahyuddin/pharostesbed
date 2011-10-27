// $Id: ErrMgrC.nc,v 1.4 2006/05/18 19:58:40 chien-liang Exp $

#include <Leds.h>

module LedsBlinkerTesterP @safe() {
	uses {
		interface Boot;
		interface LedsBlinker;
	}
}
implementation {
	task void doTest();
	
	uint8_t testno;
	
	event void Boot.booted()  { 
		testno = 0;
		post doTest();
		dbg("LedsBlinker", "LedsBlinkerTesterP.booted.\n");
	}

	task void doTest() {
		error_t e = FAIL;
		
		switch(testno) {
			case 0:
			dbg("LedsBlinker", "LedsBlinkerTesterP.doTest: blinking LED 0...\n");
			e = call LedsBlinker.blink(LEDS_LED0, 3 /*count*/, 100 /*period in ms*/);
			break;
			case 1:
			dbg("LedsBlinker", "LedsBlinkerTesterP.doTest: blinking LED 1...\n");
			e = call LedsBlinker.blink(LEDS_LED1, 3 /*count*/, 100 /*period in ms*/);
			break;
			case 2:
			dbg("LedsBlinker", "LedsBlinkerTesterP.doTest: blinking LED 2...\n");
			e = call LedsBlinker.blink(LEDS_LED2, 3 /*count*/, 100 /*period in ms*/);
			break;
			case 3:
			dbg("LedsBlinker", "LedsBlinkerTesterP.doTest: blinking LEDs 0 and 1...\n");
			e = call LedsBlinker.blink(LEDS_LED0 | LEDS_LED1, 6 /*count*/, 50 /*period in ms*/);
			break;
			case 4:
			dbg("LedsBlinker", "LedsBlinkerTesterP.doTest: blinking LEDs 1 and 2...\n");
			e = call LedsBlinker.blink(LEDS_LED1 | LEDS_LED2, 6 /*count*/, 50 /*period in ms*/);
			break;
			case 5:
			dbg("LedsBlinker", "LedsBlinkerTesterP.doTest: blinking LEDs 0, 1, and 2...\n");
			e = call LedsBlinker.blink(LEDS_LED0 | LEDS_LED1 | LEDS_LED2, 12 /*count*/, 50 /*period in ms*/);
			break;
		}
		if (e == SUCCESS)
			testno++;
	}
	
	event void LedsBlinker.blinkDone() {
		if (testno <= 5)
			post doTest();
		else {
			dbg("LedsBlinker", "LedsBlinkerTesterP: Done!\n");
		}
	}
}
