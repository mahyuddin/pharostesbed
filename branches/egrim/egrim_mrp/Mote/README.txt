README for RadioSignalMeter

Author/Contact:

  Chien-Liang Fok <liangfok@mail.utexas.edu>

Description:

  Periodically beacons a messages and listens for other beacons.
  Records the RSSI and LQI of the beacons received and sends this
  information over the UART.
  
  To change the radio power edit the Makefile and change the value
  of CC2420_DEF_RFPOWER. This variable has a default value of 31 and
  is defined in /opt/tinyos-2.x/tos/chips/cc2420/CC2420.h.

  To change the radio channel, edit the Makefile and change the
  value of CC2420_DEF_CHANNEL.  This variable has a default value of
  26 and is defined in /opt/tinyos-2.x/tos/chips/cc2420/CC2420.h.
  
  Under normal operation:
    - Green LED blinks = beacon broadcasted
    - Blue LED blinks = beacon received
    - Red LED blinks = error sending results over UART
    - All three LEDs blink = fatal error

Tools:

  None

Known bugs/limitations:

If you run into the following error when compiling:
  
  $ make
  mig -target=null -java-classname=OscilloscopeMsg java ../MultihopOscilloscope.h
  oscilloscope -o OscilloscopeMsg.java
  In file included from /usr/include/stdio.h:45,
                   from /opt/tinyos-2.x/tos/system/tos.h:9:
  /usr/include/sys/reent.h:185: syntax error before `('

This may be a problem with Cygwin 1.7.7.  This can be fixed by opening
/usr/include/sys/reent.h and changing each instance of:

  ...  _EXFNPTR(_blah, ...

to be:
  ... _EXFUN((*_blah), ...
  
For more information, see:
  http://www.mail-archive.com/tinyos-help@millennium.berkeley.edu/msg35024.html
