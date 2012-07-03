/*
CMPS03 with Arduino I2C example.  For use with Proteus Player Driver.

The SDA line is on analog pin 4 of the arduino and is connected to pin 3 of the CMPS03.
The SCL line is on analog pin 5 of the arduino and is conected to pin 2 of the CMPS03.
Both SDA and SCL are also connected to the +5v via a couple of 1k8 resistors.
A switch to calibrate the CMPS03 can be connected between pin 6 of the CMPS03 and the ground.
*/
#include <Wire.h>

#define COMPASS_ADDRESS 0x60 //defines address of compass
#define PROTEUS_BEGIN              0x24
#define PROTEUS_END                0x0A
#define PROTEUS_COMPASS_PACKET     0x04
#define I2C_DATA                   0x00

byte buf[6];

void setup(){
  buf[0] = PROTEUS_BEGIN;
  buf[1] = PROTEUS_COMPASS_PACKET;
  buf[2] = I2C_DATA;
  buf[5] = PROTEUS_END;
  Wire.begin(); //connects I2C
  Serial.begin(115200);
}

void loop(){
  byte highByte;
  byte lowByte;
  
   Wire.beginTransmission(COMPASS_ADDRESS);      //starts communication with cmps03
   Wire.write(2);                         //Sends the register we wish to read
   Wire.endTransmission();

   Wire.requestFrom(COMPASS_ADDRESS, 2);        //requests high byte
   while(Wire.available() < 2);         //while there is a byte to receive
   highByte = Wire.read();           //reads the byte as an integer
   lowByte = Wire.read();
   buf[3] = highByte;
   buf[4] = lowByte; 
   
   Serial.write(buf, 6);
   delay(100);
}
