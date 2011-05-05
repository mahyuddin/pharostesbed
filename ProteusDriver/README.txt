This is the driver for the Pharos Robot used by Player Server.
It runs on the Proteus mother plane's x86 processor.
Compile it by typing 'make'.  

After compiling the program, there will be a file called libproteusdriver.so.  
Copy this to the following directory on the mother plane's x86 computer:

/usr/local/share/player/modules/

For example, here is how to copy it onto the robot using scp:

$ scp libproteusdriver.so ut@[robot ip address]:/usr/local/share/player/modules/

