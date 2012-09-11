#include <SoftwareSerial.h>
//#include <Servo.h>

SoftwareSerial swSerial(11, 10);  // RX, TX

#define LED_PIN 13  // This is connected to the on-board LED

#define STEERING_PWM_PIN 9
/*#define STEERING_MAX_LEFT 20
#define STEERING_CENTER 100
#define STEERING_MAX_RIGHT 180

#define STEERING_MIN_PULSE 1000
#define STEERING_MAX_PULSE 2000

Servo steeringServo;*/

// Define constants and variables used for controlling the SyRen
byte START_BYTE = 170;
byte SYREN_ADDR = 128;
byte CMD_FORWARD = 0;
byte CMD_BACKWARD = 1;
byte _cmd = CMD_FORWARD;
byte _data = 0;
byte _checksum = 0;

// Define variables for interfacing with the x86
byte _ledState = 0;
char _buff[10];
int _indx = 0;
boolean _init = false;

unsigned long _prevUpdateTime = 0;

void setup() {
  Serial.begin(115200);
  swSerial.begin(2400);
  pinMode(STEERING_PWM_PIN, OUTPUT);
  //steeringServo.attach(STEERING_PWM_PIN, STEERING_MIN_PULSE, STEERING_MAX_PULSE);
  //steeringServo.write(STEERING_CENTER);
  //steeringServo.detach(STEERING_PWM_PIN);
  pinMode(LED_PIN, OUTPUT);  // enable LED
}

void loop() {
  unsigned long currTime = millis();
  if (calcTimeDiff(_prevUpdateTime, currTime) >= 20) {
    _prevUpdateTime = currTime;
    digitalWrite(STEERING_PWM_PIN, HIGH);
    //delayMicroseconds(1900); // max-right
    delayMicroseconds(1500); // center
    //delayMicroseconds(1162); // max-left
    digitalWrite(STEERING_PWM_PIN, LOW);
  }
  
  // Read in a speed command
  while (Serial.available() && _indx < 9) {
    _buff[_indx++] = Serial.read();
    if (_buff[_indx-1] == 0x0A) {  // if last char is a newline char
      _indx = 0;
      if (_buff[0] == 'i') {
        swSerial.write(START_BYTE); // set the baud rate
        _init = true;
        Serial.println("Motor Controller Initialized.");
      } else {
        int speed = atoi(_buff);
      
        if (speed <= 127 && speed >= -127) {
          if (speed > 0) {
            _cmd = CMD_FORWARD;
            _data = speed;
          } else {
            _cmd = CMD_BACKWARD;
            _data = -speed;
          }
          _checksum = 0x7F & (SYREN_ADDR + _cmd + _data);
      
          Serial.print("Speed: ");
          Serial.print(speed);
          Serial.print(", Cmd: ");
          Serial.print(_cmd);
          Serial.print(", Data: ");
          Serial.print(_data);
          Serial.print(", Checksum: ");
          Serial.println(_checksum);
        } else {
          Serial.println("Invalid speed.");
        }
        digitalWrite(LED_PIN, _ledState);
        _ledState = !_ledState;
      }
    }
  }

  if (_init) {
    swSerial.write(SYREN_ADDR); // send address byte
    swSerial.write(_cmd);
    swSerial.write(_data);
    swSerial.write(_checksum);
  }
  delay(100);
}

unsigned long calcTimeDiff(unsigned long time1, unsigned long time2) {
  if (time1 > time2) {
    unsigned long maxUL = 0xFFFFFFFF;
    return maxUL - time2 + time1;
  } else
    return time2 - time1;
}
