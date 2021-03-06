This is the software used by the Proteus robots in the Pharos
Mobile Computing Laboratory at UT.  The software is modularized into the
following directories:
   
 - PharosMiddleware: This is the middleware that runs on top of Player and
   provides higher-level operations.  For example, it helps coordinate
   the start of a multi-robot GPS-based navigation experiment, and performs
   GPS-based waypoint navigation.  This is written in Java.

 - Proteus2: Contains the software used by the Proteus 2 robot.

 - Proteus3: Contains the software used by the Proteus 3 robot.

 - Scripts: This contains scripts that are useful for parsing the log
   files generated by the Pharos Middleware.

 - Utils: Contains small programs that are useful for running multi-robot 
   experiments.
