This is a simple Arduino program that allows you to control a servo through the serial
console.  The signal line of the servo should be attached to PWM pin 9 of the Arduino.

To control the position of the server, type the desired pulse width in microseconds.
Usually, 1500 moves the servo to the center, 500 moves the servo to its maximum
counter-clockwise direction, and 2500 moves the servo to its maximum clockwise
direction.

For more details, see:
http://pharos.ece.utexas.edu/wiki/index.php/Evaluating_the_Signal_for_Controlling_a_Servo#Manually_Controlling_the_Servo.27s_Position
