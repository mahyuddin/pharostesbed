// $Id: LEDBlinkerC.nc,v 1.1 2005/10/17 05:04:04 chien-liang Exp $

/* LED Blinker - A utility for blinking the LEDs in various
 * patterns.
 *
 * By Chien-Liang Fok.
 * 
 */
configuration LedsBlinkerC {
	provides interface LedsBlinker;
}
implementation {
	components MainC, LedsBlinkerP, LedsC;
	components new TimerMilliC() as Timer0;
	
	LedsBlinkerP.Boot -> MainC;
	
	LedsBlinker = LedsBlinkerP;
	
	LedsBlinkerP.Timer0 -> Timer0;
	LedsBlinkerP.Leds -> LedsC;
}

