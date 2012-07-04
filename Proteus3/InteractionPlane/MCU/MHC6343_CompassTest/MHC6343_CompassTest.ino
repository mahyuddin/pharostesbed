/**
 * Tests the HMC6343 compass by using it to obtain the heading, pitch, and roll at 10Hz.
 * Publishes the data by printing it to the serial port.  View the data using the serial
 * monitor in the Arduino IDE.
 *
 * Two LEDs are assumed to be installed: a green one on digital pin 6, and a red one on
 * digital pin 7.  The green LED is turned on after Arduino is initialized. The red LED 
 * is blinked each time the compass is accessed.
 */
#include <Wire.h>

/**
 * Define some useful constants.
 */
#define HMC6343_ADDRESS 0x19
#define HMC6343_HEADING_REG 0x50

int GREEN_LED = 6;
int RED_LED = 7;

void setup() {
  pinMode(GREEN_LED, OUTPUT);
  pinMode(RED_LED, OUTPUT);
  Wire.begin(); // Initialize the I2C bus
  Serial.begin(115200); // Initialize the serial bus
  digitalWrite(GREEN_LED, HIGH); // Turn on the green LED to incate system started
}

void loop() {
  byte highByte, lowByte;
  
  Wire.beginTransmission(HMC6343_ADDRESS);    // Start communicating with the HMC6343 compasss
  Wire.write(HMC6343_HEADING_REG);             // Send the address of the register that we want to read
  Wire.endTransmission();

  Wire.requestFrom(HMC6343_ADDRESS, 6);    // Request six bytes of data from the HMC6343 compasss
  while(Wire.available() < 1);             // Busy wait while there is no byte to receive
  
  highByte = Wire.read();              // Reads in the bytes and convert them into proper degree units.
  lowByte = Wire.read();
  float heading = ((highByte << 8) + lowByte) / 10.0; // the heading in degrees

  highByte = Wire.read();
  lowByte = Wire.read();
  float pitch = ((highByte << 8) + lowByte) / 10.0;   // the pitch in degrees
  
  highByte = Wire.read();
  lowByte = Wire.read();
  float roll = ((highByte << 8) + lowByte) / 10.0;    // the roll in degrees
  
  Serial.print("Heading=");             // Print the sensor readings to the serial port.
  Serial.print(heading);
  Serial.print(", Pitch=");
  Serial.print(pitch);
  Serial.print(", Roll=");
  Serial.println(roll);
  
  digitalWrite(RED_LED, HIGH);
  delay(20);
  digitalWrite(RED_LED, LOW);
  delay(80);
  
  //delay(100); // Do this at approx 10Hz
}
