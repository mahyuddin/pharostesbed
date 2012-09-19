/**
 * Centers the front wheels of the Traxxas Stampede chassis.
 *
 * @author Chien-Liang Fok
 */
#include <Servo.h>

#define LEFT 20
#define CENTER 100
#define RIGHT 180
#define PAUSE 15
#define MIN_PULSE 1000
#define MAX_PULSE 2000
Servo servo;
int pos;

void setup() {
  servo.attach(9, MIN_PULSE, MAX_PULSE);
  servo.write(CENTER);
}

void loop() {
}
