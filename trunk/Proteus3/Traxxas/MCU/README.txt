The Traxxas Stampede mobility chassis used by the Proteus 3 has its own
processor based on the Arduino Pro Mini 5V 16MHz w/ Atmel ATmega328.
This directory contains various firmwares that can be installed on this
processor.

 - EncoderTest: spins the wheel slowly while sending the encoder values 
   over the serial port.  Be sure to the rear wheels are elevated when 
   running this!

 - SpeedSteeringManualControl: allows user to manually control the speed 
   via '+' and '-', and steering angle via '/' and '*' in serial console.

 - SteeringAngleManualControl: allows user to manually control the steering 
   angle via '+' and '-' in serial console.

 - SteeringAngleSweep: sweeps the steering angle stopping at the limits and
   the center.

