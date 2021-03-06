#include <SoftwareSerial.h>

SoftwareSerial swSerial(11, 10);  // RX, TX

#define LED_PIN 13  // This is connected to the on-board LED

// Define constants and variables used for controlling the SyRen
//byte START_BYTE = 170;
//byte SYREN_ADDR = 128;
//byte CMD_FORWARD = 0;
//byte CMD_BACKWARD = 1;
byte CMD_STOP = 127;
byte _cmd = CMD_STOP;
//byte _data = 0;
//byte _checksum = 0;

// Define variables for interfacing with the x86
byte _ledState = 0;
char _buff[10];
int _indx = 0;
//boolean _init = false;

void setup() {
  Serial.begin(115200);
  
  swSerial.begin(9600);
  
  
  pinMode(LED_PIN, OUTPUT);  // enable LED
}

void loop() {
  // Read in a speed command
  while (Serial.available() && _indx < 9) {
    _buff[_indx++] = Serial.read();
    if (_buff[_indx-1] == 0x0A) {  // if last char is newline char
      /*if (!_init) {
        if (_indx == 2 && _buff[0] == 'i') {
          //delay(3000); // allow motor controller to boot
 
          swSerial.write(START_BYTE); // set the baud rate
          //delay(1000);
          _init = true;
          Serial.println("Motor Controller Initialized.");
        } else {
          Serial.println("Must initialize motor by pressing 'i'.");
        }
      } else {*/
        int speed = atoi(_buff);
      
        if (speed <= 255 && speed >= 0) {
          //if (speed > 0) {
            _cmd = speed;
            //_data = speed;
          //} else {
            //_cmd = CMD_BACKWARD;
            //_data = -speed;
          //}
          //_checksum = 0x7F & (SYREN_ADDR + _cmd + _data);
      
          Serial.print("Speed: ");
          Serial.println(_cmd);
          //Serial.print(", Cmd: ");
          //Serial.print(_cmd);
          //Serial.print(", Data: ");
          //Serial.print(_data);
          //Serial.print(", Checksum: ");
          //Serial.println(_checksum);
        } else {
          Serial.println("Invalid speed.");
        }
      //}
      _indx = 0;
      digitalWrite(LED_PIN, _ledState);
      _ledState = !_ledState; 
    }
  }

  
    //swSerial.write(SYREN_ADDR); // send address byte
    swSerial.write(_cmd);
    //swSerial.write(_data);
    //swSerial.write(_checksum);
  
  delay(100);
}
