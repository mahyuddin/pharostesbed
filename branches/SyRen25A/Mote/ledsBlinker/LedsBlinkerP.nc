// $Id: LEDBlinkerC.nc,v 1.1 2005/10/17 05:04:04 chien-liang Exp $


/* LED Blinker - A utility for blinking the LEDs in various
 * patterns.
 *
 * By Chien-Liang Fok.
 */
module LedsBlinkerP @safe() {
	provides interface LedsBlinker;
	uses {
		interface Boot;
		interface Leds;
		interface Timer<TMilli> as Timer0;
	}
}
implementation {
	bool _blinking;
	uint16_t _whichLEDs, _count, _curr, _period;
	
	event void Boot.booted() {
		_blinking = FALSE;
	}
	
	void setLEDs(uint8_t whichLEDs) {
		if (whichLEDs & 1) call Leds.led0Toggle();
		if (whichLEDs & 2) call Leds.led1Toggle();
		if (whichLEDs & 4) call Leds.led2Toggle();  
	}
	
	command error_t LedsBlinker.blink(uint8_t whichLEDs, uint16_t count, uint16_t period) {
		if (!_blinking) {
			_blinking = TRUE;
			_whichLEDs = whichLEDs;
			_count = count*2;
			_period = period;
			setLEDs(_whichLEDs);
			_curr = 1;      
			call Timer0.startPeriodic(_period);
			dbg("LedsBlinker", "LedsBlinker: Blinking LEDs 0x%x, count=%i, period=%i\n", whichLEDs, count, period);
			return SUCCESS;
		} else 
			return EBUSY;
	}
	
	event void Timer0.fired() {
		setLEDs(_whichLEDs);
		if (++_curr == _count) {
			call Timer0.stop();
			_blinking = FALSE;
			signal LedsBlinker.blinkDone();
		}
	}
	
	default event void LedsBlinker.blinkDone() {}
}
