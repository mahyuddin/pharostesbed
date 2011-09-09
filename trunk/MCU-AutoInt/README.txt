This is the firmware that is installed on the 9S12 MCU on the Proteus robot. 
It is specific to the Autonomous Intersection Demo.  Basically, it reads
one of the ADC registers whose value is set by a short range IR sensor facing vertical.  It sends this information to the Proteus driver on the x86 where it is used to detect the intersection.
Compile and load it onto the MCU using CodeWarrior.

See Interrupt-Notes.txt for details on which interrupts are used.
See LED-Notes for details on the meaning of the LEDs.

