/**
 * Enables manual control of a servo.
 *
 * Attach servo's signal pin to PWM port 9 of the Arduino Mega.  Also attach
 * the servo's 5V and GND pins to the corresponding port on the Arduino Mega.
 *
 * After programing the Arduino with this sketch, open a serial console and
 * set the line ending to be "Newline".  Then type the desired pulse width
 * in milliseconds followed by [enter].
 *
 * Typically values:
 * - 500: max left
 * - 1500: center
 * - 2500: max right
 *
 * @author Chien-Liang Fok
 * @date 02/20/2012
 */
#include <Servo.h>

#define SERVO_PIN 9
#define LED_PIN 13  // This is connected to the on-board LED
#define SERVO_CENTER 1500

Servo servo;
byte _ledState = 0;
char _buff[10];
int _indx = 0;
  
void setup() {
  Serial.begin(115200);
  pinMode(LED_PIN, OUTPUT);  // Initialize LED
  servo.attach(SERVO_PIN);
  servo.writeMicroseconds(SERVO_CENTER);
}

void loop() {
  while (Serial.available() && _indx < 9) {
    _buff[_indx++] = Serial.read();
    if (_buff[_indx-1] == 0x0A) {  // if last char is newline char
      _indx = 0;
      int pulseWidth = atoi(_buff);
        
      Serial.print("Pulse width: ");
      Serial.println(pulseWidth);
  
      servo.writeMicroseconds(pulseWidth);
    
      digitalWrite(LED_PIN, _ledState);
      _ledState = !_ledState; 
    }
  }
}
