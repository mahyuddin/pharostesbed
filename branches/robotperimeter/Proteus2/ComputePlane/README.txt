This is software used by the Proteus 2 computational plane.

- MCU: This is the code that runs on the 9S12 micro-controller. This is 
  written in C.

- MCU-SimonSays: This is the 9S12 MCU code for the Simon Says demo.

- Mote: This is the software that runs on the TelosB mote, which contains
  a MSP430 MCU and 802.15.4 wireless interface.  It is written in NesC,
  and requires the TinyOS operating system.

- ProteusDriver: This is the Player Driver that allows the Player
  middleware to communicate with the robot's MCU.  The Player middleware
  provides basic access to robot sensors and actuators, e.g., movement
  and sensing commands.  This is written in C/C++.