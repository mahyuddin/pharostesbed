/**
 * The interface for blinking the LEDs.
 *
 * @author Chien-Liang Fok
 */
interface LedsBlinker {
	
	/**
	 * Starts the blinking of LEDs.
	 * 
	 * @param val Specifies which LEDs to blink.  The LEDs of the platform are enumerated 0-7.
	 * Each bit in val corresponds to one LED.
	 * @param count The number of times to blink.
	 * @param The period between blinks.
	 * @return SUCCESS if the blinking process is started.  This fails if another
	 * blink process is still running.
	 */
	command error_t blink(uint8_t val, uint16_t count, uint16_t period);
	
	/**
	 * Signalled when the blinking is done and blink(...) can be called again.
	 */
	event void blinkDone();
}
